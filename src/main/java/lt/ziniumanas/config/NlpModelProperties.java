package lt.ziniumanas.config;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nlp.model")
public class NlpModelProperties {
    @NotNull(message = "Modelio kelias (nlp.model.path) negali būti null")
    private String path;

    @NotNull(message = "Modelio failas (nlp.model.file) negali būti null")
    private String file;

    @NotNull(message = "Modelio pavadinimas (nlp.model.name) negali būti null")
    private String name;

    @Positive(message = "Maksimalus ilgis (nlp.model.maxLength) turi būti teigiamas")
    private int maxLength = 256; // Numatytoji reikšmė

    @Positive(message = "Epochų skaičius (nlp.model.epochs) turi būti teigiamas")
    private int epochs = 5;

    @Positive(message = "Mokymosi greitis (nlp.model.learning-rate) turi būti teigiamas")
    private double learningRate = 0.0001;
}