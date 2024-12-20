package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.IntStream;

public class MainComputingCosineSimilarity {
    public static void main(String[] args) throws IOException {
            // Read the data
            var lines = Files.readAllLines(Path.of(System.getProperty("user.dir") + "\\Embeddings Random Walks.csv"));

            // Passing the header
            lines.remove(0);

            // Process all the data into a hashmap
            var vectors = new HashMap<String, double[]>();

            for (var item : lines) {
                var breakLine = item.split(",", 2);
                var value = new double[breakLine[1].split(",").length - 1];
                var vectorElements = breakLine[1].split(",");
                for (var i = 0; i < value.length - 1; i++) {
                    value[i] = Double.parseDouble(vectorElements[i]);
                }
                vectors.put(breakLine[0], value);
            }

            var mapEachVecToCloseOnes = new HashMap<>();

            var  keys = vectors.keySet().toArray(String[]::new);
            for (var vec1 = 0; vec1 < vectors.size(); vec1++) {
                var vectorWithCosineSimilarities = new ArrayList<VecSimilarity>();
                for (var key : keys) {
                    var similarity = computeCosineSimilarity(vectors.get(keys[vec1]), vectors.get(key));
                    vectorWithCosineSimilarities.add(new VecSimilarity(key, similarity));
                }
                vectorWithCosineSimilarities.sort(Comparator.comparing(VecSimilarity::cosineSimilarity).reversed());
                mapEachVecToCloseOnes.put(keys[vec1], vectorWithCosineSimilarities);
            }

            // Print the results
            for (var graphMetaData : mapEachVecToCloseOnes.entrySet()) {
                System.out.println(graphMetaData.getKey() + "\t" + graphMetaData.getValue() + "\n");
            }
        }

    static double computeCosineSimilarity(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("The two vectors should have the same length");
        }
        // This for cosine similarity
        double dotProduct = IntStream.rangeClosed(0, vec1.length - 1).mapToDouble(i -> vec1[i] * vec2[i]).sum();
        double vec1Magnitude = Math.sqrt(IntStream.rangeClosed(0, vec1.length - 1).mapToDouble(i -> vec1[i] * vec1[i]).sum());
        double vec2Magnitude = Math.sqrt(IntStream.rangeClosed(0, vec2.length - 1).mapToDouble(i -> vec2[i] * vec2[i]).sum());
        return dotProduct / (vec1Magnitude * vec2Magnitude);
    }

    record VecSimilarity(String graphId, double cosineSimilarity) { }
}


