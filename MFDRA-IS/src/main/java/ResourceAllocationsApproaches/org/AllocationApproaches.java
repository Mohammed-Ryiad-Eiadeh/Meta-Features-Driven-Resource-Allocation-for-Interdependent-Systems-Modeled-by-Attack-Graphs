package ResourceAllocationsApproaches.org;

import Attack_Defence_Graph.org.GraphData;
import BehavioralDefense.org.BehavioralDefender;
import CostFunction.org.CostFunction;
import Defender.org.Defenders;
import GraphAnalysisMethods.org.AdjacentNodes;
import GraphAnalysisMethods.org.InDegreeNodes;
import GraphAnalysisMethods.org.MarkovBlanket;
import GraphAnalysisMethods.org.MinCutEdges;
import GraphCentralityMethods.org.BetweennessCentralityMethod;
import GraphCentralityMethods.org.DegreeCentralityMethod;
import GraphCentralityMethods.org.HarmonicCentralityMethod;
import GraphCentralityMethods.org.KatzCentralityMethod;
import GraphCentralityMethods.org.PageRank;
import GraphCentralityMethods.org.TrustRankAlgorithm;
import RiskBasedDefense.org.RiskBasedDefense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class AllocationApproaches {
    private final GraphData task;
    private final Defenders[][] DefendersMatrix;
    private final double[][] AdjMatrix;
    private final HashMap<Integer, ArrayList<List<List<Integer>>>> concurrentAttack;
    private final double[] AssetLossVector;
    private double ExpectedCostReduction;
    private final int SecurityBudget;

    public AllocationApproaches(GraphData task, Defenders[][] defenders, double[][] adjMatrix, HashMap<Integer, ArrayList<List<List<Integer>>>> concurrentAttack, double[] assetLossVector, int securityBudget) {
        if (task == null) {
            throw new IllegalArgumentException("The graph task object is null!");
        }
        if (defenders == null) {
            throw new IllegalArgumentException("The defenders object is null!");
        }
        if (adjMatrix == null) {
            throw new IllegalArgumentException("The adjacent matrix is null!");
        }
        if (concurrentAttack == null) {
            throw new IllegalArgumentException("The concurrent attack object is null!");
        }
        if (assetLossVector == null) {
            throw new IllegalArgumentException("The concurrent attack object is null!");
        }
        if (securityBudget <= 0) {
            throw new IllegalArgumentException("The security budget have to be positive integer!");
        }
        this.task = task;
        this.DefendersMatrix = defenders;
        this.AdjMatrix = adjMatrix;
        this.concurrentAttack = concurrentAttack;
        this.AssetLossVector = assetLossVector;
        this.SecurityBudget = securityBudget;
        this.ExpectedCostReduction = 0.0;
    }

    private double calculateTheCostBeforeAllocation() {
        // Get the simultaneous attacks to simulate real attackers
        HashMap<Integer, ArrayList<List<List<Integer>>>> concurrentAttacks = concurrentAttack;
        // Get the assets involving into this attack paths
        Set<Integer> assets = concurrentAttacks.keySet();
        // Calculate the cost for the generated paths before allocating the resources
        CostFunction costFunctionBeforeAllocation = new CostFunction(AdjMatrix, AssetLossVector);
        double costsBeforeAllocation = 0.0d;
        for (Integer asset : assets) {
            ArrayList<List<List<Integer>>> pathsToThisAsset = concurrentAttacks.get(asset);
            double cost = 0.0;
            for (List<List<Integer>> paths : pathsToThisAsset) {
                for (List<Integer> path : paths) {
                    cost += costFunctionBeforeAllocation.computeCost(path);
                }
            }
            costsBeforeAllocation += cost / concurrentAttacks.get(asset).size();
            costsBeforeAllocation += costsBeforeAllocation / pathsToThisAsset.size();
        }
        return costsBeforeAllocation;
    }

    private double calculateTheCostAfterAllocation(double[][] updatedAdjacentMatrix) {
        // Get the simultaneous attacks to simulate real attackers
        HashMap<Integer, ArrayList<List<List<Integer>>>> concurrentAttacks = concurrentAttack;
        // Get the assets involving into this attack paths
        Set<Integer> assets = concurrentAttacks.keySet();
        CostFunction costFunctionAfterAllocation = new CostFunction(updatedAdjacentMatrix, AssetLossVector);
        double costsAfterAllocation = 0.0d;
        for (Integer asset : assets) {
            ArrayList<List<List<Integer>>> pathsToThisAsset = concurrentAttacks.get(asset);
            double cost = 0.0;
            for (List<List<Integer>> paths : pathsToThisAsset) {
                for (List<Integer> path : paths) {
                    cost += costFunctionAfterAllocation.computeCost(path);
                }
            }
            costsAfterAllocation += cost / concurrentAttacks.get(asset).size();
            costsAfterAllocation += costsAfterAllocation / pathsToThisAsset.size();
        }
        return costsAfterAllocation;
    }

    public void callRiskBasedDefense() {
        // Select the test case or the graph; construct the defenders; construct the adjacency matrix; display the graph.
        GraphData graphTask = task;
        Defenders[][] defendersMatrix = deepCopy(DefendersMatrix);

        // Calculate the cost before the allocation process
        double costsBeforeAllocation = calculateTheCostBeforeAllocation();

        // Set the spare budget of security resources for each defender
        Defenders.spareBudget_D = SecurityBudget;

        // Get the total risk to do normalizing
        double totalRisk = new RiskBasedDefense(AdjMatrix).totalRisk();

        // Set the defender's security budget
        double budget = Defenders.spareBudget_D;

        // This code segment is referred to allocate spare defenders investments on paths involved in "in" and "out" degree edges
        for (int i = 0; i < AdjMatrix.length; i++) {
            for (int j = 0; j < AdjMatrix[0].length; j++) {
                if (defendersMatrix[i][j].totalInvest() > 0) {
                    Defenders edge = defendersMatrix[i][j];
                    edge.setInvest_D(edge.addSpareInvestFor_D(budget * (Math.exp(-edge.totalInvest()) / totalRisk)));
                }
            }
        }
        // Update the adjacency matrix
        double[][] modifiedAdjMatrix = graphTask.getAdjacencyMatrix(defendersMatrix);
        // Calculate the cost after the allocation process
        double costsAfterAllocation = calculateTheCostAfterAllocation(modifiedAdjMatrix);
        // Calculate the expected cost reduction
        ExpectedCostReduction = Math.abs(costsBeforeAllocation - costsAfterAllocation) / costsBeforeAllocation * 100;
    }

    public void callDefenseInDepth() {
        // Select the test case or the graph; construct the defenders; construct the adjacency matrix; display the graph.
        GraphData graphTask = task;
        Defenders[][] defendersMatrix = deepCopy(DefendersMatrix);

        // Calculate the cost before the allocation process
        double costsBeforeAllocation = calculateTheCostBeforeAllocation();

        // Set the spare budget of security resources for each defender
        Defenders.spareBudget_D = SecurityBudget;

        // This code segment is referred to get the number of all edges in the graph
        int numOfEdges = task.getNumberOfEdges();
        double budget = Defenders.spareBudget_D / numOfEdges;

        // This code segment is referred to allocate spare defenders investments on all edges equally
        for (int i = 0; i < AdjMatrix.length; i++) {
            for (int j = 0; j < AdjMatrix[0].length; j++) {
                if (defendersMatrix[i][j].totalInvest() > 0) {
                    Defenders edge = defendersMatrix[i][j];
                    edge.setInvest_D(edge.addSpareInvestFor_D(budget));
                }
            }
        }
        // Update the adjacency matrix
        double[][] modifiedAdjMatrix = graphTask.getAdjacencyMatrix(defendersMatrix);
        // Calculate the cost after the allocation process
        double costsAfterAllocation = calculateTheCostAfterAllocation(modifiedAdjMatrix);
        // Calculate the expected cost reduction
        ExpectedCostReduction = Math.abs(costsBeforeAllocation - costsAfterAllocation) / costsBeforeAllocation * 100;
    }

    public void callBehavioralDefender() {
        // Select the test case or the graph; construct the defenders; construct the adjacency matrix; display the graph.
        GraphData graphTask = task;
        Defenders[][] defendersMatrix = deepCopy(DefendersMatrix);

        // Calculate the cost before the allocation process
        double costsBeforeAllocation = calculateTheCostBeforeAllocation();

        // Set the spare budget of security resources for each defender
        Defenders.spareBudget_D = SecurityBudget;

        // This code segment is referred to allocate spare defenders investments on all attack paths according to behavioral defender
        double budget = Defenders.spareBudget_D;

        // Apply the behavioral defender
        BehavioralDefender behavioralDefender = new BehavioralDefender(AdjMatrix, 0.5f);
        double[][] newWeights = behavioralDefender.applyBehavioralDefendingForResourceAllocation();
        double sumOfProbWeighting = 0.0d;
        for (double[] newWeight : newWeights) {
            for (int j = 0; j < newWeights[0].length; j++) {
                sumOfProbWeighting += newWeight[j];
            }
        }
        for (int i = 0; i < AdjMatrix.length; i++) {
            for (int j = 0; j < AdjMatrix[0].length; j++) {
                if (AdjMatrix[i][j] > 0) {
                    Defenders edge = defendersMatrix[i][j];
                    edge.setInvest_D(edge.addSpareInvestFor_D(newWeights[i][j] / sumOfProbWeighting * budget));
                }
            }
        }
        // Update the adjacency matrix
        double[][] modifiedAdjMatrix = graphTask.getAdjacencyMatrix(defendersMatrix);
        // Calculate the cost after the allocation process
        double costsAfterAllocation = calculateTheCostAfterAllocation(modifiedAdjMatrix);
        // Calculate the expected cost reduction
        ExpectedCostReduction = Math.abs(costsBeforeAllocation - costsAfterAllocation) / costsBeforeAllocation * 100;
    }

    public void callMarkovBlanket() {
        // Select the test case or the graph; construct the defenders; construct the adjacency matrix; display the graph.
        GraphData graphTask = task;
        Defenders[][] defendersMatrix = deepCopy(DefendersMatrix);

        // Calculate the cost before the allocation process
        double costsBeforeAllocation = calculateTheCostBeforeAllocation();

        // Set the spare budget of security resources for each defender
        Defenders.spareBudget_D = SecurityBudget;

        // This code segment is referred to capture the edges involved in Markov blanket of each asset
        MarkovBlanket markovBlanket = new MarkovBlanket(AdjMatrix, AssetLossVector);
        HashMap<Integer, HashMap<String, ArrayList<Integer>>> nodes = markovBlanket.retrieveNodeOfMarkovBlanket();

        int size = nodes.values().stream().mapToInt(i -> i.values().stream().mapToInt(ArrayList::size).sum()).sum();
        double budget = Defenders.spareBudget_D / size;

        // This code segment is referred to allocate the resources on the edges covered by Markov Blanket
        for (Integer assetNode : nodes.keySet()) {
            HashMap<String, ArrayList<Integer>> allNeighbors = nodes.get(assetNode);
            for (String type : allNeighbors.keySet()) {
                ArrayList<Integer> nodeList = allNeighbors.get(type);
                for (Integer nod : nodeList) {
                    Defenders edge = (Objects.equals(type, "Children")) ? defendersMatrix[assetNode - 1][nod - 1] :
                            defendersMatrix[nod - 1][assetNode - 1];
                    edge.setInvest_D(edge.addSpareInvestFor_D(budget));
                }
            }
        }
        // Update the adjacency matrix
        double[][] modifiedAdjMatrix = graphTask.getAdjacencyMatrix(defendersMatrix);
        // Calculate the cost after the allocation process
        double costsAfterAllocation = calculateTheCostAfterAllocation(modifiedAdjMatrix);
        // Calculate the expected cost reduction
        ExpectedCostReduction = Math.abs(costsBeforeAllocation - costsAfterAllocation) / costsBeforeAllocation * 100;
    }

    public void callAdjacentNodes() {
        // Select the test case or the graph; construct the defenders; construct the adjacency matrix; display the graph.
        GraphData graphTask = task;
        Defenders[][] defendersMatrix = deepCopy(DefendersMatrix);

        // Calculate the cost before the allocation process
        double costsBeforeAllocation = calculateTheCostBeforeAllocation();

        // Set the spare budget of security resources for each defender
        Defenders.spareBudget_D = SecurityBudget;

        // This code segment is referred to capture the connections among each asset and its neighbor nodes
        AdjacentNodes adjacentNodes = new AdjacentNodes(AdjMatrix, AssetLossVector);
        HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> nodes = adjacentNodes.retrieveNodeOfFirstLevelDegree();

        // Divide the spare budget of security resources over the number of edges
        int size = nodes.values().stream().mapToInt(i -> i.values().stream().mapToInt(ArrayList::size).sum()).sum();
        double budget = Defenders.spareBudget_D / size;

        // This code segment is referred to allocate spare defenders investments on paths involved in "in" and "out" degree edges
        for (Integer assetNode : nodes.keySet()) {
            HashMap<Integer, ArrayList<Integer>> allNeighbors = nodes.get(assetNode);
            for (Integer type : allNeighbors.keySet()) {
                ArrayList<Integer> nodeList = allNeighbors.get(type);
                for (Integer nod : nodeList) {
                    Defenders edge = (type == 0) ? defendersMatrix[assetNode - 1][nod - 1] :
                            defendersMatrix[nod - 1][assetNode - 1];
                    edge.setInvest_D(edge.addSpareInvestFor_D(budget));
                }
            }
        }
        // Update the adjacency matrix
        double[][] modifiedAdjMatrix = graphTask.getAdjacencyMatrix(defendersMatrix);
        // Calculate the cost after the allocation process
        double costsAfterAllocation = calculateTheCostAfterAllocation(modifiedAdjMatrix);
        // Calculate the expected cost reduction
        ExpectedCostReduction = Math.abs(costsBeforeAllocation - costsAfterAllocation) / costsBeforeAllocation * 100;
    }

    public void callInDegreeNodes() {
        // Select the test case or the graph; construct the defenders; construct the adjacency matrix; display the graph.
        GraphData graphTask = task;
        Defenders[][] defendersMatrix = deepCopy(DefendersMatrix);

        // Calculate the cost before the allocation process
        double costsBeforeAllocation = calculateTheCostBeforeAllocation();

        // Set the spare budget of security resources for each defender
        Defenders.spareBudget_D = SecurityBudget;

        // This code segment is referred to capture the connections among each asset and its "in" degree nodes
        InDegreeNodes IDN = new InDegreeNodes(AdjMatrix, AssetLossVector);
        HashMap<Integer, ArrayList<Integer>> nodes = IDN.retrieveInDegreeNodes();

        // Divide the spare budget of security resources over the number of edges
        int size = nodes.values().stream().flatMap(ArrayList::stream).toList().size();
        double budget = Defenders.spareBudget_D / size;

        // This code segment is referred to allocate spare defenders investments on paths involved in "in" degree edges
        for (Integer assetNode : nodes.keySet()) {
            ArrayList<Integer> parents = nodes.get(assetNode);
            for (Integer nod : parents) {
                Defenders edge = defendersMatrix[nod - 1][assetNode - 1];
                edge.setInvest_D(edge.addSpareInvestFor_D(budget));
            }
        }
        // Update the adjacency matrix
        double[][] modifiedAdjMatrix = graphTask.getAdjacencyMatrix(defendersMatrix);
        // Calculate the cost after the allocation process
        double costsAfterAllocation = calculateTheCostAfterAllocation(modifiedAdjMatrix);
        // Calculate the expected cost reduction
        ExpectedCostReduction = Math.abs(costsBeforeAllocation - costsAfterAllocation) / costsBeforeAllocation * 100;
    }

    public void callMinCut() {
        // Select the test case or the graph; construct the defenders; construct the adjacency matrix; display the graph.
        GraphData graphTask = task;
        Defenders[][] defendersMatrix = deepCopy(DefendersMatrix);

        // Calculate the cost before the allocation process
        double costsBeforeAllocation = calculateTheCostBeforeAllocation();

        // Set the spare budget of security resources for each defender
        Defenders.spareBudget_D = SecurityBudget;

        // This code segment is referred to capture the min-cut edges among each asset-asset
        MinCutEdges MCE = new MinCutEdges(AdjMatrix, AssetLossVector);
        HashMap<Integer, ArrayList<MinCutEdges.MCEdge>> nodes = MCE.getMinCutEdges();

        // Divide the spare budget of security resources over the number of edges
        int size = nodes.values().stream().mapToInt(ArrayList::size).sum();
        double budget = Defenders.spareBudget_D / size;

        // This code segment is referred to allocate spare defenders investments on paths involved in "in" and "out" degree edges
        for (Integer assetNode : nodes.keySet()) {
            ArrayList<MinCutEdges.MCEdge> parents = nodes.get(assetNode);
            for (MinCutEdges.MCEdge nod : parents) {
                Defenders edge = defendersMatrix[nod.GetStart() - 1][nod.GetEnd() - 1];
                edge.setInvest_D(edge.addSpareInvestFor_D(budget));
            }
        }
        // Update the adjacency matrix
        double[][] modifiedAdjMatrix = graphTask.getAdjacencyMatrix(defendersMatrix);
        // Calculate the cost after the allocation process
        double costsAfterAllocation = calculateTheCostAfterAllocation(modifiedAdjMatrix);
        // Calculate the expected cost reduction
        ExpectedCostReduction = Math.abs(costsBeforeAllocation - costsAfterAllocation) / costsBeforeAllocation * 100;
    }

    public void callCentrality(LinkageType linkageType, Centrality centrality) {
        // Select the test case or the graph; construct the defenders; construct the adjacency matrix; display the graph.
        GraphData graphTask = task;
        Defenders[][] defendersMatrix = deepCopy(DefendersMatrix);

        // Calculate the cost before the allocation process
        double costsBeforeAllocation = calculateTheCostBeforeAllocation();

        // Set the spare budget of security resources for each defender
        Defenders.spareBudget_D = SecurityBudget;
        double budget = Defenders.spareBudget_D;

        // This code segment is referred to rank each asset according to centrality algorithms
        HashMap<Integer, Double> scores = switch (centrality) {
            case Betweenness -> new BetweennessCentralityMethod(AdjMatrix, AssetLossVector).getScores();
            case Degree ->      new DegreeCentralityMethod(AdjMatrix, AssetLossVector).getScores();
            case Harmonic ->    new HarmonicCentralityMethod(AdjMatrix, AssetLossVector).getScores();
            case Katz ->        new KatzCentralityMethod(AdjMatrix, AssetLossVector).getScores();
            case TrustRank ->   new TrustRankAlgorithm(AdjMatrix, AssetLossVector, 100).getScores();
            case PageRank ->    new PageRank(AdjMatrix, AssetLossVector, 100, PageRank.PR_DenominatorType.sumOfOutEdgesWeights).getScores();
        };

        switch (linkageType) {
            case In_Degree_Nodes -> {
                // This code segment is referred to capture the connections among each asset and its neighbor nodes
                InDegreeNodes IDN = new InDegreeNodes(AdjMatrix, AssetLossVector);
                HashMap<Integer, ArrayList<Integer>> nodes = IDN.retrieveInDegreeNodes();

                // This code segment is referred to allocate spare defenders investments on paths involved in "in" and "out" degree edges
                for (Integer assetNode : nodes.keySet()) {
                    ArrayList<Integer> parents = nodes.get(assetNode);
                    for (Integer nod : parents) {
                        Defenders edge = defendersMatrix[nod - 1][assetNode - 1];
                        int sizeParents = nodes.get(assetNode).stream().toList().size();
                        double currentAssetCutOfTotalBudget = budget * scores.get(assetNode) / sizeParents;
                        edge.setInvest_D(edge.addSpareInvestFor_D(currentAssetCutOfTotalBudget));
                    }
                }
            }
            case Adjacent_Nodes -> {
                // This code segment is referred to capture the connections among each asset and its neighbor nodes
                AdjacentNodes AdjacentNodes = new AdjacentNodes(AdjMatrix, AssetLossVector);
                HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> nodes = AdjacentNodes.retrieveNodeOfFirstLevelDegree();

                // This code segment is referred to allocate spare defenders investments on paths involved in "in" and "out" degree edges
                for (Integer assetNode : nodes.keySet()) {
                    HashMap<Integer, ArrayList<Integer>> allNeighbors = nodes.get(assetNode);
                    for (Integer type : allNeighbors.keySet()) {
                        ArrayList<Integer> nodeList = allNeighbors.get(type);
                        for (Integer nod : nodeList) {
                            Defenders edge = (type == 0) ? defendersMatrix[assetNode - 1][nod - 1] :
                                    defendersMatrix[nod - 1][assetNode - 1];
                            int sizeOfNeighborNodes = nodes.get(assetNode).values().stream().
                                    flatMap(Collection::stream).toList().size();
                            double currentAssetCutOfTotalBudget = budget * scores.get(assetNode) / sizeOfNeighborNodes;
                            edge.setInvest_D(edge.addSpareInvestFor_D(currentAssetCutOfTotalBudget));
                        }
                    }
                }
            }
            case Markov_Blanket -> {
                // This code segment is referred to capture the connections among each asset and its Markov blanket nodes
                MarkovBlanket MarkovBlanket = new MarkovBlanket(AdjMatrix, AssetLossVector);
                HashMap<Integer, HashMap<String, ArrayList<Integer>>> nodes = MarkovBlanket.retrieveNodeOfMarkovBlanket();

                // This code segment is referred to allocate the resources according to each assets' rank and its Markov blanket
                for (Integer assetNode : nodes.keySet()) {
                    HashMap<String, ArrayList<Integer>> allNeighbors = nodes.get(assetNode);
                    for (String type : allNeighbors.keySet()) {
                        ArrayList<Integer> nodeList = allNeighbors.get(type);
                        for (Integer nod : nodeList) {
                            Defenders edge = (Objects.equals(type, "Children")) ? defendersMatrix[assetNode - 1][nod - 1] :
                                    defendersMatrix[nod - 1][assetNode - 1];
                            int sizeOfCapturedNodesByMB = nodes.get(assetNode).values().stream().
                                    flatMap(Collection::stream).toList().size();
                            double currentAssetCutOfTotalBudget = budget * scores.get(assetNode) / sizeOfCapturedNodesByMB;
                            edge.setInvest_D(edge.addSpareInvestFor_D(currentAssetCutOfTotalBudget));
                            budget -= currentAssetCutOfTotalBudget;
                        }
                    }
                }
            }
            case Min_Cut_Edges -> {
                // This code segment is referred to capture the min-cut edges among the sink and destination node (asset)
                MinCutEdges minCutEdges = new MinCutEdges(AdjMatrix, AssetLossVector);
                HashMap<Integer, ArrayList<MinCutEdges.MCEdge>> nodes = minCutEdges.getMinCutEdges();

                // This code segment is referred to allocate spare defenders investments on paths involved in "in" and "out" degree edges
                for (Integer assetNode : nodes.keySet()) {
                    ArrayList<MinCutEdges.MCEdge> parents = nodes.get(assetNode);
                    for (MinCutEdges.MCEdge nod : parents) {
                        Defenders edge = defendersMatrix[nod.GetStart() - 1][nod.GetEnd() - 1];
                        int sizeParents = nodes.get(assetNode).stream().toList().size();
                        double currentAssetCutOfTotalBudget = budget * scores.get(assetNode) / sizeParents;
                        edge.setInvest_D(edge.addSpareInvestFor_D(currentAssetCutOfTotalBudget));
                    }
                }
            }
        }
        // Update the adjacency matrix
        double[][] modifiedAdjMatrix = graphTask.getAdjacencyMatrix(defendersMatrix);
        // Calculate the cost after the allocation process
        double costsAfterAllocation = calculateTheCostAfterAllocation(modifiedAdjMatrix);
        // Calculate the expected cost reduction
        ExpectedCostReduction = Math.abs(costsBeforeAllocation - costsAfterAllocation) / costsBeforeAllocation * 100;
    }

    public enum LinkageType {
        In_Degree_Nodes,
        Markov_Blanket,
        Adjacent_Nodes,
        Min_Cut_Edges
    }

    public enum Centrality {
        Betweenness,
        Degree,
        Harmonic,
        Katz,
        TrustRank,
        PageRank
    }

    private Defenders[][] deepCopy(Defenders[][] originalVersion) {
        Defenders[][] copyVersion = new Defenders[originalVersion.length][];
        for (int i = 0; i < originalVersion.length; i++) {
            copyVersion[i] = new Defenders[originalVersion[i].length];
            for (int j = 0; j < originalVersion[i].length; j++) {
                copyVersion[i][j] = originalVersion[i][j].clone();
            }
        }
        return copyVersion;
    }

    public double getExpectedCostReduction() {
        return ExpectedCostReduction;
    }
}

