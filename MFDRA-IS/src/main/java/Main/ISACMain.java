package Main;

import StoreRetrieveHashmap.org.RetrieveResourceAllocationAndTopAllocationAsHashMap;
import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.Trainer;
import org.tribuo.clustering.ClusterID;
import org.tribuo.clustering.ClusteringFactory;
import org.tribuo.clustering.kmeans.KMeansTrainer;
import org.tribuo.data.columnar.FieldProcessor;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import org.tribuo.data.columnar.processors.field.IdentityProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.math.distance.DistanceType;
import org.tribuo.util.Util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BinaryOperator;

public class ISACMain {
    public static void main(String[] args) {
        // creat row processor to handle the header of the data
        var rowProcessor = GetRowProcessor();

        // read the training and testing dataset
        var trainData = new CSVDataSource<>(Paths.get(System.getProperty("user.dir"), "\\Train Random Walks for Clustering.csv"), rowProcessor, true);
        var testData = new CSVDataSource<>(Paths.get(System.getProperty("user.dir"), "\\Test Random Walks for Clustering.csv"), rowProcessor, true);

        // generate Tribuo form data portions of the read dataset
        var train = new MutableDataset<>(trainData);
        var test = new MutableDataset<>(testData);

        // show the training data features like size
        System.out.printf("train data size = %d, number of features = %d, number of labels = %d%n", train.size(),
                train.getFeatureMap().size(),
                train.getOutputInfo().size());

        // show the testing data features like size
        System.out.printf("test data size = %d, number of features = %d, number of labels = %d%n", test.size(),
                test.getFeatureMap().size(),
                test.getOutputInfo().size());

        // this for testbed 2
        var mapLabelToResourceAllocation = new HashMap<Integer, String>() {{
            put(1, "riskBasedDefence");
            put(3, "behavioralDefender");
            put(5, "betweennessCentralityWithInDegreeNodes");
            put(6, "betweennessCentralityWithAdjacentNodes");
            put(20, "katzCentralityWithMinCut");
            put(15, "degreeCentralityWithMarkovBlanket");
            put(2, "defenceInDepth");
            put(12, "harmonicCentralityWithMinCut");
            put(24, "PRCentralityWithInDegreeNodes");
            put(7, "betweennessCentralityWithMarkovBlanket");
            put(9, "harmonicCentralityWithInDegreeNodes");
            put(23, "TRCentralityWithMinCut");
            put(14, "degreeCentralityWithAdjacentNodes");
            put(18, "katzCentralityWithAdjacentNodes");
            put(8, "betweennessCentralityWithMinCut");
            put(17, "katzCentralityWithInDegreeNodes");
            put(21, "TRCentralityWithInDegreeNodes");
            put(26, "PRCentralityWithMarkovBlanket");
            put(13, "degreeCentralityWithInDegreeNodes");
            put(11, "harmonicCentralityWithMarkovBlanket");
            put(27, "PRCentralityWithMinCut");
            put(4, "minCut");
            put(22, "TRCentralityWithAdjacentNodes");
            put(16, "degreeCentralityWithMinCut");
            put(19, "katzCentralityWithMarkovBlanket");
            put(25, "PRCentralityWithAdjacentNodes");
            put(10, "harmonicCentralityWithAdjacentNodes");
        }};

        // K_means++ cluster
        var numberOfCentroid = 9;

        var clusterAlgorithm = ClusterAlgorithm.K_means;

        var trainer = switch (clusterAlgorithm) {
            case K_means_Plus_Plus -> new KMeansTrainer(numberOfCentroid,
                    200,
                    DistanceType.L1.getDistance(),
                    KMeansTrainer.Initialisation.PLUSPLUS,
                    Runtime.getRuntime().availableProcessors(),
                    Trainer.DEFAULT_SEED
            );
            case K_means -> new KMeansTrainer(numberOfCentroid,
                    200,
                    DistanceType.L1.getDistance(),
                    Runtime.getRuntime().availableProcessors(),
                    Trainer.DEFAULT_SEED
            );
        };

        // train the model
        var sTime = System.currentTimeMillis();
        var learner = trainer.train(train);
        var eTime = System.currentTimeMillis();
        System.out.println("\nTraining with " + numberOfCentroid + " clusters took : " + Util.formatDuration(sTime, eTime));

        // the centroids after training
        System.out.println("\nThe centroid of the algorithm after training on the given number of clusters");
        for (var center : learner.getCentroids()) {
            System.out.println(center);
        }

        // get the model predictions
        var predictions = learner.predict(train);
        System.out.println(predictions);

        // Map clusters to original data (e.g., labels or features)
        Map<Integer, List<String>> clusterToData = new HashMap<>();

        for (int i = 0; i < predictions.size(); i++) {
            int clusterId = predictions.get(i).getOutput().getID();
            String originalData = train.getExample(i).getOutput().toString();

            clusterToData
                    .computeIfAbsent(clusterId, ArrayList::new)
                    .add(originalData);
        }
        // Print the data in each cluster
        System.out.println("\nThe clusters of the training set with their referred labels : ");
        for (Map.Entry<Integer, List<String>> entry : clusterToData.entrySet()) {
            System.out.println("Cluster " + entry.getKey() + ": " + entry.getValue());
        }

        var performanceMatrix = RetrieveResourceAllocationAndTopAllocationAsHashMap
                .getPerformanceMatrixAsHashMap(Path.of(System.getProperty("user.dir") + "\\Allocation Results.csv"));

        var results = getResults(test, learner, mapLabelToResourceAllocation, clusterToData, performanceMatrix);
        eTime = System.currentTimeMillis();
        System.out.println("\nThe acc based on the hit at K is : " + results.get(0));
        System.out.println("The average performance is : " + results.get(1));
        System.out.println("The Hit_At_K accuracy is : " + results.get(2));
        System.out.println("The duration time is : " + (eTime - sTime));
    }

