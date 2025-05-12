package lt.ziniumanas.nlp;

import ai.djl.Application;
import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.translate.TranslateException;
import ai.djl.training.util.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class TextClassifier {
    private static final Logger logger = LoggerFactory.getLogger(TextClassifier.class);
    private static final String MODEL_NAME = "bert-base-multilingual-cased";
    private static final String[] CATEGORIES = {
            "Sportas", "Ekonomika", "Politika", "Kultūra", "Technologijos", "Sveikata",
            "Mokslas", "Istorija", "Pasaulyje", "Lietuvoje", "Vaikams", "Muzika", "Maistas"
    };
    private ZooModel<String, Classifications> model;

    public TextClassifier() {
        loadModel();
    }

    private void loadModel() {
        try {
            Criteria<String, Classifications> criteria = Criteria.builder()
                    .optApplication(Application.NLP.TEXT_CLASSIFICATION)
                    .setTypes(String.class, Classifications.class)
                    .optModelUrls("https://resources.djl.ai/models/huggingface/pytorch/bert-base-multilingual-cased/")
                    .optEngine("PyTorch")
                    .optProgress(new ProgressBar())
                    .build();

            model = criteria.loadModel();
            logger.info("BERT modelis sėkmingai įkeltas: {}", MODEL_NAME);
        } catch (ModelException | IOException e) {
            logger.error("Klaida įkeliant BERT modelį: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko įkelti BERT modelio", e);
        }
    }

    public void trainModel(Map<String, String> textsWithLabels, String savePath, int epochs, float learningRate) {
        try {
            // Paruošiame duomenis
            List<String> texts = new ArrayList<>();
            List<Integer> labels = new ArrayList<>();
            for (Map.Entry<String, String> entry : textsWithLabels.entrySet()) {
                String text = entry.getValue();
                String label = entry.getKey();
                if (text == null || text.trim().isEmpty() || label == null || label.trim().isEmpty()) {
                    logger.warn("Praleistas tuščias tekstas ar etiketė: {}", entry);
                    continue;
                }
                texts.add(text);
                int labelIndex = Arrays.asList(CATEGORIES).indexOf(label);
                if (labelIndex == -1) {
                    logger.warn("Netinkama kategorija: {}", label);
                    continue;
                }
                labels.add(labelIndex);
            }

            if (texts.isEmpty()) {
                throw new IllegalArgumentException("Nėra galiojančių treniravimo duomenų");
            }

            // Sukuriame dataset tiesiogiai
            long[] labelArray = labels.stream().mapToLong(i -> i).toArray();
            ArrayDataset dataset = new ArrayDataset.Builder()
                    .setData(texts.toArray(new String[0]))
                    .setLabels(labelArray)
                    .setSampling(32, true) // Batch size
                    .build();

            // Konfigūruojame treniravimą
            DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                    .optOptimizer(new Adam(learningRate));

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.initialize();
                logger.info("Pradedamas BERT modelio treniravimas su {} tekstais", texts.size());
                EasyTrain.fit(trainer, epochs, dataset, null);
                logger.info("BERT modelio treniravimas baigtas");
            }

            // Išsaugome modelį
            model.save(Paths.get(savePath), "bert_classifier");
            logger.info("BERT modelis išsaugotas į {}", savePath);
        } catch (IOException | TranslateException e) {
            logger.error("Klaida treniruojant BERT modelį: {}", e.getMessage(), e);
            throw new RuntimeException("BERT treniravimo klaida", e);
        }
    }

    public void loadSavedModel(String path) {
        try {
            model.load(Paths.get(path), "bert_classifier");
            logger.info("BERT modelis įkeltas iš {}", path);
        } catch (IOException | ModelException e) {
            logger.error("Klaida įkeliant išsaugotą BERT modelį: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko įkelti BERT modelio", e);
        }
    }

    public String classify(List<String> texts) {
        try (Predictor<String, Classifications> predictor = model.newPredictor()) {
            StringBuilder result = new StringBuilder();
            for (String text : texts) {
                if (text == null || text.trim().isEmpty()) {
                    logger.warn("Tuščias tekstas, praleidžiamas");
                    result.append("Nežinoma\n");
                    continue;
                }
                Classifications classifications = predictor.predict(text);
                String predictedCategory = classifications.best().getClassName();
                result.append(predictedCategory).append("\n");
                logger.debug("Tekstas '{}' priskirtas kategorijai: {}", text.substring(0, Math.min(text.length(), 50)), predictedCategory);
            }
            return result.toString().trim();
        } catch (TranslateException e) {
            logger.error("Klaida klasifikuojant tekstą: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko klasifikuoti tekstų", e);
        }
    }

    public void close() {
        if (model != null) {
            model.close();
            logger.info("BERT modelis uždarytas");
        }
    }
}