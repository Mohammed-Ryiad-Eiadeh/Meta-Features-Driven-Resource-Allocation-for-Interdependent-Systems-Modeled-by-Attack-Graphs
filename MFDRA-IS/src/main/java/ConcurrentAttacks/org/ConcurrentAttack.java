package ConcurrentAttacks.org;

import Attack_Defence_Graph.org.AttackDefenceGraph;
import EvolutionaryOptimizers.org.GeneticAlgorithm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is used to generate concurrent attacks to the network
 */
public class ConcurrentAttack {
    private final double[][] adjMatrix;
    private final double[] assetLossVec;
    private final int popSize;
    private final double mutationRate;
    private final double crossOverProb;
    private final double selectionProportion;
    private final int maxIter;

    /**
     * This is the default constructor of the concurrent attack generation
     * @param adjMatrix AdjMatrix The adjacensy matrix of the attack graph
     * @param assetLossVec The vector of the loss corresponding each asset
     * @param mutationRate The ratio of applying mutation or not
     * @param crossOverProb The ratio of applying crossover or not
     * @param selectionProportion The ratio of how many solutions should be selected and moved to next generation
     * @param maxIter The number of allowed generations
     */
    public ConcurrentAttack(double[][] adjMatrix, double[] assetLossVec, int populationSize, double mutationRate, double crossOverProb, double selectionProportion, int maxIter) {
        if (adjMatrix == null) {
            throw new IllegalArgumentException("The matrix is null!");
        }
        if (assetLossVec == null) {
            throw new IllegalArgumentException("The asset loss vector is null");
        }
        if (populationSize <= 0) {
            throw new IllegalArgumentException("The population size must be positive integer");
        }
        if (mutationRate < 0 || mutationRate > 1) {
            throw new IllegalArgumentException("The mutation rate must be relied in [0,1]");
        }
        if (crossOverProb < 0 || crossOverProb > 1) {
            throw new IllegalArgumentException("The cross over probability must be relied in [0,1]");
        }
        if (selectionProportion < 0 || selectionProportion > 1) {
            throw new IllegalArgumentException("The selection proportionate must be relied in [0,1]");
        }
        if (maxIter < 0) {
            throw new IllegalArgumentException("The max iteration must be positive integer");
        }
        this.adjMatrix = adjMatrix;
        this.assetLossVec = assetLossVec;
        this.popSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossOverProb = crossOverProb;
        this.selectionProportion = selectionProportion;
        this.maxIter = maxIter;
    }

    /**
     * This method is used to run the genetic algorithm to generate top-1 potential attack paths among each entry-asset variations
     * @param entry The attackers' entry node
     * @param asset The asset that the attacker is targeting
     * @return Return a set of feasible paths
     */
    private List<Integer> getPotentialAttackPath(int entry, int asset) {
        // Construct cyber graph object to generate a set of paths from the start to the end node and eventually get the best one
        AttackDefenceGraph cyberGraph = new AttackDefenceGraph(adjMatrix);
        var initialSetOfAttackPaths = cyberGraph.initialPopulation(entry, asset, popSize);

        // Construct a GA object to compute the most potential attack path for the given entry and asset nodes
        GeneticAlgorithm engine = new GeneticAlgorithm(adjMatrix,
                assetLossVec,
                initialSetOfAttackPaths,
                mutationRate,
                crossOverProb,
                selectionProportion,
                maxIter);
        engine.StartOptimization();

        // Getting the most potential attack path
        List<Integer> bestPath = engine.getBestCurrent();
        return bestPath;
    }

    /**
     * This method is used to run the genetic algorithm to generate all potential attacks among each entry-asset variations
     * @param entry The attackers' entry node
     * @param asset The asset that the attacker is targeting
     * @return Return a set of feasible paths
     */
    private List<List<Integer>> getAllPotentialAttackPathAmongTheseNode(int entry, int asset) {
        // Construct cyber graph object to generate a set of paths from the start to the end node and evantually get the best one
        AttackDefenceGraph cyberGraph = new AttackDefenceGraph(adjMatrix);
        var initialSetOfAttackPaths = cyberGraph.initialPopulation(entry, asset, popSize);

        // Construct a GA object to compute the most potential attack path for the given entry and asset nodes
        GeneticAlgorithm engine = new GeneticAlgorithm(adjMatrix,
                assetLossVec,
                initialSetOfAttackPaths,
                mutationRate,
                crossOverProb,
                selectionProportion,
                maxIter);
        engine.StartOptimization();

        // Getting the most potential attack path
        List<List<Integer>> allPaths = engine.getLastGeneration();
        return allPaths;
    }

    /**
     * This method is used to retrieve top-1 feasible paths that are generated between every entry node to each asset node
     * @return Return a map of assets and all corresponding paths to it
     */
    public HashMap<Integer, ArrayList<List<List<Integer>>>> getTop_1_Paths() {
        HashMap<Integer, ArrayList<List<List<Integer>>>> map = new HashMap<>();
        ArrayList<Integer> allAssets = new ArrayList<>();
        ArrayList<Integer> allEntries = new ArrayList<>();

        for (int i = 0; i < assetLossVec.length; i++) {
            if (assetLossVec[i] > 0) {
                allAssets.add(i + 1);
                map.put(i + 1, new ArrayList<>());
            } else {
                allEntries.add(i + 1);
            }
        }

        // Process paths in parallel
        IntStream.rangeClosed(0, allEntries.size() - 1).parallel().forEach(i -> {
            for (Integer allAsset : allAssets) {
                List<Integer> path = getPotentialAttackPath(allEntries.get(i), allAsset);
                if (!path.isEmpty() && path.size() > 2) {
                    synchronized (map) {
                        // Wrap the path in an additional list to match the desired structure
                        map.get(allAsset).add(Collections.singletonList(path)); // List<List<Integer>>
                    }
                }
            }
        });

        // Remove entries with empty path lists
        map.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        return map;
    }

    /**
     * This method is used to retrieve all feasible paths that are generated between every entry node to each asset node
     * @return Return a map of assets and all corresponding paths to it
     */
    public HashMap<Integer, ArrayList<List<List<Integer>>>> getAllPaths() {
        HashMap<Integer, ArrayList<List<List<Integer>>>> map = new HashMap<>();
        ArrayList<Integer> allAssets = new ArrayList<>();
        ArrayList<Integer> allEntries = new ArrayList<>();
        for (int i = 0; i < assetLossVec.length; i++) {
            if (assetLossVec[i] > 0) {
                allAssets.add(i + 1);
                map.put(i + 1, new ArrayList<>(new ArrayList<>()));
            }
            else {
                allEntries.add(i + 1);
            }
        }
        IntStream.rangeClosed(0, allEntries.size() - 1).parallel().forEach( i -> {
            for (Integer Asset : allAssets) {
                List<List<Integer>> allPaths = getAllPotentialAttackPathAmongTheseNode(allEntries.get(i), Asset);
                allPaths.removeIf(path -> path.size() <= 1);
                allPaths = allPaths.stream().distinct().collect(Collectors.toList());
                synchronized (map) {
                    map.get(Asset).add(allPaths);
                }
            }
        });
        for (Map.Entry<Integer, ArrayList<List<List<Integer>>>> entry : map.entrySet()) {
            ArrayList<List<List<Integer>>> paths = entry.getValue();
            paths.removeIf(List::isEmpty);
            for (List<List<Integer>> pathList : paths) {
                pathList.removeIf(List::isEmpty);
            }
        }
        map.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        return map;
    }
}