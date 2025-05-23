package lt.ziniumanas.nlp;

import lt.ziniumanas.config.NlpModelProperties;
import lt.ziniumanas.nlp.TextClassificationTranslator;
import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Component
public class TextClassifier implements AutoCloseable{
    private static final Logger logger = LoggerFactory.getLogger(TextClassifier.class);
    private final ZooModel<String, Classifications> model;
    private final TextClassificationTranslator translator;

    public TextClassifier(NlpModelProperties properties) {
        logger.info("Inicializuojamas TextClassifier su modelio keliu: {}", properties.getPath());
        try {
            Path modelPath = Paths.get(properties.getPath());
            Path modelFile = modelPath.resolve("custom-bert.onnx");
            if (!Files.exists(modelFile)) {
                throw new IOException("ONNX modelio failas nerastas: " + modelFile);
            }

            List<String> classes = Arrays.asList(
                    "Ekonomika", "Istorija", "Kultūra", "Laisvalaikis", "Lietuvoje",
                    "Maistas", "Mokslas", "Muzika", "Pasaulyje", "Politika",
                    "Sportas", "Sveikata", "Technologijos", "Vaikams"
            );
            translator = new TextClassificationTranslator(classes, properties);

            Criteria<String, Classifications> criteria = Criteria.builder()
                    .setTypes(String.class, Classifications.class)
                    .optModelPath(Paths.get(properties.getPath(), "custom-bert.onnx"))
                    .optEngine("OnnxRuntime")
                    .optTranslator(translator)
                    .optOption("mapLocation", "true")       // <-- svarbu OnnxRuntime kartais
                    .optProgress(new ProgressBar())         // <-- kad matytum progresą (nebūtina, bet naudinga)
                    .build();

            model = ModelZoo.loadModel(criteria);
            logger.info("ONNX modelis sėkmingai įkeltas");
        } catch (IOException | ModelException e) {
            logger.error("Klaida įkeliant modelį: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko inicializuoti TextClassifier", e);
        }
    }

    public List<Classifications> classify(List<String> texts) {
        try (Predictor<String, Classifications> predictor = model.newPredictor()) {
            return texts.stream().map(text -> {
                try {
                    logger.debug("Klasifikuojamas tekstas: {}", text.substring(0, Math.min(text.length(), 50)));
                    Classifications result = predictor.predict(text);
                    logger.debug("Klasifikacijos rezultatas: {}", result);
                    return result;
                } catch (TranslateException e) {
                    logger.error("Klaida klasifikuojant tekstą: {}", text, e);
                    throw new RuntimeException("Klasifikacijos klaida", e);
                }
            }).toList();
        } catch (Exception e) {
            logger.error("Klaida naudojant Predictor", e);
            throw new RuntimeException("Nepavyko atlikti klasifikacijos", e);
        }
    }

    @Override
    public void close() {
        if (model != null) {
            model.close();
            logger.info("TextClassifier modelis uždarytas");
        }
        if (translator != null) {
            translator.close();
            logger.info("TextClassificationTranslator uždarytas");
        }
    }
}