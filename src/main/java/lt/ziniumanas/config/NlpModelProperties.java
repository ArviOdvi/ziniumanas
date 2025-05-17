package lt.ziniumanas.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "nlp.model")
public class NlpModelProperties {
    // Getteriai ir setteriai
    private String path;
    private String name;
    private String file;

}

