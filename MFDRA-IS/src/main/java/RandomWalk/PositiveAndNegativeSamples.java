package RandomWalk;

import GraphCentralityMethods.org.PageRank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PositiveAndNegativeSamples {
    private final double[][] adjMatrix;
    private final double[] assetLossVec;
    private final ArrayList<ArrayList<Integer>> sequences;
    private final int NumberOfTopNegativeSampleNodes;
    private final int NumberOfRemainingNegativeSample;

    /**
     * This constructor is used to initialize the adjacency matrix that is going to be passed to page rank
     * @param adjMatrix The adjacency matrix which represents the attack-defence graph
     * @param lossVector The assets' loss values as a vector
     * @param sequences The sequences which is the random walks
     */
    public PositiveAndNegativeSamples(double[][] adjMatrix, double[] lossVector, ArrayList<ArrayList<Integer>> sequences) {
        if (adjMatrix == null) {
            throw new IllegalArgumentException("The matrix is null!");
        }
        if (lossVector == null) {
            throw new IllegalArgumentException("The loss vector is null");
        }
        if (sequences == null) {
            throw new IllegalArgumentException("The random walks are null");
        }
        this.adjMatrix = adjMatrix;
        this.assetLossVec = lossVector;
        this.sequences = sequences;
        int negativeSampleNodeCounter = 0;
        for (double v : lossVector) {
            if (v > 0) {
                negativeSampleNodeCounter += 1;
            }
        }
        this.NumberOfTopNegativeSampleNodes = (int) (negativeSampleNodeCounter * 0.6);
        this.NumberOfRemainingNegativeSample = negativeSampleNodeCounter - NumberOfTopNegativeSampleNodes;
    }

    public ArrayList<Dataset> generatePositiveSamplesViaBigRamAndNegativeSamples() {
        ArrayList<Dataset> dataset = new ArrayList<>();
        for (ArrayList<Integer> sequence : sequences) {
            for (int i = 0; i < sequence.size() - 1; i++) {
                dataset.add(new Dataset(sequence.get(i), sequence.get(i + 1), "positive"));
            }
        }
        PageRank pageRank = new PageRank(adjMatrix, assetLossVec, 100, PageRank.PR_DenominatorType.numOfOutEdges);
        HashMap<Integer, Double> mapOfScores = pageRank.getScores();

        ArrayList<Map.Entry<Integer, Double>> listOfAssetsScores = new ArrayList<>(mapOfScores.entrySet());
        listOfAssetsScores.sort(Comparator.comparing(Map.Entry<Integer, Double>::getValue).reversed());

        List<Map.Entry<Integer, Double>> listOfTopAssetNodes = listOfAssetsScores.subList(0, NumberOfTopNegativeSampleNodes);
        List<Map.Entry<Integer, Double>> listOfRemainingAssets = listOfAssetsScores.subList(NumberOfRemainingNegativeSample, listOfAssetsScores.size());
        ArrayList<Integer> nonAssetsNode = new ArrayList<>();
        for (int node = 0; node < adjMatrix.length; node++) {
            if (assetLossVec[node] == 0) {
                nonAssetsNode.add(node + 1);
            }
        }
        for (Integer integer : nonAssetsNode) {
            for (Map.Entry<Integer, Double> listOfTopAssetNode : listOfTopAssetNodes) {
                int node = integer;
                int asset = listOfTopAssetNode.getKey();
                if (adjMatrix[node - 1][asset - 1] == 0) {
                    if (!dataset.contains(new Dataset(node, asset - 1, "negative"))) {
                        dataset.add(new Dataset(node, asset - 1, "negative"));
                    }
                    int asset2 = listOfRemainingAssets.get(new Random().nextInt(listOfRemainingAssets.size())).getKey();
                    if (adjMatrix[node - 1][asset2 - 1] == 0) {
                        if (!dataset.contains(new Dataset(node, asset2 - 1, "negative"))) {
                            dataset.add(new Dataset(node, asset2 - 1, "negative"));
                        }
                    }
                }
            }
        }
        Collections.shuffle(dataset);
        return dataset;
    }
}
