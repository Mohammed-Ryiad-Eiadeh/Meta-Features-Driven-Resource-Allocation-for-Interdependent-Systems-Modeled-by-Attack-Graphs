package GraphAnalysisMethods.org;

import org.jgrapht.alg.flow.DinicMFImpl;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MinCutEdges {
    private final double[][] adjMatrix;
    private final double[] assetLossVec;

    /**
     * This constructor is used to initialize the adjacency matrix and prepare it for JGraphT API to conduct some graph theory approaches
     * @param adjMatrix The adjacency matrix which represents the attack-defence graph
     * @param lossVector The assets' loss values as a vector
     */
    public MinCutEdges(double[][] adjMatrix, double[] lossVector) {
        if (adjMatrix == null) {
            throw new IllegalArgumentException("The matrix is null!");
        }
        if (lossVector == null) {
            throw new IllegalArgumentException("The loss vector is null");
        }
        this.adjMatrix = adjMatrix;
        this.assetLossVec = lossVector;
    }

    /**
     * This method is used to retrieve all min-cut edges foe all entry-asset variations
     * @return All min-cut edges among each entry-asset
     */
    public HashMap<Integer, ArrayList<MCEdge>> getMinCutEdges() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        double[][] Data = this.adjMatrix;
        for (int node = 0; node < Data[0].length; node++) {
            graph.addVertex(node);
        }
        for (int node = 0; node < Data.length; node++) {
            for (int nod = 0; nod < Data[0].length; nod++) {
                if (Data[node][nod] > 0) {
                    graph.addEdge(node, nod);
                    graph.setEdgeWeight(node, nod, Math.exp(-Data[node][nod]));
                }
            }
        }
        DinicMFImpl<Integer, DefaultWeightedEdge> dinicMF = new DinicMFImpl<>(graph);
        ArrayList<Integer> entries = new ArrayList<>();
        ArrayList<Integer> assets = new ArrayList<>();
        // get the entries and the assets alone from each other
        for (int nod = 0; nod < adjMatrix.length; nod++) {
            if (assetLossVec[nod] > 0) {
                assets.add(nod + 1);
            }
            else {
                entries.add(nod + 1);
            }
        }
        // get the min-cut edges
        HashMap<Integer, ArrayList<MCEdge>> minCutEdges = new HashMap<>();
        for (int asset : assets) {
            ArrayList<MCEdge> listEdges = new ArrayList<>();
            for (int entry : entries) {
                dinicMF.calculateMinCut(entry - 1, asset - 1);
                Set<DefaultWeightedEdge> minCut = dinicMF.getCutEdges();
                ArrayList<DefaultWeightedEdge> list = new ArrayList<>(minCut);
                list.forEach(edge -> listEdges.add(new MCEdge(graph.getEdgeSource(edge) + 1, graph.getEdgeTarget(edge) + 1)));
                minCutEdges.put(asset, listEdges);
            }
        }
        /*for (int k : minCutEdges.keySet()) {
            for (MCEdge mcEdge : minCutEdges.get(k)) {
                System.out.println(k + "\t" + mcEdge.start + "\t" + mcEdge.end);
            }
        }*/
        return minCutEdges;
    }

    /**
     * This inner class is used to set the start and the end node of each edge
     */
    public class MCEdge {
        private final int start;
        private final int end;

        /**
         * This constructor is used to creat an object holds the start and the end nodes of an edge
         * @param start The start node of the given edge
         * @param end The end node of the given edge
         */
        public MCEdge(int start, int end) {
            this.start = start;
            this.end = end;
        }

        /**
         * This method is used to retrieve the start node of a given edge
         * @return The start node
         */
        public int GetStart() {
            return start;
        }

        /**
         * This method is used to retrieve the end node of a given edge
         * @return The end node
         */
        public int GetEnd() {
            return end;
        }
    }
}
