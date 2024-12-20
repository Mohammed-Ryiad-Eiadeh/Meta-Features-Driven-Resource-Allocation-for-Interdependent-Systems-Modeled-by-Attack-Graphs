package Attack_Defence_Graph.org;

import Defender.org.Defenders;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class is used to read different samples attack-defence graph
 */
public class GraphData {
    private Defenders[][] defenders;
    private int numOfEdges;
    private double[] assetLossVector;

    /**
     * This constructor is used to construct a matrix of defenders according to the given data
     */
    public GraphData(Path graphPath) {
        try {
            List<String> lines = Files.readAllLines(Path.of(String.valueOf(graphPath)));
            int numOfNodes = Integer.parseInt(lines.get(0).split("\s")[0]);
            defenders = new Defenders[numOfNodes][numOfNodes];
            for (int i = 1; i < lines.size() - 1; i++) {
                String[] twoVertex = lines.get(i).split("\s");
                int nodeI = Integer.parseInt(twoVertex[0]) - 1;
                int nodeJ = Integer.parseInt(twoVertex[1]) - 1;
                switch (twoVertex.length) {
                    case 2 -> defenders[nodeI][nodeJ] = new Defenders(1);
                    case 3 -> defenders[nodeI][nodeJ] = new Defenders(Double.parseDouble(twoVertex[2]));
                }
            }
            for (int r = 0; r < defenders.length; r++) {
                for (int c = 0; c < defenders[0].length; c++) {
                    if (defenders[r][c] == null) {
                        defenders[r][c] = new Defenders(0);
                    }
                }
            }
            Arrays.stream(defenders).forEach(defender -> IntStream.range(0, defenders[0].length).
                            filter(col -> defender[col].totalInvest() > 0).forEach(col -> numOfEdges++));

            String[] lossLine = lines.get(lines.size() - 1).split("\s");
            assetLossVector = new double[lossLine.length - 1];
            for (int i = 0; i < assetLossVector.length; i++) {
                assetLossVector[i] = Double.parseDouble(lossLine[i + 1].trim());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is used to retrieve the losses of each asset of the desired graph problem
     * @return The vector of asset losses
     */
    public double[] getNodeAssetsLossValues() {
        return assetLossVector;
    }

    /**
     * This method is used to return the adjacency matrix of the wanted graph problem
     * @param defenders The matrix of defenders and their initial investments
     * @return The adjacency matrix
     */
    public double[][] getAdjacencyMatrix(Defenders[][] defenders) {
        if (defenders == null) {
            throw new IllegalArgumentException("The defenders are null!");
        }
        var adjMat = new double[defenders.length][defenders[0].length];
        for (var row = 0; row < defenders.length; row++) {
            for (var col = 0; col < defenders[0].length; col++) {
                adjMat[row][col] = defenders[row][col].totalInvest();
            }
        }
        return adjMat;
    }

    /**
     * This method is used to visualize the attack-defence graph problem
     * @param data The adjacency matrix of the graph
     */
    public void Display(double[][] data) {
        if (data == null) {
            throw new IllegalArgumentException("The matrix is null!");
        }
        SingleGraph visualizer = new SingleGraph("graph");
        visualizer.setStrict(false);
        visualizer.setAutoCreate(true);
        for (int node = 0; node < data.length; node++) {
            for (int nod = 0; nod < data[0].length; nod++) {
                if (data[node][nod] > 0) {
                    String nodeI = node + 1 + "";
                    String nodeJ = nod + 1 + "";
                    visualizer.addNode(nodeI);
                    visualizer.addNode(nodeJ);
                    visualizer.addEdge(nodeI.concat(nodeJ), nodeI, nodeJ, true);
                }
            }
        }
        visualizer.nodes().forEach(x -> x.setAttribute("label", x.getId()));
        List<Edge> listEdges = visualizer.edges().toList();
        for (Edge listEdge : listEdges) {
            int edgeId = Integer.parseInt(listEdge.getNode0().getId().split("->")[0]) - 1;
            int nodeId = Integer.parseInt(listEdge.getNode1().getId().split("->")[0]) - 1;
            listEdge.setAttribute("label", data[edgeId][nodeId]);
        }
        System.setProperty("org.graphstream.ui", "swing");
        visualizer.display();
    }

    /**
     * This method is used to display the adjacency matrix of the investments
     * @param AdjMat Return the adjacency matrix of the investments
     */
    public void DisplayTheAdjacencyMatrix(double[][] AdjMat) {
        if (AdjMat == null) {
            throw new IllegalArgumentException("The matrix of investments are null!");
        }
        System.out.println("The adjacent matrix of the investments as weights:");
        for (double[] vi : AdjMat) {
            for (double vj : vi) {
                System.out.print(Math.round(vj * 100) / 100.0 + "\t");
            }
            System.out.println();
        }
    }

    /**
     * This method is used to retrieve the total summation of loss units based on the given attack path
     * @param AttackPathByDE The attack path by the attacker
     * @param assetsLossValues The vector of losses as each loss referred to a particular node
     * @return The summation of losses if the attack is occurred
     */
    public double getValueOfLoss(List<Integer> AttackPathByDE, double[] assetsLossValues) {
        if (AttackPathByDE == null) {
            throw new IllegalArgumentException("The solution is null!");
        }
        if (assetsLossValues == null) {
            throw new IllegalArgumentException("The loss vector is null!");
        }
        double sumOfLoss = 0.0d;
        for (Integer integer : AttackPathByDE) {
            sumOfLoss += assetsLossValues[integer - 1];
        }
        return sumOfLoss;
    }

    /**
     * This method is used to return the defenders' matrix corresponding to the given graph
     * @return The wanted graph as matrix of defenders
     */
    public Defenders[][] getAttackDefenceGraph() {
        return defenders;
    }

    /**
     * This method is used to retrieve the number of edges in the given graph
     * @return The number of edges
     */
    public int getNumberOfEdges() {
        return numOfEdges;
    }
}