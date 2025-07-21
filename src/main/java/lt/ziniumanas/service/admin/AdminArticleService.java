package lt.ziniumanas.service.admin;

import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lt.ziniumanas.repository.NewsSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminArticleService {

    private final ArticleRepository articleRepository;
    private final NewsSourceRepository newsSourceRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ArticleDto> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<ArticleDto> getArticleById(Long id) {
        return articleRepository.findById(id).map(ArticleDto::new);
    }

    @org.springframework.transaction.annotation.Transactional
    public ArticleDto updateArticle(ArticleDto articleDto) {
        Article article = articleRepository.findById(articleDto.getId())
                .orElseThrow(() -> new RuntimeException("Straipsnis nerastas"));
        article.setArticleName(articleDto.getArticleName());
        article.setContents(articleDto.getContents());
        article.setArticleCategory(articleDto.getArticleCategory());
        article.setArticleStatus(articleDto.getArticleStatus());
        article.setVerificationStatus(articleDto.getVerificationStatus());
        article.setArticleDate(articleDto.getArticleDate());
        if (articleDto.getNewsSourceId() != null) {
            NewsSource newsSource = newsSourceRepository.findById(articleDto.getNewsSourceId())
                    .orElseThrow(() -> new RuntimeException("NewsSource nerastas"));
            article.setNewsSource(newsSource);
        }
        Article updated = articleRepository.save(article);
        return new ArticleDto(updated);
    }
    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    public Article updateArticle(Long id, Article updatedArticle) {
        Article existing = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Straipsnis nerastas, id = " + id));

        existing.setArticleName(updatedArticle.getArticleName());
        existing.setContents(updatedArticle.getContents());
        existing.setArticleDate(updatedArticle.getArticleDate());
        existing.setArticleStatus(updatedArticle.getArticleStatus());
        existing.setVerificationStatus(updatedArticle.getVerificationStatus());

        return articleRepository.save(existing);
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("Straipsnis nerastas");
        }
        articleRepository.deleteById(id);
    }
}