package lt.ziniumanas.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AiArticleCategorizationDto {
    private String text;

    public AiArticleCategorizationDto() {
    }

    public AiArticleCategorizationDto(String text) {
        this.text = text;
    }

}
