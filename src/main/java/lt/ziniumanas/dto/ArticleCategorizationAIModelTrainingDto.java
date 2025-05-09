package lt.ziniumanas.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class ArticleCategorizationAIModelTrainingDto {
    // Getters and Setters
    @NotEmpty(message = "Tekstų sąrašas negali būti tuščias")
    @Size(min = 1, message = "Turi būti bent vienas tekstas")
    private List<String> texts;

    @NotEmpty(message = "Etikečių sąrašas negali būti tuščias")
    @Size(min = 1, message = "Turi būti bent viena etiketė")
    private List<String> labels;

}