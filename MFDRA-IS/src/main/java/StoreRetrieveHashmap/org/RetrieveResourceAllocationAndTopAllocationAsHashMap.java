package StoreRetrieveHashmap.org;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface RetrieveResourceAllocationAndTopAllocationAsHashMap {
    static HashMap<String, List<String>> getPerformanceMatrixAsHashMap(Path path) {
        HashMap<String, List<String>> map = new HashMap<>();
        try {
            List<String> data = Files.readAllLines(Path.of(String.valueOf(path)));
            for (String line : data) {
                List<String> breakLine = new ArrayList<>(Arrays.stream(line.split(",")).toList());
                String key = breakLine.remove(0);
                if (Objects.equals(key, "GraphID")) {
                    map.put("Allocation", breakLine);
                    continue;
                }
                map.put(key, breakLine);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return map;
    }

    static HashMap<String, ArrayList<String>> getTopAllocationApproaches(String fileName) {
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\" + fileName + ".csv"))) {
            List<String> lines =  bufferedReader.lines().collect(Collectors.toList());
            for (String line : lines) {
                String[] breakLine = line.split(",");
                String key = breakLine[0];
                ArrayList<String> value = new ArrayList<>();
                for (int i = 1; i < breakLine.length; i += 2) {
                    value.add(breakLine[i]);
                }
                map.put(key, value);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return map;
    }
}
