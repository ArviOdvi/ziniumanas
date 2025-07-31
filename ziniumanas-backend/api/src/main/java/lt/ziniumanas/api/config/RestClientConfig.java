package lt.ziniumanas.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.ziniumanas.config.ClassificationApiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {
    @Bean
    public HttpClient httpClient(ClassificationApiProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
