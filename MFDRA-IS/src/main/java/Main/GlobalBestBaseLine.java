package Main;

import StoreRetrieveHashmap.org.RetrieveResourceAllocationAndTopAllocationAsHashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class GlobalBestBaseLine {
    public static void main(String[] args) throws IOException {
        var trainLines = Files.readAllLines(Path.of(System.getProperty("user.dir"), "\\Train Random Walks.csv"));
        trainLines.remove(0);
        var mapClassCount = new HashMap<String, Integer>();
        var sTime = System.currentTimeMillis();
        for (var line : trainLines) {
            var breakLine = line.split(",");
            if (mapClassCount.containsKey(breakLine[breakLine.length - 1])) {
                mapClassCount.replace(breakLine[breakLine.length - 1], mapClassCount.get(breakLine[breakLine.length - 1]) + 1);
            } else {
                mapClassCount.put(breakLine[breakLine.length - 1], 1);
            }
        }
        mapClassCount.entrySet().forEach(System.out::println);
        var globalBest = mapClassCount
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        System.out.println("The global best resource allocation is : " + globalBest);

        var performanceMatrix = RetrieveResourceAllocationAndTopAllocationAsHashMap
                .getPerformanceMatrixAsHashMap(Path.of(System.getProperty("user.dir") + "\\Allocation Results.csv"));

        var testLines = Files.readAllLines(Path.of(System.getProperty("user.dir"), "\\Test Random Walks.csv"));
        testLines.remove(0);

        // define the map of top resource allocation approaches
        var map = RetrieveResourceAllocationAndTopAllocationAsHashMap
                .getTopAllocationApproaches("Top Allocation Methods");

        var top_K = 10;
        var hit_At_K_acc = 0d;
        var accuracy = 0d;
        var avgAllocationPerformance = 0d;
        for (var line : testLines) {
            var breakLine = line.split(",");
            var actualLabel = breakLine[breakLine.length - 1];
            if (Objects.equals(actualLabel, globalBest)) {
                accuracy++;
            }
            var index = performanceMatrix.get("Allocation").indexOf(globalBest);
            var costRelativeReduction = performanceMatrix.get(breakLine[0]).get(index);
            avgAllocationPerformance += Double.parseDouble(costRelativeReduction);
            ArrayList<String> top_K_GroundTruths = new ArrayList<>();
            for (int i = 0; i < top_K; i++) {
                var graphID = line.split(",")[0];
                top_K_GroundTruths.add(map.get(graphID).get(i).toUpperCase(Locale.ROOT));
            }
            if (top_K_GroundTruths.contains(globalBest.toUpperCase(Locale.ROOT))) {
                hit_At_K_acc++;
            }
        }
        var eTime = System.currentTimeMillis();
        System.out.println("The accuracy is : " + accuracy / testLines.size());
        System.out.println("The average performance is : " + avgAllocationPerformance / testLines.size());
        System.out.println("The Hit_At_K accuracy is : " + hit_At_K_acc / testLines.size());
        System.out.println("The duration time is : " + (eTime - sTime));
    }
}
