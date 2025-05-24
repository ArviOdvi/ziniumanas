package lt.ziniumanas.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ArticleClassificationRequest {
    private String text;

    public ArticleClassificationRequest() {}

    public ArticleClassificationRequest(String text) {
        this.text = text;
    }

}
