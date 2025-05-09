package lt.ziniumanas.service.adminservice;

import lt.ziniumanas.nlp.TextVectorizer;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
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

    @Value("${model.save.path:models/ArticleCategorizationAImodel.zip}")
    private String modelSavePath;

    @Value("${paragraph.vectors.save.path:models/paragraph_vectors.zip}")
    private String paragraphVectorsSavePath;

    @Value("${model.epochs:10}")
    private int epochs;

    @Value("${model.hidden.layer.size:100}")
    private int hiddenLayerSize;

    @Value("${model.batch.size:32}")
    private int batchSize;

    @Value("${paragraph.vectors.min.word.frequency:5}")
    private int minWordFrequency;

    @Value("${paragraph.vectors.layer.size:100}")
    private int pvLayerSize;

    @Value("${paragraph.vectors.epochs:10}")
    private int pvEpochs;

    @Value("${paragraph.vectors.learning.rate:0.025}")
    private double pvLearningRate;

    @Value("${paragraph.vectors.window.size:5}")
    private int pvWindowSize;

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
        log.info("Neuroninis tinklas sukurtas: įvesties dydis {}, klasės {}", inputSize, numClasses);
        return model;
    }

    public void trainModel(List<String> texts, List<String> labels) {
        if (texts.isEmpty() || labels.isEmpty() || texts.size() != labels.size()) {
            throw new IllegalArgumentException("Tekstai ir etiketės turi būti netušti ir vienodo dydžio");
        }

        // Loguojame įvesties duomenis
        log.info("Gauta {} tekstų ir {} etikečių. Pirmasis tekstas: '{}', pirmoji etiketė: '{}'",
                texts.size(), labels.size(), texts.get(0), labels.get(0));

        for (String text : texts) {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Tekstas negali būti tuščias arba null");
            }
        }

        log.info("Treniruojama su {} tekstais ir {} unikaliomis etiketėmis", texts.size(), new HashSet<>(labels).size());

        // Sukuriame tekstų ir etikečių žemėlapį
        Map<String, String> textsWithLabels = new HashMap<>();
        for (int i = 0; i < texts.size(); i++) {
            textsWithLabels.put(labels.get(i) + "_" + i, texts.get(i));
        }

        // Tikriname, ar ParagraphVectors modelis jau egzistuoja
        ParagraphVectors paragraphVectors;
        File pvFile = new File(paragraphVectorsSavePath);
        if (pvFile.exists()) {
            log.info("Įkeliamas egzistuojantis ParagraphVectors modelis iš {}", paragraphVectorsSavePath);
            paragraphVectors = TextVectorizer.loadModel(paragraphVectorsSavePath);
        } else {
            log.info("Treniruojamas naujas ParagraphVectors modelis");
            try {
                paragraphVectors = TextVectorizer.trainModel(textsWithLabels, paragraphVectorsSavePath,
                        minWordFrequency, pvLayerSize, pvEpochs, pvLearningRate, pvWindowSize);
            } catch (Exception e) {
                log.error("Nepavyko treniruoti ParagraphVectors modelio: {}", e.getMessage());
                throw new RuntimeException("ParagraphVectors treniravimo klaida", e);
            }
        }

        // Vektorizuojame tekstus
        INDArray features;
        try {
            features = TextVectorizer.vectorize(texts, paragraphVectors);
        } catch (Exception e) {
            log.error("Nepavyko vektorizuoti tekstų: {}", e.getMessage());
            throw new RuntimeException("Tekstų vektorizacijos klaida", e);
        }

        // Normalizuojame požymius
        DataNormalization normalizer = new NormalizerMinMaxScaler();
        normalizer.fit(new DataSet(features, features));
        normalizer.transform(features);

        // Paverčiame etiketes į one-hot formatą
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

        // Sukuriame pilną duomenų rinkinį
        DataSet fullDataSet = new DataSet(features, target);

        // Treniruojame modelį per epochas
        long startTime = System.currentTimeMillis();
        for (int epoch = 0; epoch < epochs; epoch++) {
            // Padalijame duomenis į mini-partijas rankiniu būdu
            for (int i = 0; i < fullDataSet.numExamples(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, fullDataSet.numExamples());
                INDArray batchFeatures = features.getRows(i, endIndex);
                INDArray batchLabels = target.getRows(i, endIndex);
                DataSet batch = new DataSet(batchFeatures, batchLabels);
                try {
                    model.fit(batch);
                } catch (Exception e) {
                    log.error("Klaida treniruojant mini-partiją {}-{} epochoje {}: {}", i, endIndex, epoch + 1, e.getMessage());
                    throw new RuntimeException("Treniravimo klaida mini-partijoje", e);
                }
            }
            log.info("Baigta epocha {}/{}", epoch + 1, epochs);
        }
        log.info("Neuroninio tinklo treniravimas užbaigtas per {} ms", System.currentTimeMillis() - startTime);

        // Išsaugome modelį
        try {
            File modelFile = new File(modelSavePath);
            if (!modelFile.getParentFile().exists() && !modelFile.getParentFile().mkdirs()) {
                throw new IOException("Nepavyko sukurti katalogo: " + modelFile.getParentFile().getAbsolutePath());
            }
            ModelSerializer.writeModel(model, modelFile, true);
            log.info("Neuroninio tinklo modelis išsaugotas į {}", modelFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Nepavyko išsaugoti neuroninio tinklo modelio: " + e.getMessage(), e);
        }
    }
}