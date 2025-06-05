package lt.ziniumanas.dto;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class ArticleClassificationResponse {

    private String category;
    public ArticleClassificationResponse(String category) {
        this.category = category;
    }

}
