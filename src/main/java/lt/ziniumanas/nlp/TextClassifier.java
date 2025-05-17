package lt.ziniumanas.nlp;

import lt.ziniumanas.config.NlpModelProperties;
import ai.djl.Application;
import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorFactory;
import ai.djl.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



@Component
public class TextClassifier {
    private static final Logger logger = LoggerFactory.getLogger(TextClassifier.class);

    private final ZooModel<String, Classifications> model;
    private final Predictor<String, Classifications> predictor;
    private final List<String> classes = Arrays.asList(
            "Ekonomika", "Istorija", "Kultūra", "Laisvalaikis", "Lietuvoje", "Maistas", "Mokslas", "Muzika",
            "Pasaulyje", "Politika", "Sportas", "Sveikata", "Technologijos", "Vaikams"
    );

    public TextClassifier(NlpModelProperties properties) {
        try {
            Path modelPath = Paths.get(properties.getPath());
            if (!Files.exists(modelPath)) {
                logger.error("Modelio katalogas {} neegzistuoja", modelPath);
                throw new IOException("Modelio katalogas nerastas: " + modelPath);
            }

            Criteria<String, Classifications> criteria = Criteria.builder()
                    .optApplication(Application.NLP.TEXT_CLASSIFICATION)
                    .setTypes(String.class, Classifications.class)
                    .optModelPath(modelPath)
                    .optOption("modelName", properties.getFile())
                    .optEngine("PyTorch")
                    .optTranslatorFactory(new CustomTranslatorFactory(classes, properties))
                    .optProgress(new ProgressBar())
                    .build();

            this.model = ModelZoo.loadModel(criteria);
            this.predictor = model.newPredictor();
            logger.info("Modelis '{}' įkeltas sėkmingai iš {}", properties.getName(), modelPath);
        } catch (IOException e) {
            logger.error("Nepavyko įkelti modelio ar tokenizerio dėl failų prieigos klaidos", e);
            throw new RuntimeException("Failų prieigos klaida įkeliant modelį", e);
        } catch (ModelException e) {
            logger.error("Nepavyko įkelti DJL modelio dėl modelio konfigūracijos", e);
            throw new RuntimeException("DJL modelio įkėlimo klaida", e);
        } catch (Exception e) {
            logger.error("Netikėta klaida įkeliant modelį", e);
            throw new RuntimeException("Nepavyko įkelti DJL modelio", e);
        }
    }

    public String classify(List<String> texts) {
        if (texts == null || texts.isEmpty() || texts.get(0).trim().isEmpty()) {
            logger.warn("Tuščias arba netinkamas tekstas klasifikacijai");
            return "Nežinoma";
        }

        String text = texts.get(0);
        try {
            logger.debug("Klasifikuojamas tekstas: {}", text);
            Classifications result = predictor.predict(text);
            String bestClass = result.best().getClassName();
            logger.info("Tekstas klasifikuotas kaip: {}", bestClass);
            return bestClass;
        } catch (TranslateException e) {
            logger.error("Klaida klasifikuojant tekstą: {}", text, e);
            return "Nežinoma";
        }
    }

    @PreDestroy
    public void close() {
        try {
            predictor.close();
            model.close();
            logger.info("Modelis uždarytas sėkmingai");
        } catch (Exception e) {
            logger.error("Klaida uždarant modelį", e);
        }
    }

    static class CustomTranslatorFactory implements TranslatorFactory {
        private final List<String> classes;
        private final NlpModelProperties properties;

        public CustomTranslatorFactory(List<String> classes, NlpModelProperties properties) {
            this.classes = classes;
            this.properties = properties;
        }

        @Override
        public <I, O> Translator<I, O> newInstance(Class<I> input, Class<O> output, Model model, Map<String, ?> arguments) {
            return (Translator<I, O>) new TextClassificationTranslator(classes, properties);
        }

        @Override
        public Set<Pair<Type, Type>> getSupportedTypes() {
            return Collections.singleton(new Pair<>(String.class, Classifications.class));
        }
    }
}