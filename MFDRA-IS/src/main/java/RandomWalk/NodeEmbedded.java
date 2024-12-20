package RandomWalk;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.IntStream;

public class NodeEmbedded {
    private final int NUM_OF_NODE;
    private final ArrayList<Dataset> SAMPLES;
    private final int EMBEDDING_DIM;
    private final int NUM_EPOCHS;
    private final double LEARNING_RATE;
    private final int SeedOfRandomEmbedding;
    private final HashMap<Integer, double[]> embeddings;

    public NodeEmbedded(int numOfNodes, ArrayList<Dataset> samples, int embedding_dim, int num_epochs, double learning_rate, int seedOfRandomEmbedding) {
        NUM_OF_NODE = numOfNodes;
        SAMPLES = new ArrayList<>(samples);
        if (embedding_dim < 1) {
            throw new IllegalArgumentException("The number of the embedding dimension size have to be positive integer");
        }
        EMBEDDING_DIM = embedding_dim;
        if (num_epochs < 1) {
            throw new IllegalArgumentException("The number of the epochs have to be positive integer");
        }
        NUM_EPOCHS = num_epochs;
        if (learning_rate < Double.MIN_VALUE) {
            throw new IllegalArgumentException("The number of the embedding dimension size have to be positive");
        }
        if (seedOfRandomEmbedding < 1) {
            throw new IllegalArgumentException("The value seed have to be positive integer");
        }
        this.SeedOfRandomEmbedding = seedOfRandomEmbedding;
        LEARNING_RATE = learning_rate;
        embeddings = new HashMap<>(initializeEmbedding());
    }

    private HashMap<Integer, double[]> initializeEmbedding() {
        HashMap<Integer, double[]> embeddings = new HashMap<>();
        Random random = new Random(SeedOfRandomEmbedding);
        for (int node = 0; node < NUM_OF_NODE; node++) {
            double[] embeddingVec = new double[EMBEDDING_DIM];
            for (int embeddingElementIndex = 0; embeddingElementIndex < EMBEDDING_DIM; embeddingElementIndex++) {
                embeddingVec[embeddingElementIndex] = random.nextDouble() * 0.01;
            }
            embeddings.put(node + 1, embeddingVec);
        }
        return embeddings;
    }

    public void trainModel() {
        for (int iter = 0; iter < NUM_EPOCHS; iter++) {
            for (Dataset instance : SAMPLES) {
                int targetNode = instance.targetNode();
                int contextNode = instance.contextNode();
                double[] targetNodeEmbedding = embeddings.get(targetNode);
                double[] contextNodeEmbedding = embeddings.get(contextNode);

                double[] targetNodeGradient = computeGradient(targetNodeEmbedding, contextNodeEmbedding, instance.label());
                double[] contextNodeGradient = computeGradient(contextNodeEmbedding, targetNodeEmbedding, instance.label());

                updateEmbeddings(instance.targetNode(), targetNodeGradient);
                updateEmbeddings(instance.contextNode(), contextNodeGradient);
            }
            System.out.println("Epoch " + iter + " completed.");
        }
    }

    private void updateEmbeddings(int nodeIndex, double[] gradient) {
        double[] newEmbedding = new double[EMBEDDING_DIM];
        for (int embeddingElementIndex = 0; embeddingElementIndex < EMBEDDING_DIM; embeddingElementIndex++) {
            newEmbedding[embeddingElementIndex] = embeddings.get(nodeIndex)[embeddingElementIndex]
                    + LEARNING_RATE
                    * gradient[embeddingElementIndex];
        }
        embeddings.replace(nodeIndex, newEmbedding);
    }

    private double[] computeGradient(double[] embedding1, double[] embedding2, String label) {
        double[] gradient = new double[EMBEDDING_DIM];
        double dotProduct = IntStream.range(0, EMBEDDING_DIM)
                .mapToDouble(i -> embedding1[i] * embedding2[i])
                .sum();
        double prediction = sigmoid(dotProduct);
        double groundTruth = label.equals("positive") ? 1 : 0;
        double error = groundTruth - prediction;
        for (int embeddingElementIndex = 0; embeddingElementIndex < EMBEDDING_DIM; embeddingElementIndex++) {
            gradient[embeddingElementIndex] = error * embedding2[embeddingElementIndex];
        }
        return gradient;
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public HashMap<Integer, double[]> getEmbeddings() {
        return embeddings;
    }

    public void exportSingleGraphEmbeddings(Path path) {
        try (FileWriter fileWriter = new FileWriter(String.valueOf(path))) {
            StringBuilder header = new StringBuilder("Node");
            for (int i = 0; i < EMBEDDING_DIM; i++) {
                header.append(", Att").append(i + 1);
            }
            fileWriter.write(header.toString());
            fileWriter.write("\n");

            for (int i = 0; i < embeddings.size(); i++) {
                StringBuilder row = new StringBuilder();
                row.append(i + 1);
                for (int j = 0; j < EMBEDDING_DIM; j++) {
                    row.append(", ").append(embeddings.get(i + 1)[j]);
                }
                fileWriter.write(row.toString());
                fileWriter.write("\n");
            }
            fileWriter.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
