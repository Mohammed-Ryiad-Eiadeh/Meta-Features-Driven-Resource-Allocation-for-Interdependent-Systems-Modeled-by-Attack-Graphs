package Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SplitData {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(System.getProperty("user.dir") + "\\Embeddings Random Walks.csv"));
        String header = lines.remove(0);
        Collections.shuffle(lines, new Random(12345));
        List<String> train = new ArrayList<>(lines.subList(0, (int) (lines.size() * 0.60)));
        List<String> test = new ArrayList<>(lines.subList((int) (lines.size() * 0.60), lines.size()));
        train.add(0, header);
        test.add(0, header);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\Train Random Walks.csv"))) {
            for (String line : train) {
                bufferedWriter.write(line + "\n");
            }
            bufferedWriter.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "\\Test Random Walks.csv"))) {
            for (String line : test) {
                bufferedWriter.write(line + "\n");
            }
            bufferedWriter.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

