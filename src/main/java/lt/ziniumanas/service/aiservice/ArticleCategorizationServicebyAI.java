package lt.ziniumanas.service.aiservice;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import lt.ziniumanas.nlp.TextClassificationTranslatorbyAI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class ArticleCategorizationServicebyAI {
    private static final Logger logger = LoggerFactory.getLogger(ArticleCategorizationServicebyAI.class);
    private final ZooModel<String, Classifications> textClassifierModel;

    public ArticleCategorizationServicebyAI() {
        ZooModel<String, Classifications> model = null;
        try {
            Criteria<String, Classifications> criteria = Criteria.builder()
                    .setTypes(String.class, Classifications.class)
                    .optModelUrls("file:///path/to/your/trained_model.ptl.zip") // Atnaujinkite su savo modelio keliu
                    .optTranslator(new TextClassificationTranslatorbyAI()) // Reikės sukurti savo Translator
                    .build();
            model = ModelZoo.loadModel(criteria);
        } catch (IOException | ModelException e) {
            logger.error("Klaida įkeliant AI modelį: {}", e.getMessage());
        }
        this.textClassifierModel = model;
    }
    public String categorizeArticle(String text) {
        if (textClassifierModel != null) {
            try (Predictor<String, Classifications> predictor = textClassifierModel.newPredictor()) {
                Classifications classifications = predictor.predict(text);
                if (!classifications.items().isEmpty()) {
                    return classifications.best().getClassName();
                } else {
                    return "Nežinoma";
                }
            } catch (TranslateException e) {
                logger.error("Klaida prognozuojant kategoriją: {}", e.getMessage());
                return "Klaida";
            }
        } else {
            return "Modelis neįkeltas";
        }
    }
}
