package lt.ziniumanas.service.outsource.collector;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.repository.ArticlePendingUrlRepository;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.ArticleScrapingRuleRepository;
import lt.ziniumanas.service.ai_service.AiArticleCategorizationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCollectorFactory {
    private final ArticleRepository articleRepository;
    private final ArticlePendingUrlRepository pendingUrlRepository;
    private final ArticleScrapingRuleRepository scrapingRuleRepository;
    private final AiArticleCategorizationService aiArticleCategorizationService;

    public ArticleCollector createCollector(CollectorType type) {
        return switch (type) {
            case HTML -> new HtmlArticleCollector(articleRepository, pendingUrlRepository, scrapingRuleRepository, aiArticleCategorizationService);
            case RSS -> new RssArticleCollector(articleRepository, pendingUrlRepository, scrapingRuleRepository, aiArticleCategorizationService);
            case JSON_API -> new JsonApiArticleCollector(articleRepository, aiArticleCategorizationService);
            case PDF -> new PdfArticleCollector(articleRepository, aiArticleCategorizationService);
        };
    }
}