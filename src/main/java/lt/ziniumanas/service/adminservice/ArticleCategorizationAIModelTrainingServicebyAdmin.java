package lt.ziniumanas.service.adminservice;

import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class ArticleCategorizationAIModelTrainingServicebyAdmin {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ArticleCategorizationAIModelTrainingServicebyAdmin(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long getTrainingDataCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM article_training_data", Long.class);
    }

    public void handleTrainingData(ArticleCategorizationAIModelTrainingDto dto) {
        List<String> texts = dto.getTexts();
        List<String> labels = dto.getLabels();

        if (texts.size() != labels.size()) {
            throw new IllegalArgumentException("Tekstų ir kategorijų skaičius nesutampa.");
        }

        for (int i = 0; i < texts.size(); i++) {
            jdbcTemplate.update("INSERT INTO article_training_data (text, category) VALUES (?, ?)",
                    texts.get(i), labels.get(i));
        }
    }

    public void trainModel() throws Exception {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT text, category FROM article_training_data");

        if (rows.size() < 10) throw new RuntimeException("Per mažai įrašų modelio treniravimui.");

        Collections.shuffle(rows);
        int total = rows.size();
        int trainEnd = (int) (total * 0.6);
        int validEnd = (int) (total * 0.8);

        List<Map<String, Object>> trainRows = rows.subList(0, trainEnd);
        List<Map<String, Object>> validRows = rows.subList(trainEnd, validEnd);
        List<Map<String, Object>> testRows = rows.subList(validEnd, total);

        writeCsv(trainRows, "dataset.csv");
        writeCsv(validRows, "valid.csv");
        writeCsv(testRows, "test.csv");

        // Paleidžiamas Python treniravimo skriptas
        Process process = new ProcessBuilder("python", "C:/Users/Admin/IdeaProjects/Ziniumanas/train_model.py")
                .inheritIO()
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python modelio treniravimas nepavyko. Exit code: " + exitCode);
        }
    }

    private void writeCsv(List<Map<String, Object>> rows, String fileName) throws Exception {
        Path path = Path.of("C:/Users/Admin/IdeaProjects/Ziniumanas/models", fileName);
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write("text,category\n");
            for (Map<String, Object> row : rows) {
                String text = row.get("text").toString().replace("\"", "\"\"");
                String category = row.get("category").toString();
                writer.write("\"" + text + "\",\"" + category + "\"\n");
            }
        }
    }

    public List<String> getValidCategories() {
        return List.of(
                "Ekonomika", "Istorija", "Kultūra", "Laisvalaikis", "Lietuvoje",
                "Maistas", "Mokslas", "Muzika", "Pasaulyje", "Politika",
                "Sportas", "Sveikata", "Technologijos", "Vaikams"
        );
    }

    public Map<String, Object> getTestMetrics() {
        try {
            String path = "C:/Users/Admin/IdeaProjects/Ziniumanas/models/test_metrics.json";
            byte[] jsonData = Files.readAllBytes(Paths.get(path));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonData, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Nepavyko nuskaityti testavimo metrikų: " + e.getMessage(), e);
        }
    }
}