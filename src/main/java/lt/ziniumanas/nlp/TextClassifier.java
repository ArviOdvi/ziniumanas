package lt.ziniumanas.nlp;

import ai.djl.Application;
import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Batch;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.TranslateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.Arrays;
import java.util.List;

@Component
public class TextClassifier {
    private static final Logger logger = LoggerFactory.getLogger(TextClassifier.class);

    private ZooModel<String, Classifications> model;
    private Predictor<String, Classifications> predictor;
    private final String modelName = "distilbert-base-uncased";
    private boolean isModelLoaded = false;
    private List<String> uniqueLabels;

    private static final List<String> DEFAULT_CLASSES = Arrays.asList(
            "Sportas", "Ekonomika", "Politika", "Kultūra", "Technologijos", "Sveikata",
            "Mokslas", "Istorija", "Pasaulyje", "Lietuvoje", "Vaikams", "Muzika", "Maistas"
    );

    public TextClassifier() {
        loadModel();
    }

    private void loadModel() {
        Criteria<String, Classifications> criteria = Criteria.builder()
                .optApplication(Application.NLP.TEXT_CLASSIFICATION)
                .setTypes(String.class, Classifications.class)
                .optArtifactId(modelName)
                .build();
        try {
            model = ModelZoo.loadModel(criteria);
            predictor = model.newPredictor(new TextClassificationTranslator(DEFAULT_CLASSES));
            isModelLoaded = true;
            logger.info("DJL modelis '{}' įkeltas sėkmingai", modelName);
        } catch (IOException | ModelException e) {
            logger.error("Nepavyko įkelti DJL modelio '{}': {}", modelName, e.getMessage(), e);
        }
    }

    public void trainModel(Map<String, String> textsWithLabels, String modelPath, int epochs, float learningRate) {
        if (textsWithLabels == null || textsWithLabels.isEmpty()) {
            logger.error("Treniravimo duomenys tušti arba null");
            throw new IllegalArgumentException("Treniravimo duomenys negali būti tušti");
        }

        try {
            // Sukurti Dataset
            List<String> texts = new ArrayList<>(textsWithLabels.keySet());
            List<String> labels = new ArrayList<>(textsWithLabels.values());
            uniqueLabels = new ArrayList<>(new HashSet<>(labels));
            int batchSize = 8; // arba koks tau tinkamas dydis
            TextClassificationDataset dataset = new TextClassificationDataset(texts, labels, uniqueLabels, batchSize);

            // Sukonfigūruoti treniravimo nustatymus
            DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                    .optOptimizer(Optimizer.adam().optLearningRateTracker(Tracker.fixed(learningRate)).build())
                    .addTrainingListeners(TrainingListener.Defaults.logging());

            Model rawModel = Model.newInstance("custom_bert");
            Trainer trainer = rawModel.newTrainer(config);
            trainer.initialize(new Shape(1, 512));; // ar tau reikia 512? Tik jei tokenizer taip nustato

            logger.info("Pradedamas modelio treniravimas su {} epochomis", epochs);
            for (int epoch = 0; epoch < epochs; epoch++) {
                for (Batch batch : dataset.getData(rawModel.getNDManager())) {
                    trainer.step(batch);  // Atliekame treniravimo žingsnį su dabartiniu paketu
                    batch.close();
                }
                logger.info("Baigta epocha {}/{}", epoch + 1, epochs);
            }

            // Išsaugoti modelį
            rawModel.save(Paths.get(modelPath), "custom_bert");
            trainer.close();
            logger.info("Modelis išsaugotas kelyje: {}", modelPath);

            // Uždaryti trenerį
            trainer.close();

            // Perkrauti modelį su naujomis etiketėmis
            close();
            loadModel(modelPath);
        } catch (Exception e) {
            logger.error("Klaida treniruojant modelį: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko treniruoti modelio", e);
        }
    }

    public String classify(List<String> texts) {
        if (!isModelLoaded) {
            logger.warn("DJL modelis dar neįkeltas, bandoma įkelti...");
            loadModel();
            if (!isModelLoaded) {
                return "Nežinoma";
            }
        }

        if (texts == null || texts.isEmpty()) {
            return "Nežinoma";
        }

        try {
            Classifications result = predictor.predict(texts.get(0));
            if (result != null && !result.getClassNames().isEmpty()) {
                return result.best().getClassName();
            } else {
                return "Nežinoma";
            }
        } catch (TranslateException e) {
            logger.error("Klaida klasifikuojant tekstą: {}", e.getMessage(), e);
            return "Nežinoma";
        }
    }

    public void loadModel(String modelPath) {
        try {
            Model rawModel = Model.newInstance("custom_bert");
            rawModel.load(Paths.get(modelPath));
            model = new ZooModel<>(rawModel, new TextClassificationTranslator(uniqueLabels != null ? uniqueLabels : DEFAULT_CLASSES));
            predictor = model.newPredictor();
            isModelLoaded = true;
            logger.info("Modelis įkeltas iš kelio: {}", modelPath);
        } catch (Exception e) {
            logger.error("Nepavyko įkelti modelio iš '{}': {}", modelPath, e.getMessage(), e);
            loadModel(); // Grįžtame prie numatytojo modelio
        }
    }

    @PreDestroy
    public void close() {
        if (predictor != null) {
            try {
                predictor.close();
            } catch (Exception e) {
                logger.error("Klaida uždarant predictor: {}", e.getMessage(), e);
            }
        }
        if (model != null) {
            try {
                model.close();
                logger.info("DJL modelis '{}' uždarytas", modelName);
            } catch (Exception e) {
                logger.error("Klaida uždarant modelį '{}': {}", modelName, e.getMessage(), e);
            }
        }
    }
}