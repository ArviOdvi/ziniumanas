package lt.ziniumanas.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.dto.AiArticleCategorizationDto;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiArticleCategorizationRestClient {
    private final ObjectMapper objectMapper;
    private final String classificationUrl = "http://localhost:5000/predict"; // Gali būti konfigūruojama

    public String classify(String contents) {
        if (contents == null || contents.trim().isEmpty()) {
            return "Nežinoma";
        }

        try {
            String jsonRequest = objectMapper.writeValueAsString(new AiArticleCategorizationDto(contents));
            HttpURLConnection connection = createConnection(classificationUrl);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
            return responseJson.has("label") ? responseJson.get("label").asText() : "Nežinoma";

        } catch (Exception e) {
            log.error("\u274C AI klasifikatoriaus klaida: {}", e.getMessage(), e);
            return "Nežinoma";
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
}
