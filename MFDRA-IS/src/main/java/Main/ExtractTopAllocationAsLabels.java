package Main;

import StoreHashmap.org.StoreHashmap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtractTopAllocationAsLabels implements StoreHashmap {
    public static void main(String[] args) throws IOException {
        List<String> data = Files.readAllLines(Path.of(System.getProperty("user.dir"), "\\Allocation Results.csv"));
        String[] header = data.remove(0).split(",");

        HashMap<String, HashMap<String, Double>> topAllocationsWithOrdering = new HashMap<>();
        for (String datum : data) {
            String[] breakLine = datum.split(",");
            String graphID = null;
            HashMap<String, Double> allocation_costReduction = new HashMap<>();
            for (int ii = 1; ii < breakLine.length; ii++) {
                graphID = breakLine[0];
                allocation_costReduction.put(header[ii], Double.parseDouble(breakLine[ii]));
            }
            // Sort the map and store it as a LinkedHashMap to preserve the order
            HashMap<String, Double> sortedMap = allocation_costReduction
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparing(Map.Entry<String, Double>::getValue).reversed())
                    .collect(Collectors.toMap(
                            Map.Entry<String, Double>::getKey,
                            Map.Entry<String, Double>::getValue,
                            (e1, e2) -> e1, // In case of duplicate keys, keep the first one
                            LinkedHashMap::new // This ensures the sorted order is preserved
                    ));
            topAllocationsWithOrdering.put(graphID, sortedMap);
        }
        StoreHashmap.storeHashMapHashmapEntries(topAllocationsWithOrdering, "Top Allocation Methods");
    }
}
