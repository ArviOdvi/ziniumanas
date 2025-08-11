package lt.ziniumanas.service.outsource.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.service.ai_service.AiArticleCategorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonApiArticleCollector implements ArticleCollector{

    private final ArticleRepository articleRepository;
    private final AiArticleCategorizationService aiService;

    @Override
    public void collectArticles() {
        log.debug("📡 JSON API straipsnių rinkimas dar neįgyvendintas.");
        // Čia realizuosi API užklausas, konversiją į Article objektus ir saugojimą
    }
}
