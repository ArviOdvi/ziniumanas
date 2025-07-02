package lt.ziniumanas.dto;

import lombok.*;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.ArticleImage;
import lt.ziniumanas.model.ArticleVideo;
import lt.ziniumanas.model.enums.ArticleStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
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
    private boolean verificationStatus;
    private String articleCategory;
    private NewsSourceDto newsSource;
    private List<String> imageUrls;
    private List<String> videoUrls;

    public ArticleDto(Article article) {
        this.id = article.getId();
        this.articleName = article.getArticleName();
        this.contents = article.getContents();
        this.articleDate = article.getArticleDate();
        this.articleStatus = article.getArticleStatus();
        this.verificationStatus = article.isVerificationStatus();
        this.articleCategory = article.getArticleCategory();
        this.newsSource = new NewsSourceDto(article.getNewsSource());

        this.imageUrls = article.getImages() != null ?
                article.getImages().stream()
                        .map(ArticleImage::getImageUrl)
                        .collect(Collectors.toList()) : null;

        this.videoUrls = article.getVideos() != null ?
                article.getVideos().stream()
                        .map(ArticleVideo::getVideoUrl)
                        .collect(Collectors.toList()) : null;
    }
}
