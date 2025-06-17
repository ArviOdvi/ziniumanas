package lt.ziniumanas.service.outsource;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.service.outsource.collector.ArticleCollector;
import lt.ziniumanas.service.outsource.collector.ArticleCollectorFactory;
import lt.ziniumanas.service.outsource.collector.CollectorType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutsourceArticleService {

    private final ArticleCollectorFactory articleCollectorFactory;

    private ArticleCollector htmlCollector;
    private ArticleCollector rssCollector;

    @PostConstruct
    public void init() {
        htmlCollector = articleCollectorFactory.createCollector(CollectorType.HTML);
        rssCollector = articleCollectorFactory.createCollector(CollectorType.RSS);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        log.debug("🚀 Aplikacija startuoja – pradedamas visų tipų straipsnių surinkimas");
        collectAllArticles();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Async
    public void scheduledCollection() {
        log.debug("🔁 Automatinis straipsnių surinkimas (kas 30 min)");
        collectAllArticles();
    }

    private void collectAllArticles() {
//        log.debug("📄 Pradedamas HTML straipsnių surinkimas");
//        htmlCollector.collectArticles();

        log.debug("📰 Pradedamas RSS straipsnių surinkimas");
        rssCollector.collectArticles();

        // Jei ateityje bus daugiau kolektorių – tiesiog pridėk čia
    }
}