package lt.ziniumanas.repository;

import lt.ziniumanas.model.ArticlePendingUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticlePendingUrlRepository extends JpaRepository<ArticlePendingUrl, Long> {
    Optional<ArticlePendingUrl> findByUrl(String url);
    List<ArticlePendingUrl> findByNewsSourceId(Long newsSourceId);
}

