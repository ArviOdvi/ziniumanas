package lt.ziniumanas.service.adminservice;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.ArticleScrapingRule;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.ArticleScrapingRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdminScrapingRuleService {
    private final ArticleScrapingRuleRepository scrapingRuleRepository;

    @Autowired
    public AdminScrapingRuleService(ArticleScrapingRuleRepository scrapingRuleRepository) {
        this.scrapingRuleRepository = scrapingRuleRepository;
    }

    public List<ArticleScrapingRule> getAllArticleScrapingRule() {
        return scrapingRuleRepository.findAll();
    }

    public Optional<ArticleScrapingRule> getArticleScrapingRuleById(Long id) {
        return scrapingRuleRepository.findById(id);
    }

    public ArticleScrapingRule createArticle(ArticleScrapingRule articleScrapingRule) {
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

    public void deleteArticle(Long id) {
        scrapingRuleRepository.deleteById(id);
    }

    // Čia galima pridėti kitą verslo logiką, susijusią su straipsniais
}
