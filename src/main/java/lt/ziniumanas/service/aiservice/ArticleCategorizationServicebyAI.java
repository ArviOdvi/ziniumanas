package lt.ziniumanas.service.aiservice;

import jakarta.annotation.PreDestroy;
import lt.ziniumanas.model.aimodel.TrainingData;
import lt.ziniumanas.nlp.TextClassifier;
import lt.ziniumanas.repository.airepository.TrainingDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ArticleCategorizationServicebyAI {
    private static final Logger logger = LoggerFactory.getLogger(ArticleCategorizationServicebyAI.class);
    private final TrainingDataRepository trainingDataRepository;
    private final TextClassifier classifier;
    private static final String MODEL_PATH = "models/bert_classifier";

    @Autowired
    public ArticleCategorizationServicebyAI(TrainingDataRepository trainingDataRepository) {
        this.trainingDataRepository = trainingDataRepository;
        this.classifier = new TextClassifier();
    }

    public void trainModel() {
        List<TrainingData> trainingData = trainingDataRepository.findAll();
        if (trainingData.isEmpty()) {
            logger.error("Nėra treniravimo duomenų");
            throw new IllegalStateException("Treniravimo duomenys nerasti");
        }

        Map<String, String> textsWithLabels = trainingData.stream()
                .filter(data -> data.getText() != null && data.getCategory() != null)
                .collect(Collectors.toMap(
                        TrainingData::getCategory,
                        TrainingData::getText,
                        (existing, replacement) -> existing // Išlaikome pirmą tekstą, jei yra dublikatai
                ));

        logger.info("Treniruojama su {} tekstais", textsWithLabels.size());
        classifier.trainModel(textsWithLabels, MODEL_PATH, 5, 0.0001f);
    }

    public String categorizeArticle(String articleText) {
        if (articleText == null || articleText.trim().isEmpty()) {
            logger.warn("Tuščias straipsnio tekstas, grąžinama numatytoji kategorija");
            return "Nežinoma";
        }

        try {
            String category = classifier.classify(Collections.singletonList(articleText));
            logger.info("Straipsnis kategorizuotas kaip: {}", category);
            return category;
        } catch (Exception e) {
            logger.error("Klaida kategorizuojant straipsnį: {}", e.getMessage(), e);
            return "Nežinoma";
        }
    }

    @PreDestroy
    public void cleanup() {
        classifier.close();
    }
}