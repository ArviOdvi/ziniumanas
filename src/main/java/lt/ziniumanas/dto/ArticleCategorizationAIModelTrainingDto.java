package lt.ziniumanas.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ArticleCategorizationAIModelTrainingDto {
    private List<String> texts;
    private List<String> labels;
}

