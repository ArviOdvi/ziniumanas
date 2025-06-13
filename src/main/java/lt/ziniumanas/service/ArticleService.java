package lt.ziniumanas.service;
//Pagrindiniai aplikacijos veiksmai ir taisykles. Naudoja repozitorijas duomenims pasiekti ir manipuliuoti.

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.error.ArticleNotFoundException;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public List<Article> getArticlesByCategory(String category) {
        return articleRepository.findByArticleCategoryIgnoreCaseOrderByArticleDateDesc(category);
    }

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }
    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    public Article updateArticle(Long id, Article updatedArticle) {
        Article existing = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Straipsnis nerastas, id = " + id));

        // Tik atnaujiname tai, ką leidžiame vartotojui keisti
        existing.setArticleName(updatedArticle.getArticleName());
        existing.setContents(updatedArticle.getContents());
        existing.setArticleDate(updatedArticle.getArticleDate());
        existing.setArticleStatus(updatedArticle.getArticleStatus());
        existing.setVerificationStatus(updatedArticle.isVerificationStatus());

        return articleRepository.save(existing);
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    // Čia galima pridėti kitą verslo logiką, susijusią su straipsniais
}
