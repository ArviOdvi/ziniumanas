package lt.ziniumanas.service.outsource.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.service.ai_service.AiArticleCategorizationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfArticleCollector implements ArticleCollector{

    private final ArticleRepository articleRepository;
    private final AiArticleCategorizationService aiService;

    @Override
    public void collectArticles() {
        log.debug("📄 PDF straipsnių rinkimas dar neįgyvendintas.");
        // Čia realizuosi PDF failų parsisiuntimą, teksto išgavimą ir straipsnio sukūrimą
    }
}
