package lt.ziniumanas.service.ai_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.ziniumanas.config.ClassificationApiProperties;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
@Service
public class PythonClassificationClient {
    private final ObjectMapper objectMapper;
    private final ClassificationApiProperties apiProperties;

    public PythonClassificationClient(ObjectMapper objectMapper, ClassificationApiProperties apiProperties) {
        this.objectMapper = objectMapper;
        this.apiProperties = apiProperties;
    }

    public String classifyText(String text) throws Exception {
        URL url = new URL(apiProperties.getUrl());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String requestJson = objectMapper.writeValueAsString(new TextRequest(text));
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        JsonNode response = objectMapper.readTree(connection.getInputStream());
        return response.get("category").asText();
    }

    private static class TextRequest {
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