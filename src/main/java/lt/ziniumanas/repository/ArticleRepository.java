package lt.ziniumanas.repository;
// Operacijos su tinklapio modelio objektais duomenų bazėje: išsaugojimas, paieška, atnaujinimas, šalinimas
import lt.ziniumanas.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
          // Galima pridėti papildomus metodus, jei reikės specifinių užklausų
          Optional<Article> findByArticleNameAndArticleDate(String articleName, LocalDate articleDate);
          List<Article> findByArticleCategoryIgnoreCaseOrderByArticleDateDesc(String category);
          List<Article> findByArticleCategoryIgnoreCase(String category);
          List<Article> findByArticleNameContainingIgnoreCase(String query);
}
