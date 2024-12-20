package Main;

import StoreRetrieveHashmap.org.RetrieveResourceAllocationAndTopAllocationAsHashMap;
import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.Trainer;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.classification.dtree.CARTClassificationTrainer;
import org.tribuo.classification.evaluation.LabelEvaluation;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.classification.liblinear.LibLinearClassificationTrainer;
import org.tribuo.classification.mnb.MultinomialNaiveBayesTrainer;
import org.tribuo.classification.sgd.fm.FMClassificationTrainer;
import org.tribuo.classification.sgd.kernel.KernelSVMTrainer;
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer;
import org.tribuo.classification.sgd.objectives.Hinge;
import org.tribuo.classification.xgboost.XGBoostClassificationTrainer;
import org.tribuo.common.nearest.KNNClassifierOptions;
import org.tribuo.data.columnar.FieldProcessor;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import org.tribuo.data.columnar.processors.field.IdentityProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.interop.tensorflow.DenseFeatureConverter;
import org.tribuo.interop.tensorflow.GradientOptimiser;
import org.tribuo.interop.tensorflow.ImageConverter;
import org.tribuo.interop.tensorflow.LabelConverter;
import org.tribuo.interop.tensorflow.TensorFlowTrainer;
import org.tribuo.interop.tensorflow.example.CNNExamples;
import org.tribuo.interop.tensorflow.example.MLPExamples;
import org.tribuo.math.distance.DistanceType;
import org.tribuo.math.kernel.Linear;
import org.tribuo.math.optimisers.AdaGradRDA;
import org.tribuo.transform.TransformTrainer;
import org.tribuo.transform.TransformationMap;
import org.tribuo.transform.transformations.LinearScalingTransformation;
import org.tribuo.util.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClassificationMain implements RetrieveResourceAllocationAndTopAllocationAsHashMap {
    public static void main(String[] args) throws IOException {
        // creat row processor to handle the header of the data
        var rowProcessor = GetRowProcessor();

        // read the training and testing dataset
        var trainData = new CSVDataSource<>(Paths.get(System.getProperty("user.dir"), "\\Train Random Walks.csv"), rowProcessor, true);
        var testData = new CSVDataSource<>(Paths.get(System.getProperty("user.dir"), "\\Test Random Walks.csv"), rowProcessor, true);

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

        // define label evaluation for evaluation purposes
        LabelEvaluation Evaluator;

        // define the map of top resource allocation approaches
        var map = RetrieveResourceAllocationAndTopAllocationAsHashMap
                .getTopAllocationApproaches("Top Allocation Methods");

        //
        var performanceMatrix = RetrieveResourceAllocationAndTopAllocationAsHashMap
                .getPerformanceMatrixAsHashMap(Path.of(System.getProperty("user.dir") + "\\Allocation Results.csv"));

        // define the k value involved in hit-at-k
        var k = 10;

        // define two variable to store the start and end date
        var sTime = 0L;
        var eTime = 0L;

        // define, train, and test LeNet-5
        var inputName = "LeNet5";
        var LeNet_5 = CNNExamples.buildLeNetGraph(inputName, 32, 255, train.getOutputs().size());
        var optimizer = GradientOptimiser.ADAGRAD;
        var optimizerParameters = Map.of("learningRate", 0.01f, "initialAccumulatorValue", 0.01f);
        var TF_Trainer = new TensorFlowTrainer<>(LeNet_5.graphDef,
                LeNet_5.outputName,
                optimizer,
                optimizerParameters,
                new ImageConverter(inputName, 32, 32, 1),
                new LabelConverter(),
                16,
                10,
                16,
                -1);
        var TF_Learner = TF_Trainer.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(TF_Learner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------LeNet-5 Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, TF_Learner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, TF_Learner, map, performanceMatrix, k).get(1));

        var inName = "MLP";
        var mlpGraph = MLPExamples.buildMLPGraph(inName, train.getFeatureMap().size(), new int[] {300, 200, 30}, train.getOutputs().size());
        var mlpOptimizer = GradientOptimiser.ADAGRAD;
        var mlpOptimizerParameters = Map.of("learningRate", 0.01f, "initialAccumulatorValue", 0.01f);
        var TF_Trainer_MLP = new TensorFlowTrainer<>(mlpGraph.graphDef,
                mlpGraph.outputName,
                mlpOptimizer,
                mlpOptimizerParameters,
                new DenseFeatureConverter(inName),
                new LabelConverter(),
                25,
                100,
                25,
                -1);
        var TF_MLP_Learner = TF_Trainer_MLP.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(TF_MLP_Learner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------MLP Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, TF_MLP_Learner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, TF_MLP_Learner, map, performanceMatrix, k).get(1));

        // define, train, and test factorization machine classifier
        var FactorizationMachine = new FMClassificationTrainer(new Hinge(), new AdaGradRDA(0.1, 0.8), 1, 1000, 1, Trainer.DEFAULT_SEED, 10, 0.2);
        var FMLearner = FactorizationMachine.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(FMLearner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------Factorization Machine Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, FMLearner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, FMLearner, map, performanceMatrix, k).get(1));

                // define, train, and test SVM classifier
        var SVMTrainer = new KernelSVMTrainer(new Linear(), 0.1, 1, Trainer.DEFAULT_SEED);
        var SVMLearner = SVMTrainer.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(SVMLearner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------Support Vector Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, SVMLearner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, SVMLearner, map, performanceMatrix, k).get(1));

        // define, train, and test LR classifier
        var LRTrainer = new LogisticRegressionTrainer();
        var LRLearner = LRTrainer.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(LRLearner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------Logistic Regression Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, LRLearner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, LRLearner, map, performanceMatrix, k).get(1));

        // define, train, and test KNN classifier
        var KNNTrainer = new KNNClassifierOptions();
        KNNTrainer.knnK = 3;
        KNNTrainer.distType = DistanceType.COSINE;
        var KNNLearner = KNNTrainer.getTrainer().train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(KNNLearner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------KNN Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, KNNLearner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, KNNLearner, map, performanceMatrix, k).get(1));

                // define, train, and test CART classifier
        var CartTrainer = new CARTClassificationTrainer();
        var CartLearner = CartTrainer.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(CartLearner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------CART Tree Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, CartLearner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, CartLearner, map, performanceMatrix, k).get(1));

        // define, train, and test LibLinear classifier
        var LibLinTrainer = new LibLinearClassificationTrainer();
        var LibLinLearner = LibLinTrainer.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(LibLinLearner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------LibLinear Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, LibLinLearner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, LibLinLearner, map, performanceMatrix, k).get(1));

                // define, train, and test XGBoost classifier
        var XGBTrainer = new XGBoostClassificationTrainer(10);
        var XGBLearner = XGBTrainer.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(XGBLearner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------XGBoost Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, XGBLearner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, XGBLearner, map, performanceMatrix, k).get(1));

        // define, train, and test MNB classifier
        var MNBTrainer = new MultinomialNaiveBayesTrainer();
        var transformation = new TransformationMap(List.of(new LinearScalingTransformation(0, 1)));
        var transformedTrainer = new TransformTrainer<>(MNBTrainer, transformation);
        var MNBLearner = transformedTrainer.train(train);
        sTime = System.currentTimeMillis();
        Evaluator = new LabelEvaluator().evaluate(MNBLearner, test);
        eTime = System.currentTimeMillis();
        System.out.println("----------------MNB Performance\n" + Evaluator +
                "\n----------------Confusion Matrix \n" + Evaluator.getConfusionMatrix() +
                "\n" + "Duration Time is : " + Util.formatDuration(sTime, eTime) + "\n\n");
        System.out.println("The acc based on the hit at K is : " + hitAt_K(test, MNBLearner, map, performanceMatrix, k).get(0));
        System.out.println("The average performance is : " + hitAt_K(test, MNBLearner, map, performanceMatrix, k).get(1));
    }

    /**
     * this method is used to construct Row Processor to process the data header
     * @return the constructed row processor
     */
    static RowProcessor<Label> GetRowProcessor() {
        // creat an array list to process the graph id field
        var fieldProcessor = new ArrayList<FieldProcessor>();
        fieldProcessor.add(new IdentityProcessor("GraphID"));

        // creat hashmap to hold the fields name of the dataset
        var FeatureProcessor = new HashMap<String, FieldProcessor>();
        FeatureProcessor.put("AT.*", new DoubleFieldProcessor("AT.*"));

        // creat class label processor for the classes in the dataset to construct the label factory
        var ClassProcessor = new FieldResponseProcessor<>("Class", "nan", new LabelFactory());

        // return the row processor of the generated labels
        return new RowProcessor.Builder<Label>().setRegexMappingProcessors(FeatureProcessor).setFieldProcessors(fieldProcessor).build(ClassProcessor);
    }

    static List<Double> hitAt_K(MutableDataset<Label> testPart, Model<Label> trainedModel, HashMap<String, ArrayList<String>> map, HashMap<String, List<String>> performanceMatrix, int top_K) {
        if (map.isEmpty()) {
            throw new IllegalArgumentException("The map holding the graph with their top-allocation approaches is empty");
        }
        double accuracy = 0.0;
        double avgAllocationPerformance = 0d;
        for (Example<Label> sample : testPart.getData()) {
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
            String prediction = trainedModel.predict(sample).getOutput().getLabel();
            ArrayList<String> top_K_GroundTruths = new ArrayList<>();
            for (int i = 0; i < top_K; i++) {
                top_K_GroundTruths.add(map.get(graphID).get(i).toUpperCase(Locale.ROOT));
            }
            if (top_K_GroundTruths.contains(prediction.toUpperCase(Locale.ROOT))) {
                accuracy++;
            }
            var index = performanceMatrix.get("Allocation").stream().map(String::toUpperCase).toList().indexOf(prediction.toUpperCase());
            var costRelativeReduction = performanceMatrix.get(graphID).get(index);
            avgAllocationPerformance += Double.parseDouble(costRelativeReduction);
        }
        return List.of(accuracy / testPart.getData().size(), avgAllocationPerformance / testPart.getData().size());
    }
}

