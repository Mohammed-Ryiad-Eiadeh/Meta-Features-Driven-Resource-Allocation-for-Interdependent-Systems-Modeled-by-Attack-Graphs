package Main;

import StoreRetrieveHashmap.org.RetrieveResourceAllocationAndTopAllocationAsHashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class ARGOSMARTMain {
    public static void main(String[] args) throws IOException {
        var trainLines = Files.readAllLines(Path.of(System.getProperty("user.dir"), "\\Train Random Walks.csv"));
        trainLines.remove(0);

        var testLines = Files.readAllLines(Path.of(System.getProperty("user.dir"), "\\Test Random Walks.csv"));
        testLines.remove(0);

        var performanceMatrix = RetrieveResourceAllocationAndTopAllocationAsHashMap
                .getPerformanceMatrixAsHashMap(Path.of(System.getProperty("user.dir") + "\\Allocation Results.csv"));

        // define the map of top resource allocation approaches
        var map = RetrieveResourceAllocationAndTopAllocationAsHashMap
                .getTopAllocationApproaches("Top Allocation Methods");

        var top_K = 10;
        var hit_At_K_acc = 0d;
        var accuracy = 0d;
        var avgAllocationPerformance = 0d;
        var sTime = System.currentTimeMillis();
        for (var testInstance : testLines) {
            var instanceTest = Arrays.stream(testInstance.split(",")).toList();
            var instanceTest0 = instanceTest.subList(1, instanceTest.size() - 1).stream().mapToDouble(Double::parseDouble).toArray();
            var mapMatchingAndSimilarity = new ArrayList<LabelWithSimilarity>();
            for (var trainInstance : trainLines) {
                var instanceTrain = Arrays.stream(trainInstance.split(",")).toList();
                var instanceTrain0 = instanceTrain.subList(1, instanceTrain.size() - 1).stream().mapToDouble(Double::parseDouble).toArray();
                var similarity = MainComputingCosineSimilarity.computeCosineSimilarity(instanceTest0, instanceTrain0);
                mapMatchingAndSimilarity.add(new LabelWithSimilarity(instanceTrain.get(instanceTrain.size() - 1), similarity));
            }
            mapMatchingAndSimilarity.sort(Comparator.comparing(LabelWithSimilarity::similarity).reversed());
            var predictedLabel = mapMatchingAndSimilarity.get(0).label;
            if (Objects.equals(instanceTest.get(instanceTest.size() - 1), predictedLabel)) {
                accuracy++;
            }
            ArrayList<String> top_K_GroundTruths = new ArrayList<>();
            for (int i = 0; i < top_K; i++) {
                var graphID = testInstance.split(",")[0];
                top_K_GroundTruths.add(map.get(graphID).get(i).toUpperCase(Locale.ROOT));
            }
            if (top_K_GroundTruths.contains(predictedLabel.toUpperCase(Locale.ROOT))) {
                hit_At_K_acc++;
            }
            var index = performanceMatrix.get("Allocation").indexOf(predictedLabel);
            var costRelativeReduction = performanceMatrix.get(instanceTest.get(0)).get(index);
            avgAllocationPerformance += Double.parseDouble(costRelativeReduction);
        }
        var eTime = System.currentTimeMillis();
        System.out.println("The accuracy is : " + accuracy / testLines.size());
        System.out.println("The average performance is : " + avgAllocationPerformance / testLines.size());
        System.out.println("The Hit_At_K accuracy is : " + hit_At_K_acc / testLines.size());
        System.out.println("The duration time is : " + (eTime - sTime));
    }
    record LabelWithSimilarity (String label, double similarity) { }
}

