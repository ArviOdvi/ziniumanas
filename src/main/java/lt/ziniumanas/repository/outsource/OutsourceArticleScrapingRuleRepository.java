package lt.ziniumanas.repository.outsource;

import lt.ziniumanas.model.outsource.OutsourceArticleScrapingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OutsourceArticleScrapingRuleRepository extends JpaRepository<OutsourceArticleScrapingRule, Long> {
    List<OutsourceArticleScrapingRule> findByNewsSourceId(Long newsSourceId);
}
