package lt.ziniumanas.repository;

import lt.ziniumanas.model.ArticleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ArticleImageRepository extends JpaRepository<ArticleImage, Long> {
    List<ArticleImage> findByArticleIdOrderByOrderAsc(Long articleId);
    void deleteByArticleId(Long articleId);
}
