package lt.ziniumanas.dto;

import lombok.*;

import jakarta.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCategorizationAIModelTrainingDto {
    @NotEmpty(message = "Tekstų sąrašas negali būti tuščias")
    private String text;

    @NotEmpty(message = "Kategorijų sąrašas negali būti tuščias")
    private String label;
}