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
    private static final Logger logger = LoggerFactory.getLogger(ArticleCategorizationServicebyAI.class);

    private final TextClassifier textClassifier;

    @Autowired
    public ArticleCategorizationServicebyAI(TextClassifier textClassifier) {
        this.textClassifier = textClassifier;
    }

    public String categorizeArticle(String articleText) {
        if (articleText == null || articleText.trim().isEmpty()) {
            logger.warn("Tuščias straipsnio tekstas, grąžinama numatytoji kategorija");
            return "Nežinoma";
        }

        try {
            // Klasifikacija naudojant TextClassifier
            List<String> inputTexts = List.of(articleText); // Supakuojame į List
            List<Classifications> results = textClassifier.classify(inputTexts);

            if (results.isEmpty()) {
                logger.warn("Klasifikacijos rezultatai tušti, grąžinama numatytoji kategorija");
                return "Nežinoma";
            }

            // Gauname geriausią kategoriją iš pirmojo Classifications objekto
            Classifications classification = results.get(0);
            String category = classification.best().getClassName();
            logger.info("Straipsnis kategorizuotas kaip: {}", category);
            return category;
        } catch (Exception e) {
            logger.error("Klaida kategorizuojant straipsnį: {}", e.getMessage(), e);
            return "Nežinoma";
        }
    }

    @PreDestroy
    public void close() {
        // TextClassifier tvarko modelio ir predictor uždarymą
        logger.info("ArticleCategorizationServicebyAI ruošiasi uždarymui");
    }
}