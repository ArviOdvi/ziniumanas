package lt.ziniumanas.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import lt.ziniumanas.api.config.ClassificationApiProperties;
import lt.ziniumanas.api.dto.AiArticleCategorizationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
@Slf4j
@Component
@RequiredArgsConstructor
public class AiArticleCategorizationRestClient {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient; // Injekcija iš RestClientConfig
    private final ClassificationApiProperties properties; // Injekcija, kad gautum url

    public String classify(String contents) {
        if (contents == null || contents.trim().isEmpty()) {
            return "Nežinoma";
        }

        try {
            String jsonRequest = objectMapper.writeValueAsString(new AiArticleCategorizationDto(contents));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getUrl())) // Naudojame url iš konfigūracijos
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.debug("AI serveris grąžino klaidą: statusas {}, atsakymas: {}", response.statusCode(), response.body());
                return "Nežinoma";
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            return responseJson.has("label") ? responseJson.get("label").asText() : "Nežinoma";

        } catch (Exception e) {
            log.debug("❌ AI klasifikatoriaus klaida: {}", e.getMessage(), e);
            return "Nežinoma";
        }
    }
}
