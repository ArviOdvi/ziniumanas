package lt.ziniumanas.service.admin;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.model.ArticleScrapingRule;
import lt.ziniumanas.repository.ArticleScrapingRuleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminScrapingRuleService {
    private final ArticleScrapingRuleRepository scrapingRuleRepository;

    public List<ArticleScrapingRule> getAllArticleScrapingRule() {
        return scrapingRuleRepository.findAll();
    }

    public ArticleScrapingRule createArticleScrapingRule(ArticleScrapingRule articleScrapingRule) {
        return scrapingRuleRepository.save(articleScrapingRule);
    }

    public ArticleScrapingRule updateArticleScrapingRule(Long id, ArticleScrapingRule updatedArticleScrapingRule) {
        return scrapingRuleRepository.findById(id)
                .map(articleScrapingRule -> {
                    updatedArticleScrapingRule.setId(id);
                    return scrapingRuleRepository.save(updatedArticleScrapingRule);
                })
                .orElse(null); // Arba galite mesti išimtį
    }

    public void deleteArticleScrapingRule(Long id) {
        scrapingRuleRepository.deleteById(id);
    }

    // Čia galima pridėti kitą verslo logiką, susijusią su straipsniais
}
