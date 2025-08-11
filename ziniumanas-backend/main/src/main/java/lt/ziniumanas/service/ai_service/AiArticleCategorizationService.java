package lt.ziniumanas.service.ai_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.api.client.AiArticleCategorizationRestClient;
import lt.ziniumanas.model.Article;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiArticleCategorizationService {

    private final AiArticleCategorizationRestClient aiClient;

    public void assignCategory(Article article) {
        if (article == null || article.getContents() == null || article.getContents().trim().isEmpty()) {
            log.debug("⚠️ Straipsnis arba jo turinys tuščias – kategorija nepriskirta");
            if (article != null) {
                article.setArticleCategory("Nežinoma");
            }
            return;
        }

        try {
            String category = aiClient.classify(article.getContents());
            log.debug("✅ AI priskyrė kategoriją: {}", category);
            article.setArticleCategory(category);
        } catch (Exception e) {
            log.debug("❌ Klaida jungiantis prie Python klasifikatoriaus: {}", e.getMessage(), e);
            article.setArticleCategory("Nežinoma");
        }
    }

    @PreDestroy
    public void close() {
        log.debug("⚠️ ArticleCategorizationServicebyAI ruošiasi uždarymui");
    }
}