package lt.ziniumanas.service.adminservice;

import lt.ziniumanas.nlp.TextVectorizer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
@Service
public class ArticleCategorizationAIModelTrainingServicebyAdmin {
    private static final Logger log = LoggerFactory.getLogger(ArticleCategorizationAIModelTrainingServicebyAdmin.class);

    @Value("${model.save.path:model.zip}")
    private String modelSavePath;

    @Value("${paragraph.vectors.save.path:paragraph_vectors.zip}")
    private String paragraphVectorsSavePath;

    @Value("${model.epochs:10}")
    private int epochs;

    @Value("${model.hidden.layer.size:100}")
    private int hiddenLayerSize;

    @Value("${model.batch.size:32}")
    private int batchSize;

    public MultiLayerNetwork createModel(int inputSize, int numClasses) {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new DenseLayer.Builder().nIn(inputSize).nOut(hiddenLayerSize).activation(Activation.RELU).build())
                .layer(new OutputLayer.Builder().nIn(hiddenLayerSize).nOut(numClasses).activation(Activation.SOFTMAX).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
        log.info("Neuroninis tinklas sukurtas su įvesties dydžiu {} ir {} klasėmis", inputSize, numClasses);
        return model;
    }

    public void trainModel(List<String> texts, List<String> labels) {
        if (texts.isEmpty() || labels.isEmpty() || texts.size() != labels.size()) {
            throw new IllegalArgumentException("Įvesties duomenys neteisingi: tekstai ir etiketės turi būti netušti ir vienodo dydžio");
        }

        // Sukuriame Map<String, String> iš tekstų ir etikečių
        Map<String, String> textsWithLabels = new HashMap<>();
        for (int i = 0; i < texts.size(); i++) {
            textsWithLabels.put(labels.get(i) + "_" + i, texts.get(i)); // Unikalus raktas
        }

        // Apmokome ParagraphVectors modelį
        ParagraphVectors paragraphVectors = TextVectorizer.trainModel(textsWithLabels, paragraphVectorsSavePath);

        // Vektorizuojame tekstus
        INDArray features = TextVectorizer.vectorize(texts, paragraphVectors);

        // Paverčiame kategorijas į vieno karšto kodo etiketes
        List<String> uniqueLabels = new ArrayList<>(new HashSet<>(labels));
        Map<String, Integer> labelMap = new HashMap<>();
        for (int i = 0; i < uniqueLabels.size(); i++) {
            labelMap.put(uniqueLabels.get(i), i);
        }

        INDArray target = Nd4j.zeros(labels.size(), uniqueLabels.size());
        for (int i = 0; i < labels.size(); i++) {
            target.putScalar(i, labelMap.get(labels.get(i)), 1.0);
        }

        // Sukuriame modelį
        MultiLayerNetwork model = createModel(features.columns(), uniqueLabels.size());

        // Rankiniu būdu apdorojame mini-partijas
        DataSet fullDataSet = new DataSet(features, target);
        List<DataSet> miniBatches = fullDataSet.batchBy(batchSize);

        // Apmokome modelį
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (DataSet miniBatch : miniBatches) {
                model.fit(miniBatch);
            }
            log.info("Baigta epocha {}", epoch + 1);
        }

        // Išsaugome modelį
        try {
            File modelFile = new File(modelSavePath);
            modelFile.getParentFile().mkdirs();
            ModelSerializer.writeModel(model, modelFile, true);
            log.info("Neuroninio tinklo modelis išsaugotas į {}", modelFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Nepavyko išsaugoti neuroninio tinklo modelio", e);
        }
    }
}