    static int findMostFrequent(List<String> nums) {
        return nums.stream()
                .map(Integer::parseInt)
                .reduce(BinaryOperator.maxBy(Comparator.comparingInt(o -> Collections.frequency(nums, o))))
                .orElseThrow(() -> new RuntimeException("No most frequent number found"));
    }


    /**
     * this method is used to construct Row Processor to process the data header
     * @return the constructed row processor
     */
    static RowProcessor<ClusterID> GetRowProcessor() {
        // creat an array list to process the graph id field
        var fieldProcessor = new ArrayList<FieldProcessor>();
        fieldProcessor.add(new IdentityProcessor("GraphID"));

        // creat hashmap to hold the fields name of the dataset
        var FeatureProcessor = new HashMap<String, FieldProcessor>();
        FeatureProcessor.put("AT.*", new DoubleFieldProcessor("AT.*"));

        // creat class label processor for the classes in the dataset to construct the label factory
        var ClassProcessor = new FieldResponseProcessor<>("Class", "nan", new ClusteringFactory());

        // return the row processor of the generated labels
        return new RowProcessor.Builder<ClusterID>().setRegexMappingProcessors(FeatureProcessor).setFieldProcessors(fieldProcessor).build(ClassProcessor);
    }

    static List<Double> getResults(MutableDataset<ClusterID> testPart, Model<ClusterID> trainedModel, Map<Integer, String> map, Map<Integer, List<String>> clusterToData, HashMap<String, List<String>> performanceMatrix) {
        // define the map of top resource allocation approaches
        var map0 = RetrieveResourceAllocationAndTopAllocationAsHashMap
                .getTopAllocationApproaches("Top Allocation Methods");

        var top_K = 5;
        var hit_At_K_acc = 0d;
        double accuracy = 0.0;
        double avgAllocationPerformance = 0d;
        for (Example<ClusterID> sample : testPart.getData()) {
            String[] instanceWords = sample.
                    toString().
                    replace("(", " ").
                    replace(",", " ").
                    split(" ");

            String graphID = " ";
            for (String word : instanceWords) {
                if (word.startsWith("GraphID")) {
                    graphID = word.split("@")[1];
                }
            }
            int prediction = trainedModel
                    .predict(sample)
                    .getOutput()
                    .getID();
            
            int groundTruth = sample
                    .getOutput()
                    .getID();
            
            if (clusterToData.get(prediction) != null) { // this if-statement is not necessary if the data is balanced
                if (findMostFrequent(clusterToData.get(prediction)) == groundTruth) {
                    accuracy++;
                }
            }
            if (clusterToData.get(prediction) == null) { // this if statement is not necessary if the data is balanced and the clusters well describe the data points in the test sets
                continue;
            }
            var label = map.get(findMostFrequent(clusterToData.get(prediction)));
            var index = performanceMatrix
                    .get("Allocation")
                    .stream()
                    .map(String::toUpperCase)
                    .toList()
                    .indexOf(label.toUpperCase());
            
            var costRelativeReduction = performanceMatrix
                    .get(graphID)
                    .get(index);
            
            avgAllocationPerformance += Double.parseDouble(costRelativeReduction);
            ArrayList<String> top_K_GroundTruths = new ArrayList<>();
            for (int i = 0; i < top_K; i++) {
                top_K_GroundTruths.add(map0.get(graphID).get(i).toUpperCase(Locale.ROOT));
            }
            if (top_K_GroundTruths.contains(label.toUpperCase())) {
                hit_At_K_acc++;
            }
        }
        return List.of(accuracy / testPart.getData().size(),
                avgAllocationPerformance / testPart.getData().size(),
                hit_At_K_acc / testPart.getData().size());
    }

    enum ClusterAlgorithm {
        K_means, K_means_Plus_Plus
    }
}
