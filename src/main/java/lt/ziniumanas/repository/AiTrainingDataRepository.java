package lt.ziniumanas.repository;

import lt.ziniumanas.model.AiCategorizationTrainingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AiTrainingDataRepository extends JpaRepository<AiCategorizationTrainingData, Long> {
    Optional<AiCategorizationTrainingData> findByText(String text);
    long count();
    @Query("SELECT DISTINCT t.category FROM AiCategorizationTrainingData t")
    List<String> findDistinctCategories();
    List<AiCategorizationTrainingData> findTop15ByOrderByCreatedAtDesc();

    // Naujas metodas visiems įrašams su rūšiavimu
    List<AiCategorizationTrainingData> findAllByOrderByCreatedAtDesc();
}
