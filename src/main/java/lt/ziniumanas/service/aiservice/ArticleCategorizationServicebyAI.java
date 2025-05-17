package lt.ziniumanas.service.aiservice;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.ZooModel;
import lt.ziniumanas.nlp.TextClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PreDestroy;
import java.util.List;


@Service
public class ArticleCategorizationServicebyAI {

        private static final String modelPath = "src/main/resources/models/text_classifier_model.h5";
        private static final Logger logger = LoggerFactory.getLogger(ArticleCategorizationServicebyAI.class);

        private final TextClassifier textClassifier; // Naudojame TextClassifier

        @Autowired // Įpurškiame TextClassifier per konstruktorių
        public ArticleCategorizationServicebyAI(TextClassifier textClassifier) {
            this.textClassifier = textClassifier;
        }
        public String categorizeArticle(String articleText) {
            if (articleText == null || articleText.trim().isEmpty()) {
                logger.warn("Tuščias straipsnio tekstas, grąžinama numatytoji kategorija");
                return "Nežinoma";
            }

            // Klasifikacija naudojant TextClassifier
            List<String> inputTexts = List.of(articleText); // Supakuojame į List, kaip reikalauja classify
            String category = textClassifier.classify(inputTexts);
            logger.info("Straipsnis kategorizuotas kaip: {}", category);
            return category;
        }

        @PreDestroy
        public void close() {
            // Nebereikia rūpintis modelio ir predictor uždarymu, tai daro TextClassifier
            logger.info("ArticleCategorizationServicebyAI ruošiasi uždarymui");
        }
    }
