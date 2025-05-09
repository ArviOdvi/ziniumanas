package lt.ziniumanas.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ArticleCategorizationAIModelTrainingDto {
    @NotEmpty(message = "Tekstų sąrašas negali būti tuščias")
    private List<String> texts = new ArrayList<>();

    @NotEmpty(message = "Kategorijų sąrašas negali būti tuščias")
    private List<String> labels = new ArrayList<>();
}