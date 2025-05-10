package lt.ziniumanas.repository.airepository;

import org.springframework.data.jpa.repository.JpaRepository;
import lt.ziniumanas.model.aimodel.TrainingData;
public interface TrainingDataRepository extends JpaRepository<TrainingData, Long> {
}
