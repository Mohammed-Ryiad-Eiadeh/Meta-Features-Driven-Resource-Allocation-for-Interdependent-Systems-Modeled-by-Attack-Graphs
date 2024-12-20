package StoreHashmap.org;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public interface StoreHashmap {
    static void storeGraphEmbeddings(HashMap<String, double[]> hashMap, String fileName) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\" + fileName +".csv"))) {
            // Write the header
            StringBuilder header = new StringBuilder();
            header.append("GraphID");
            for (int i = 0; i < hashMap.values().iterator().next().length; i++) {
                header.append(",ATT").append(i + 1);
            }
            bufferedWriter.write(header.toString());
            bufferedWriter.newLine(); // Write a new line after the header

            // Write rows
            for (Map.Entry<String, double[]> entry : hashMap.entrySet()) {
                StringBuilder row = new StringBuilder();
                row.append(entry.getKey()); // Write GraphID

                // Append values with commas, avoiding trailing commas
                for (int i = 0; i < entry.getValue().length; i++) {
                    row.append(",").append(entry.getValue()[i]);
                }
                bufferedWriter.write(row.toString());
                bufferedWriter.newLine(); // Write a new line for each row
            }

            bufferedWriter.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void storeHashMapHashmapEntries(HashMap<String, HashMap<String, Double>> map, String fileName) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\" + fileName +".csv"))) {
            for (String key : map.keySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(key).append(",");
                for (Map.Entry<String, Double> entry : map.get(key).entrySet()) {
                    stringBuilder.append(entry.getKey()).append(",").append(entry.getValue()).append(",");
                }
                bufferedWriter.write(stringBuilder + "\n");
            }
            bufferedWriter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
