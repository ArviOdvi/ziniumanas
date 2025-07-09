package lt.ziniumanas.service.admin;

import jakarta.transaction.Transactional;
import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminArticleService {

    private final ArticleRepository articleRepository;

    public List<ArticleDto> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }

    public Optional<ArticleDto> getArticleById(Long id) {
        return articleRepository.findById(id).map(ArticleDto::new);
    }

    public ArticleDto updateArticle(ArticleDto articleDto) {
        Article article = articleRepository.findById(articleDto.getId())
                .orElseThrow(() -> new RuntimeException("Straipsnis nerastas"));
        article.setArticleName(articleDto.getArticleName());
        article.setContents(articleDto.getContents());
        article.setArticleCategory(articleDto.getArticleCategory());
        article.setArticleStatus(articleDto.getArticleStatus());
        article.setVerificationStatus(articleDto.isVerificationStatus());
        article.setArticleDate(articleDto.getArticleDate());
        Article updated = articleRepository.save(article);
        return new ArticleDto(updated);
    }

    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("Straipsnis nerastas");
        }
        articleRepository.deleteById(id);
    }
}