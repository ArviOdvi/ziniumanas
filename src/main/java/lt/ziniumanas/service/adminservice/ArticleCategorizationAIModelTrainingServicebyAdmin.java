package lt.ziniumanas.service.adminservice;

import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.model.aimodel.TrainingData;
import lt.ziniumanas.repository.airepository.TrainingDataRepository;
import lt.ziniumanas.nlp.TextClassifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ArticleCategorizationAIModelTrainingServicebyAdmin(
            TrainingDataRepository trainingDataRepository,
            TextClassifier classifier) {
        this.trainingDataRepository = trainingDataRepository;
        this.classifier = classifier;
    }

    @Async
    @Transactional
    public void trainModel() {
        List<TrainingData> trainingData = trainingDataRepository.findAll();
        if (trainingData.isEmpty()) {
            log.error("Nėra treniravimo duomenų");
            throw new IllegalStateException("Treniravimo duomenys nerasti");
        }

        Map<String, String> textsWithLabels = trainingData.stream()
                .filter(data -> data.getText() != null && data.getCategory() != null)
                .collect(Collectors.toMap(
                        TrainingData::getText,
                        TrainingData::getCategory,
                        (existing, replacement) -> existing, // Ignoruoti dublikatus
                        LinkedHashMap::new
                ));

        Set<String> uniqueCategories = new HashSet<>(textsWithLabels.values());
        if (uniqueCategories.size() < CLASSES.size()) {
            log.warn("Treniravimo duomenys apima tik {}/{} kategorijų", uniqueCategories.size(), CLASSES.size());
        }

        log.info("Treniruojama su {} tekstais", textsWithLabels.size());

        try {
            long start = System.currentTimeMillis();
            classifier.trainModel(textsWithLabels, modelSavePath, epochs, learningRate);
            log.info("Modelis ištreniruotas per {} ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Klaida treniruojant modelį: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko treniruoti modelio", e);
        }
    }

    public long getTrainingDataCount() {
        return trainingDataRepository.count();
    }

    public List<String> getValidCategories() {
        return new ArrayList<>(CLASSES);
    }

    @Async
    @Transactional
    public void handleTrainingData(ArticleCategorizationAIModelTrainingDto dto) {
        List<String> texts = dto.getTexts().stream()
                .map(this::preprocessText)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        List<String> labels = dto.getLabels().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        validateInput(texts, labels);

        Map<String, String> textsWithLabels = new LinkedHashMap<>();
        List<TrainingData> newData = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (trainingDataRepository.findByText(text).isPresent()) {
                log.warn("Tekstas jau egzistuoja: {}", text.substring(0, Math.min(text.length(), 50)));
                continue;
            }

            TrainingData data = TrainingData.builder()
                    .text(text)
                    .category(labels.get(i))
                    .build();
            newData.add(data);
            textsWithLabels.put(text, labels.get(i));
            log.info("Paruoštas įrašas: tekstas='{}...', kategorija='{}'",
                    text.substring(0, Math.min(text.length(), 50)), labels.get(i));
        }

        if (textsWithLabels.isEmpty()) {
            log.warn("Nėra naujų unikalių tekstų treniravimui");
            throw new IllegalArgumentException("Klaida: nėra naujų unikalių tekstų treniravimui");
        }

        try {
            trainingDataRepository.saveAll(newData);
            log.info("Išsaugota {} naujų treniravimo įrašų", newData.size());

            long startTime = System.currentTimeMillis();
            classifier.trainModel(textsWithLabels, modelSavePath, epochs, learningRate);
            log.info("Modelis ištreniruotas per {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Klaida treniruojant modelį: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko užbaigti modelio treniravimo: " + e.getMessage());
        }
    }

    private String preprocessText(String text) {
        if (text == null) return "";
        // Pašalinti HTML žymes, specialiuosius simbolius, normalizuoti
        text = text.replaceAll("<[^>]+>", "") // Pašalina HTML
                .replaceAll("[^a-zA-Z0-9\\s]", " ") // Pašalina specialiuosius simbolius
                .toLowerCase()
                .trim();
        // Apriboti ilgį iki 500 simbolių (saugus distilbert žetonų limitокурашіптуваннямізуванння
        // Apriboti ilgį iki 500 simbolių (saugus distilbert žetonų limitas)
        return text.length() > 500 ? text.substring(0, 500) : text;
    }

    private void validateInput(List<String> texts, List<String> labels) {
        if (texts.isEmpty() || labels.isEmpty()) {
            log.warn("Tušti tekstų arba etikečių sąrašai: tekstai={}, etiketės={}", texts.size(), labels.size());
            throw new IllegalArgumentException("Klaida: įveskite bent vieną tekstą ir kategoriją");
        }

        if (texts.size() != labels.size()) {
            log.warn("Tekstų ir etikečių skaičius nesutampa: {} vs {}", texts.size(), labels.size());
            throw new IllegalArgumentException("Klaida: tekstų ir kategorijų skaičius turi sutapti");
        }

        for (String label : labels) {
            if (!CLASSES.contains(label)) {
                log.warn("Neteisinga kategorija: {}", label);
                throw new IllegalArgumentException("Klaida: neteisinga kategorija: " + label);
            }
        }
    }
}