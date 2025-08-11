package lt.ziniumanas.repository;

import lt.ziniumanas.model.NewsmanUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsmanUserRepository extends JpaRepository<NewsmanUser, Long> {
    Optional<NewsmanUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
