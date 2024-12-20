package RandomWalk;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class PrepareGraph {
    private final double[][] adjMatrix;

    /**
     * This constructor is used to initialize the adjacency matrix that is going to be passed to page rank
     * @param adjMatrix The adjacency matrix which represents the attack-defence graph
     */
    public PrepareGraph(double[][] adjMatrix) {
        if (adjMatrix == null) {
            throw new IllegalArgumentException("The matrix is null!");
        }
        this.adjMatrix = adjMatrix;
    }

    /**
     * This method is used to convert the adjacent matrix to graph in order to start to operate
     * @return The graph
     */
    public DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> getGraph () {
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
        return graph;
    }
}
