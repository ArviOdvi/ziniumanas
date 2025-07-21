package lt.ziniumanas.service;
//Pagrindiniai aplikacijos veiksmai ir taisykles. Naudoja repozitorijas duomenims pasiekti ir manipuliuoti.

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.error.ArticleNotFoundException;
import lt.ziniumanas.model.Article;
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

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        return articles.stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getArticlesByCategory(String category) {
        List<Article> articles = articleRepository.findByArticleCategoryIgnoreCaseOrderByArticleDateDesc(category);
        return articles.stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }
}