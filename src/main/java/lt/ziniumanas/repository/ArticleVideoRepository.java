package lt.ziniumanas.repository;

import lt.ziniumanas.model.ArticleVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ArticleVideoRepository extends JpaRepository<ArticleVideo, Long> {
    List<ArticleVideo> findByArticleIdOrderByOrderAsc(Long articleId);
    void deleteByArticleId(Long articleId);
}
