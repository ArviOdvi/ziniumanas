package lt.ziniumanas.service.ai_service;

import lt.ziniumanas.dto.AiArticleCategorizationDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.ziniumanas.config.ClassificationApiProperties;
import lt.ziniumanas.model.Article;
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

    public void assignCategory(Article article) {
        if (article == null || article.getContents() == null || article.getContents().trim().isEmpty()) {
            logger.warn("Straipsnis arba jo turinys tuščias – kategorija nepriskirta");
            article.setArticleCategory("Nežinoma");
            return;
        }

        try {
            String jsonRequest = objectMapper.writeValueAsString(new AiArticleCategorizationDto(article.getContents()));

            URL url = new URL(classificationApiProperties.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000); // rekomenduojama
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
            String category = responseJson.has("label") ? responseJson.get("label").asText() : "Nežinoma";

            logger.info("AI priskyrė kategoriją: {}", category);
            article.setArticleCategory(category);

        } catch (Exception e) {
            logger.error("Klaida jungiantis prie Python klasifikatoriaus: {}", e.getMessage(), e);
            article.setArticleCategory("Nežinoma");
        }
    }

    @PreDestroy
    public void close() {
        logger.info("ArticleCategorizationServicebyAI ruošiasi uždarymui");
    }
}