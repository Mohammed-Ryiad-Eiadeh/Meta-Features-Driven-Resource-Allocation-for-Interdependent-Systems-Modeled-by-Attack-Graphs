package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class GenerateRandomDatasets {
    public static void main(String[] args) throws IOException {
        var numOfAttackGraphs = 10;
        for (var graph = 0; graph < numOfAttackGraphs; graph++) {
            var rand = 20 + new Random().nextInt(51);
            var matrix = new int[rand][rand];
            var numOfEdges = 0;
            var lines = new ArrayList<String>();
            for (var i = 0; i < matrix.length; i++) {
                for (var j = 0; j < matrix.length; j++) {
                    if (i != j) {
                        matrix[i][j] = new Random().nextDouble() > 0.9 ? 1 : 0;
                    }
                    if (matrix[i][j] > 0) {
                        numOfEdges++;
                    }
                }
            }
            lines.add(matrix.length + " <--# of nodes, " + numOfEdges + " edges");
            for (var i = 0; i < matrix.length; i++) {
                for (var j = 0; j < matrix.length; j++) {
                    if (matrix[i][j] > 0) {
                        lines.add((i + 1) + " " + (j + 1));
                        numOfEdges++;
                    }
                }
            }
            var assetLosses = new int[matrix.length];
            for (var nod = 0; nod < matrix.length; nod++) {
                if (new Random().nextDouble() > 0.8) {
                    assetLosses[nod] = new Random().nextInt(101);
                }
            }
            StringBuilder stringBuilder = new StringBuilder("Loss");
            for (var loss : assetLosses) {
                stringBuilder.append(" ").append(loss).append(" ");
            }
            lines.add(stringBuilder.toString());
            Files.write(Paths.get(System.getProperty("user.dir") + "\\myGraph"+ graph + ".txt"), lines);
        }
    }
}