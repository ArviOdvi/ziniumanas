package lt.ziniumanas.repository.ai_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import lt.ziniumanas.model.aimodel.TrainingData;

import java.util.Optional;

public interface TrainingDataRepository extends JpaRepository<TrainingData, Long> {
    Optional<TrainingData> findByText(String text);
}
