package lt.ziniumanas.service.adminservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.model.aimodel.TrainingData;
import lt.ziniumanas.repository.ai_repository.TrainingDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ArticleCategorizationAIModelTrainingbyAdminService {
    private static final Logger log = LoggerFactory.getLogger(ArticleCategorizationAIModelTrainingbyAdminService.class);
    private static final String BASE_PATH = System.getProperty("user.dir");
    private final TrainingDataRepository trainingDataRepository;

    @Autowired
    public ArticleCategorizationAIModelTrainingbyAdminService(TrainingDataRepository trainingDataRepository) {
        this.trainingDataRepository = trainingDataRepository;
    }

    public long getTrainingDataCount() {
        try {
            return trainingDataRepository.count();
        } catch (Exception e) {
            log.error("Klaida gaunant treniravimo duomenų kiekį: {}", e.getMessage(), e);
            return 0;
        }
    }

    public List<TrainingData> getRecentTrainingData() {
        try {
            return trainingDataRepository.findTop15ByOrderByCreatedAtDesc();
        } catch (Exception e) {
            log.error("Klaida gaunant naujausius treniravimo duomenis: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<TrainingData> getAllTrainingData() {
        try {
            return trainingDataRepository.findAllByOrderByCreatedAtDesc();
        } catch (Exception e) {
            log.error("Klaida gaunant visus treniravimo duomenis: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void handleTrainingData(ArticleCategorizationAIModelTrainingDto dto) {
        List<String> texts = dto.getTexts();
        List<String> labels = dto.getLabels();
        List<String> validCategories = getValidCategories();

        if (texts.size() != labels.size()) {
            throw new IllegalArgumentException("Tekstų ir kategorijų skaičius nesutampa.");
        }

        for (String label : labels) {
            if (!validCategories.contains(label)) {
                throw new IllegalArgumentException("Neleistina kategorija: " + label);
            }
        }

        for (int i = 0; i < texts.size(); i++) {
            TrainingData data = TrainingData.builder()
                    .text(texts.get(i))
                    .category(labels.get(i))
                    .build();
            trainingDataRepository.save(data);
            log.info("Įrašytas įrašas: tekstas='{}', kategorija='{}'", texts.get(i), labels.get(i));
        }
    }

    public void trainModel() throws Exception {
        List<TrainingData> rows = trainingDataRepository.findAll();

        if (rows.size() < 10) {
            throw new RuntimeException("Per mažai įrašų modelio treniravimui. Reikalinga bent 10 įrašų, yra: " + rows.size());
        }

        Collections.shuffle(rows);
        int total = rows.size();
        int trainEnd = (int) (total * 0.6);
        int validEnd = (int) (total * 0.8);

        List<TrainingData> trainRows = rows.subList(0, trainEnd);
        List<TrainingData> validRows = rows.subList(trainEnd, validEnd);
        List<TrainingData> testRows = rows.subList(validEnd, total);

        writeCsv(trainRows, "/models/" + "dataset.csv");
        writeCsv(validRows, "/models/" + "valid.csv");
        writeCsv(testRows, "/models/" + "test.csv");

        Process process = new ProcessBuilder("python", BASE_PATH + "/python-classifier-api/" + "train_model.py")
                .inheritIO()
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python modelio treniravimas nepavyko. Exit code: " + exitCode);
        }
    }

    private void writeCsv(List<TrainingData> rows, String fileName) throws Exception {
        Path path = Path.of(BASE_PATH, fileName);
        Files.createDirectories(path.getParent());
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write("text,category\n");
            for (TrainingData row : rows) {
                String text = row.getText().replace("\"", "\"\"");
                String category = row.getCategory();
                writer.write("\"" + text + "\",\"" + category + "\"\n");
            }
        }
    }

    public List<String> getValidCategories() {
        List<String> categories = trainingDataRepository.findDistinctCategories();
        return categories.isEmpty() ? List.of(
                "Ekonomika", "Istorija", "Kultūra", "Laisvalaikis", "Lietuvoje",
                "Maistas", "Mokslas", "Muzika", "Pasaulyje", "Politika",
                "Sportas", "Sveikata", "Technologijos", "Vaikams"
        ) : categories;
    }

    public Map<String, Object> getTestMetrics() {
        try {
            Path path = Path.of(BASE_PATH, "test_metrics.json");
            byte[] jsonData = Files.readAllBytes(path);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonData, Map.class);
        } catch (Exception e) {
            log.error("Nepavyko nuskaityti testavimo metrikų: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko nuskaityti testavimo metrikų: " + e.getMessage(), e);
        }
    }
}