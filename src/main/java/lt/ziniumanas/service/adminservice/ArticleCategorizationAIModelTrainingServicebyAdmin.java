package lt.ziniumanas.service.adminservice;

import lt.ziniumanas.model.aimodel.TrainingData;
import lt.ziniumanas.repository.airepository.TrainingDataRepository;
import lt.ziniumanas.nlp.TextClassifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleCategorizationAIModelTrainingServicebyAdmin {
    private static final Logger log = LoggerFactory.getLogger(ArticleCategorizationAIModelTrainingServicebyAdmin.class);
    private static final List<String> CLASSES = Arrays.asList(
            "Sportas", "Ekonomika", "Politika", "Kultūra", "Technologijos", "Sveikata",
            "Mokslas", "Istorija", "Pasaulyje", "Lietuvoje", "Vaikams", "Muzika", "Maistas"
    );

    @Value("${model.save.path:models/bert_classifier}")
    private String modelSavePath;

    @Value("${model.epochs:5}")
    private int epochs;

    @Value("${model.learning.rate:0.0001}")
    private float learningRate;

    private final TrainingDataRepository trainingDataRepository;
    private final TextClassifier classifier;

    @Autowired
    public ArticleCategorizationAIModelTrainingServicebyAdmin(TrainingDataRepository trainingDataRepository) {
        this.trainingDataRepository = trainingDataRepository;
        this.classifier = new TextClassifier();
    }

    public void trainModel() {
        // Nuskaitome treniravimo duomenis
        List<TrainingData> trainingData = trainingDataRepository.findAll();
        if (trainingData.isEmpty()) {
            log.error("Nėra treniravimo duomenų");
            throw new IllegalStateException("Treniravimo duomenys nerasti");
        }

        // Sukuriame tekstų ir etikečių žemėlapį
        Map<String, String> textsWithLabels = trainingData.stream()
                .filter(data -> data.getText() != null && data.getCategory() != null)
                .collect(Collectors.toMap(
                        TrainingData::getCategory,
                        TrainingData::getText,
                        (existing, replacement) -> existing // Išlaikome pirmą tekstą, jei yra dublikatai
                ));

        // Tikriname kategorijas
        for (String label : textsWithLabels.keySet()) {
            if (!CLASSES.contains(label)) {
                log.warn("Nerasta etiketė '{}' tarp galimų kategorijų: {}", label, CLASSES);
            }
        }
        if (textsWithLabels.size() < CLASSES.size()) {
            log.warn("Treniravimo duomenys apima tik {}/{} kategorijų", textsWithLabels.size(), CLASSES.size());
        }

        log.info("Treniruojama su {} tekstais", textsWithLabels.size());

        // Treniruojame BERT modelį
        try {
            long startTime = System.currentTimeMillis();
            classifier.trainModel(textsWithLabels, modelSavePath, epochs, learningRate);
            log.info("BERT modelio treniravimas užbaigtas per {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Klaida treniruojant BERT modelį: {}", e.getMessage(), e);
            throw new RuntimeException("BERT treniravimo klaida", e);
        }
    }
}