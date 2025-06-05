package lt.ziniumanas.service.adminservice;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminArticleManagementService {
    private final ArticleRepository articleRepository;

    @Autowired
    public AdminArticleManagementService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    public Article updateArticle(Long id, Article updatedArticle) {
        return articleRepository.findById(id)
                .map(article -> {
                    updatedArticle.setId(id);
                    return articleRepository.save(updatedArticle);
                })
                .orElse(null); // Arba galite mesti išimtį
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    // Čia galima pridėti kitą verslo logiką, susijusią su straipsniais
}

