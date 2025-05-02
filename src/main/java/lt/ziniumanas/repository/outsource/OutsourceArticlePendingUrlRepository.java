package lt.ziniumanas.repository.outsource;

import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.outsource.OutsourceArticlePendingUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutsourceArticlePendingUrlRepository extends JpaRepository<OutsourceArticlePendingUrl, Long> {
    Optional<OutsourceArticlePendingUrl> findByUrl(String url);
    List<OutsourceArticlePendingUrl> findByNewsSourceId(Long newsSourceId);
}

