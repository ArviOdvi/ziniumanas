package lt.ziniumanas.dto;

import lombok.*;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.model.enums.VerificationStatus;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDto {
    private Long id;
    private String articleName;
    private String contents;
    private LocalDate articleDate;
    private ArticleStatus articleStatus;
    private VerificationStatus verificationStatus;
    private String articleCategory;
    private Long newsSourceId;

    public ArticleDto(Article article) {
        this.id = article.getId();
        this.articleName = article.getArticleName();
        this.contents = article.getContents();
        this.articleDate = article.getArticleDate();
        this.articleStatus = article.getArticleStatus();
        this.verificationStatus = article.getVerificationStatus();
        this.articleCategory = article.getArticleCategory();
        this.newsSourceId = article.getNewsSource() != null ? article.getNewsSource().getId() : null;
    }
}