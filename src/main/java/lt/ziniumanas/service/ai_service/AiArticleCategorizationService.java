package lt.ziniumanas.service.ai_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.ziniumanas.config.ClassificationApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


@Service
public class AiArticleCategorizationService {
    private static final Logger logger = LoggerFactory.getLogger(AiArticleCategorizationService.class);

    private final ObjectMapper objectMapper;
    private final ClassificationApiProperties classificationApiProperties;

    public AiArticleCategorizationService(ObjectMapper objectMapper,
                                          ClassificationApiProperties classificationApiProperties) {
        this.objectMapper = objectMapper;
        this.classificationApiProperties = classificationApiProperties;
    }

    public String categorizeArticle(String articleText) {
        if (articleText == null || articleText.trim().isEmpty()) {
            logger.warn("Tuščias straipsnio tekstas, grąžinama numatytoji kategorija");
            return "Nežinoma";
        }

        try {
            // JSON objektas
            String jsonRequest = objectMapper.writeValueAsString(new TextRequest(articleText));

            // URL paimtas is properties
            URL url = new URL(classificationApiProperties.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
            String category = responseJson.has("label") ? responseJson.get("label").asText() : "Nežinoma";

            logger.info("Straipsnis kategorizuotas kaip: {}", category);
            return category;

        } catch (Exception e) {
            logger.error("Klaida jungiantis prie Python klasifikatoriaus: {}", e.getMessage(), e);
            return "Nežinoma";
        }
    }

    @PreDestroy
    public void close() {
        logger.info("ArticleCategorizationServicebyAI ruošiasi uždarymui");
    }

    static class TextRequest {
        public String text;

        public TextRequest(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}