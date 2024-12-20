package RandomWalk;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.RandomWalkVertexIterator;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class RandomWalk {
    private final DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;
    private final double[] AssetLossVec;
    private final int NumOfHops;

    /**
     * This constructor is used to initialize the adjacency matrix that is going to be passed to page rank
     * @param lossVector The assets' loss values as a vector
     * @param numOfHops The moves of each random walk
     */
    public RandomWalk(DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph, double[] lossVector, int numOfHops) {
        if (graph == null) {
            throw new IllegalArgumentException("The graph is null!");
        }
        if (lossVector == null) {
            throw new IllegalArgumentException("The loss vector is null");
        }
        if (numOfHops < 0) {
            throw new IllegalArgumentException("The number of hops in the random walks should be positive integer");
        }
        this.graph = graph;
        this.AssetLossVec = lossVector;
        this.NumOfHops = numOfHops;
    }

    /**
     * This method is used to call the
     * @return The
     */
    public ArrayList<ArrayList<Integer>> getSequences() {
        // call the RandomWalk approach
        RandomWalkVertexIterator<Integer, DefaultWeightedEdge> randomWalkVertexIterator;
        ArrayList<ArrayList<Integer>> sequence = new ArrayList<>();
        for (var node : graph.vertexSet()) {
            if (AssetLossVec[node] == 0) {
                for (int numOfPaths = 0; numOfPaths < 5; numOfPaths++) {
                    randomWalkVertexIterator = new RandomWalkVertexIterator<>(graph, node, NumOfHops);
                    ArrayList<Integer> path = new ArrayList<>();
                    while (randomWalkVertexIterator.hasNext()) {
                        path.add(randomWalkVertexIterator.next() + 1);
                    }
                    sequence.add(path);
                }
            }
        }
        sequence = (ArrayList<ArrayList<Integer>>) sequence.stream().distinct().collect(Collectors.toList());
        sequence.removeIf(walk -> walk.size() < 2);
        return sequence;
    }
}

