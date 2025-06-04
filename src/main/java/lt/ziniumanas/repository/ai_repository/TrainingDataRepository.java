package lt.ziniumanas.repository.ai_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import lt.ziniumanas.model.aimodel.TrainingData;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrainingDataRepository extends JpaRepository<TrainingData, Long> {
    Optional<TrainingData> findByText(String text);
    long count();
    @Query("SELECT DISTINCT t.category FROM TrainingData t")
    List<String> findDistinctCategories();
    List<TrainingData> findTop15ByOrderByCreatedAtDesc();

    // Naujas metodas visiems įrašams su rūšiavimu
    List<TrainingData> findAllByOrderByCreatedAtDesc();
}
