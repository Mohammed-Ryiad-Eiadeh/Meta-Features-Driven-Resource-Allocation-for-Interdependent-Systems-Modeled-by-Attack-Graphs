package StoreRetrieveHashmap.org;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface StoreDataAsTable {
    static void storeDataFromHashMap(String fileName, String[] headers, HashMap<String, HashMap<String, Double>> map) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + File.separator + fileName + ".csv"))) {
            // Construct the header
            StringBuilder header = new StringBuilder();
            header.append("GraphID");

            // Get the list of headers dynamically from the map's first entry
            List<String> headerKeys = Arrays.stream(headers).toList();
            for (String key : headerKeys) {
                header.append(",").append(key);
            }
            bufferedWriter.write(header.toString());
            bufferedWriter.newLine();  // Use newLine() instead of "\n" for cross-platform compatibility

            // Write the rows
            for (Map.Entry<String, HashMap<String, Double>> entry : map.entrySet()) {
                StringBuilder row = new StringBuilder();
                row.append(entry.getKey());  // Add GraphID first

                for (String key : headerKeys) {
                    row.append(",").append(entry.getValue().get(key));  // Maintain correct order by key
                }
                bufferedWriter.write(row.toString());
                bufferedWriter.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}