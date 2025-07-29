package lt.ziniumanas.repository;
// Operacijos su tinklapio modelio objektais duomenų bazėje: išsaugojimas, paieška, atnaujinimas, šalinimas
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
          // Galima pridėti papildomus metodus, jei reikės specifinių užklausų
    Optional<Article> findByArticleNameAndArticleDate(String articleName, LocalDate articleDate);
    List<Article> findByArticleNameContainingIgnoreCase(String query);
    List<Article> findByArticleStatus(ArticleStatus status);
    List<Article> findByArticleCategoryIgnoreCaseAndArticleStatusOrderByArticleDateDesc(String category, ArticleStatus status);
    List<Article> findByArticleNameContainingIgnoreCaseAndVerificationStatus(String query, VerificationStatus verificationStatus);
}
