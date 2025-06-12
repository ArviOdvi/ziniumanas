package lt.ziniumanas.service.adminservice;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminArticleService {
    private final ArticleRepository articleRepository;

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
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

