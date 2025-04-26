package lt.ziniumanas.repository;

import lt.ziniumanas.model.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsSourceRepository extends JpaRepository<NewsSource, Long> {
    // Čia galima pridėti papildomus metodus
    Optional<NewsSource> findByUrlAddressContainingIgnoreCase(String urlAddress);
}
