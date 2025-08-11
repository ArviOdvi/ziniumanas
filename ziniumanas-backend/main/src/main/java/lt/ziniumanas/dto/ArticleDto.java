package lt.ziniumanas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private Long id;

    @JsonProperty("articleName")
    private String articleName;

    @JsonProperty("contents")
    private String contents;

    @JsonProperty("articleDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate articleDate;

    @JsonProperty("articleStatus")
    private ArticleStatus articleStatus;

    @JsonProperty("verificationStatus")
    private VerificationStatus verificationStatus;

    @JsonProperty("articleCategory")
    private String articleCategory;

    @JsonProperty("newsSourceId")
    private Long newsSourceId;

    @JsonProperty("sourceName")
    private String sourceName;

    public ArticleDto(Article article) {
        this.id = article.getId();
        this.articleName = article.getArticleName();
        this.contents = article.getContents();
        this.articleDate = article.getArticleDate();
        this.articleStatus = article.getArticleStatus();
        this.verificationStatus = article.getVerificationStatus();
        this.articleCategory = article.getArticleCategory();
        this.newsSourceId = article.getNewsSource() != null ? article.getNewsSource().getId() : null;
        this.sourceName = article.getNewsSource() != null ? article.getNewsSource().getSourceName() : null;
    }
}