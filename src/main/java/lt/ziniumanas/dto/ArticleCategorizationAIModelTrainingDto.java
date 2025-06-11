package lt.ziniumanas.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
public class ArticleCategorizationAIModelTrainingDto {
    @NotEmpty(message = "Tekstų sąrašas negali būti tuščias")
    private String text;

    @NotEmpty(message = "Kategorijų sąrašas negali būti tuščias")
    private String label;
}