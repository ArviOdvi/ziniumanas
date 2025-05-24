package lt.ziniumanas.config;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "classification.api")
public class ClassificationApiProperties {
    @NotNull(message = "API URL (classification.api.url) negali būti null")
    private String url;
}
