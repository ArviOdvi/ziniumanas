package lt.ziniumanas.service.ai_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.dto.AiArticleCategorizationDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.ziniumanas.config.ClassificationApiProperties;
import lt.ziniumanas.model.Article;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiArticleCategorizationService {

    private final ObjectMapper objectMapper;
    private final ClassificationApiProperties classificationApiProperties;

    public void assignCategory(Article article) {
        if (article == null || article.getContents() == null || article.getContents().trim().isEmpty()) {
            log.debug("⚠️ Straipsnis arba jo turinys tuščias – kategorija nepriskirta");
            if (article == null) {
                log.debug("⚠️ Straipsnis yra null – kategorija nepriskirta");
                return;
            }

            if (article.getContents() == null || article.getContents().trim().isEmpty()) {
                log.debug("⚠️ Straipsnio turinys tuščias – kategorija nepriskirta");
                article.setArticleCategory("Nežinoma");
                return;
            }
            article.setArticleCategory("Nežinoma");
            return;
        }

        try {
            String jsonRequest = objectMapper.writeValueAsString(new AiArticleCategorizationDto(article.getContents()));

            HttpURLConnection connection = createConnection(classificationApiProperties.getUrl());

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
            String category = responseJson.has("label") ? responseJson.get("label").asText() : "Nežinoma";

            log.debug("✅ AI priskyrė kategoriją: {}", category);
            article.setArticleCategory(category);

        } catch (Exception e) {
            log.debug("❌ Klaida jungiantis prie Python klasifikatoriaus: {}", e.getMessage(), e);
            article.setArticleCategory("Nežinoma");
        }
    }
    private HttpURLConnection createConnection(String urlString) throws IOException {
        URI uri = URI.create(urlString);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);
        return connection;
    }

    @PreDestroy
    public void close() {
        log.debug("⚠️ ArticleCategorizationServicebyAI ruošiasi uždarymui");
    }
}