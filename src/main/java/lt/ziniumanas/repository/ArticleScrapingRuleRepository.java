package lt.ziniumanas.repository;

import lt.ziniumanas.model.ArticleScrapingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ArticleScrapingRuleRepository extends JpaRepository<ArticleScrapingRule, Long> {
    List<ArticleScrapingRule> findByNewsSourceId(Long newsSourceId);

}
