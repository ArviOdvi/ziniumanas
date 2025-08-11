package lt.ziniumanas.service;
//Pagrindiniai aplikacijos veiksmai ir taisykles. Naudoja repozitorijas duomenims pasiekti ir manipuliuoti.

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.error.ArticleNotFoundException;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.model.enums.VerificationStatus;
import lt.ziniumanas.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Optional<ArticleDto> findById(Long id) {
        return articleRepository.findById(id)
                .map(ArticleDto::new);
    }

    @Transactional(readOnly = true)
    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> searchByQuery(String query) {
        List<Article> articles = articleRepository.findByArticleNameContainingIgnoreCaseAndVerificationStatus(
                query,
                VerificationStatus.TRUE
        );
        return articles.stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getAllArticles() {
        List<Article> articles = articleRepository.findByArticleStatus(ArticleStatus.PUBLISHED);
        return articles.stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getArticlesByCategory(String category) {
        List<Article> articles = articleRepository.findByArticleCategoryIgnoreCaseAndArticleStatusOrderByArticleDateDesc(
                category,
                ArticleStatus.PUBLISHED
        );
        return articles.stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }
}