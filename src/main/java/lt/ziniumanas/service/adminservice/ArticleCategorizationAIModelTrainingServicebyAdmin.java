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
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
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
    private static final List<String> CLASSES = Arrays.asList(
            "Sportas", "Ekonomika", "Politika", "Kultūra", "Technologijos", "Sveikata",
            "Mokslas", "Istorija", "Pasaulyje", "Lietuvoje", "Vaikams", "Muzika"
    );

    @Value("${model.save.path:models/ArticleCategorizationAImodel.zip}")
    private String modelSavePath;

    @Value("${paragraph.vectors.save.path:models/Article_categorization_paragraph_vectors.zip}")
    private String paragraphVectorsSavePath;

    @Value("${model.epochs:30}")
    private int epochs;

    @Value("${model.hidden.layer.size:300}")
    private int hiddenLayerSize;

    @Value("${model.batch.size:16}")
    private int batchSize;

    @Value("${paragraph.vectors.min.word.frequency:1}")
    private int minWordFrequency;

    @Value("${paragraph.vectors.layer.size:300}")
    private int pvLayerSize;

    @Value("${paragraph.vectors.epochs:30}")
    private int pvEpochs;

    @Value("${paragraph.vectors.learning.rate:0.025}")
    private double pvLearningRate;

    @Value("${paragraph.vectors.window.size:5}")
    private int pvWindowSize;

    public MultiLayerNetwork createModel(int inputSize, int numClasses) {
        if (numClasses != CLASSES.size()) {
            log.warn("Kategorijų skaičius ({}) neatitinka laukiamo ({}). Tikrinami treniravimo duomenys.", numClasses, CLASSES.size());
        }

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .l2(1e-4)
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(inputSize)
                        .nOut(hiddenLayerSize)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(hiddenLayerSize)
                        .nOut(hiddenLayerSize / 2)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(hiddenLayerSize / 2)
                        .nOut(numClasses)
                        .activation(Activation.SOFTMAX)
                        .build())
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

        log.info("Gauta {} tekstų ir {} etikečių. Pirmasis tekstas: '{}', pirmoji etiketė: '{}'",
                texts.size(), labels.size(), texts.get(0), labels.get(0));

        // Pašaliname tuščius tekstus
        List<String> validTexts = new ArrayList<>();
        List<String> validLabels = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i) != null && !texts.get(i).trim().isEmpty()) {
                validTexts.add(texts.get(i));
                validLabels.add(labels.get(i));
            } else {
                log.warn("Praleistas tuščias tekstas indeksu {}", i);
            }
        }
        if (validTexts.isEmpty()) {
            throw new IllegalArgumentException("Nėra galiojančių tekstų treniravimui");
        }
        log.info("Galiojančių tekstų: {}, etikečių: {}", validTexts.size(), validLabels.size());

        // Tikriname, ar visos kategorijos yra iš CLASSES
        Set<String> uniqueLabels = new HashSet<>(validLabels);
        for (String label : uniqueLabels) {
            if (!CLASSES.contains(label)) {
                log.warn("Nerasta etiketė '{}' tarp galimų kategorijų: {}", label, CLASSES);
            }
        }
        if (uniqueLabels.size() < CLASSES.size()) {
            log.warn("Treniravimo duomenys apima tik {}/{} kategorijų", uniqueLabels.size(), CLASSES.size());
        }

        // Sukuriame tekstų ir etikečių žemėlapį
        Map<String, String> textsWithLabels = new HashMap<>();
        for (int i = 0; i < validTexts.size(); i++) {
            textsWithLabels.put(validLabels.get(i) + "_" + i, validTexts.get(i));
        }

        // Treniruojame arba įkeliame ParagraphVectors
        ParagraphVectors paragraphVectors;
        File pvFile = new File(paragraphVectorsSavePath);
        if (pvFile.exists()) {
            log.info("Įkeliamas egzistuojantis ParagraphVectors modelis iš {}", paragraphVectorsSavePath);
            paragraphVectors = TextVectorizer.loadModel(paragraphVectorsSavePath);
            // Treniruojame iš naujo su visais tekstais, kad atnaujintume modelį
            log.info("Atnaujinamas ParagraphVectors modelis su {} tekstais", validTexts.size());
            try {
                paragraphVectors = TextVectorizer.trainModel(textsWithLabels, paragraphVectorsSavePath,
                        minWordFrequency, pvLayerSize, pvEpochs, pvLearningRate, pvWindowSize);
            } catch (Exception e) {
                log.error("Nepavyko atnaujinti ParagraphVectors modelio: {}", e.getMessage());
                throw new RuntimeException("ParagraphVectors treniravimo klaida", e);
            }
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
            features = TextVectorizer.vectorize(validTexts, paragraphVectors);
            log.info("Vektorizacijos rezultatas: {} eilučių, {} stulpelių", features.rows(), features.columns());
        } catch (Exception e) {
            log.error("Nepavyko vektorizuoti tekstų: {}", e.getMessage());
            throw new RuntimeException("Tekstų vektorizacijos klaida", e);
        }

        // Normalizuojame požymius
        DataNormalization normalizer = new NormalizerMinMaxScaler();
        normalizer.fit(new DataSet(features, features));
        normalizer.transform(features);

        // Paverčiame etiketes į one-hot formatą
        Map<String, Integer> labelMap = new HashMap<>();
        for (int i = 0; i < CLASSES.size(); i++) {
            labelMap.put(CLASSES.get(i), i);
        }

        INDArray target = Nd4j.zeros(validTexts.size(), CLASSES.size());
        for (int i = 0; i < validTexts.size(); i++) {
            String label = validLabels.get(i);
            if (labelMap.containsKey(label)) {
                target.putScalar(i, labelMap.get(label), 1.0);
            } else {
                log.warn("Nepaisoma nežinoma etiketė: {}", label);
            }
        }

        // Patikriname dimensijas
        log.info("Features: {} eilučių, {} stulpelių; Target: {} eilučių, {} stulpelių",
                features.rows(), features.columns(), target.rows(), target.columns());
        if (features.rows() != target.rows()) {
            throw new IllegalStateException("Features ir Target eilučių skaičius nesutampa: " +
                    features.rows() + " vs " + target.rows());
        }

        // Įkeliame esamą modelį arba kuriame naują
        MultiLayerNetwork model;
        File modelFile = new File(modelSavePath);
        if (modelFile.exists()) {
            log.info("Įkeliamas egzistuojantis neuroninis tinklas iš {}", modelSavePath);
            try {
                model = ModelSerializer.restoreMultiLayerNetwork(modelFile);
                model.setListeners(new ScoreIterationListener(100));
            } catch (IOException e) {
                log.warn("Nepavyko įkelti esamo modelio, kuriamas naujas: {}", e.getMessage());
                model = createModel(features.columns(), CLASSES.size());
            }
        } else {
            log.info("Treniruojamas naujas neuroninis tinklas");
            model = createModel(features.columns(), CLASSES.size());
        }

        // Sukuriame DataSet
        DataSet fullDataSet = new DataSet(features, target);

        // Treniruojame modelį per epochas
        long startTime = System.currentTimeMillis();
        for (int epoch = 0; epoch < epochs; epoch++) {
            int numExamples = fullDataSet.numExamples();
            for (int i = 0; i < numExamples; i += batchSize) {
                int endIndex = Math.min(i + batchSize, numExamples);
                INDArray batchFeatures = features.get(NDArrayIndex.interval(i, endIndex), NDArrayIndex.all());
                INDArray batchLabels = target.get(NDArrayIndex.interval(i, endIndex), NDArrayIndex.all());
                DataSet batch = new DataSet(batchFeatures, batchLabels);
                try {
                    model.fit(batch);
                    log.debug("Treniruota mini-partija {}-{}", i, endIndex);
                } catch (Exception e) {
                    log.error("Klaida treniruojant mini-partiją {}-{} epochoje {}: {}", i, endIndex, epoch + 1, e.getMessage(), e);
                    throw new RuntimeException("Treniravimo klaida mini-partijoje", e);
                }
            }
            log.info("Baigta epocha {}/{}", epoch + 1, epochs);
        }
        log.info("Neuroninio tinklo treniravimas užbaigtas per {} ms", System.currentTimeMillis() - startTime);

        // Išsaugome modelį
        try {
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