package lt.ziniumanas.config;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "classification.api")
public class ClassificationApiProperties {
    @NotNull(message = "API URL (classification.api.url) negali būti null")
    private String url;
}
