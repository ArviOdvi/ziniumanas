package lt.ziniumanas.service.outsource;

import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.ArticleScrapingRule;
import lt.ziniumanas.model.ArticlePendingUrl;
import lt.ziniumanas.repository.NewsSourceRepository;
import lt.ziniumanas.repository.ArticleScrapingRuleRepository;
import lt.ziniumanas.repository.ArticlePendingUrlRepository;
import lt.ziniumanas.util.PendingArticleUrlTableSequenceResetUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OutsourceArticlePendingUrlService {
    private static final Logger logger = LoggerFactory.getLogger(OutsourceArticlePendingUrlService.class);

    private final NewsSourceRepository newsSourceRepository;
    private final ArticleScrapingRuleRepository scrapingRuleRepository;
    private final ArticlePendingUrlRepository pendingUrlRepository;
    private final PendingArticleUrlTableSequenceResetUtil sequenceResetUtil;

    public OutsourceArticlePendingUrlService(
            NewsSourceRepository newsSourceRepository,
            ArticleScrapingRuleRepository scrapingRuleRepository,
            ArticlePendingUrlRepository pendingUrlRepository,
            PendingArticleUrlTableSequenceResetUtil sequenceResetUtil
    ) {
        this.newsSourceRepository = newsSourceRepository;
        this.scrapingRuleRepository = scrapingRuleRepository;
        this.pendingUrlRepository = pendingUrlRepository;
        this.sequenceResetUtil = sequenceResetUtil;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        logger.info("🚀 Paleidžiama: trinami seni laukiančių URL įrašai ir surenkami nauji.");
        collectArticleUrlsOnStart();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Async
    public void scheduledUrlCollection() {
        logger.info("🕒 Periodinis laukiančių URL surinkimas kas 30 min...");
        scheduleCollectArticleUrls();
    }

    public void collectArticleUrlsOnStart() {
        logger.info("🧹 Trinami seni įrašai ir restartuojama seka...");
        pendingUrlRepository.deleteAll();
        sequenceResetUtil.resetPendingUrlSequence();
        logger.info("🌐 Pradedamas URL surinkimas iš visų šaltinių...");
        collectUrlsFromAllSources();
        logger.info("✅ Pradinis straipsnių URL surinkimas užbaigtas.");
    }

    public void scheduleCollectArticleUrls() {
        logger.info("🕒 Pusvalandinis straipsnių URL surinkimas...");
        collectUrlsFromAllSources();
        logger.info("✅ Pusvalandinis straipsnių URL surinkimas užbaigtas.");
    }

    private void collectUrlsFromAllSources() {
        List<NewsSource> sources = newsSourceRepository.findAll();
        for (NewsSource source : sources) {
            collectArticleUrls(source);
        }
    }

    private void collectArticleUrls(NewsSource source) {
        logger.info("🔍 Tikrinamas šaltinis: {}", source.getSourceName());

        List<ArticleScrapingRule> rules = scrapingRuleRepository.findByNewsSourceId(source.getId());
        if (rules.isEmpty()) {
            logger.warn("⚠️ Nerasta scraping taisyklių šaltiniui: {}", source.getSourceName());
            return;
        }

        Set<String> foundUrls = new HashSet<>();

        for (ArticleScrapingRule rule : rules) {
            try {
                Document doc = Jsoup.connect(source.getUrlAddress())
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                Elements postTitles = doc.select(rule.getTitleSelector());

                for (Element postTitle : postTitles) {
                    Element linkElement = postTitle.selectFirst("a[href]");
                    if (linkElement != null) {
                        String url = linkElement.absUrl("href");
                        if (!url.isBlank() && foundUrls.add(url)) {
                            savePendingUrl(url, source);
                        }
                    }
                }

                logger.info("✅ Rasta URL'ų šaltiniui {}: {}", source.getSourceName(), foundUrls.size());

            } catch (Exception e) {
                logger.error("❌ Klaida renkant URL'ą iš {}: {}", source.getSourceName(), e.getMessage());
            }
        }
    }

    private void savePendingUrl(String url, NewsSource source) {
        if (pendingUrlRepository.findByUrl(url).isEmpty()) {
            ArticlePendingUrl pending = ArticlePendingUrl.builder()
                    .url(url)
                    .newsSource(source)
                    .build();
            pendingUrlRepository.save(pending);
            logger.info("💾 Išsaugotas naujas URL: {}", url);
        } else {
            logger.debug("🔁 URL jau egzistuoja: {}", url);
        }
    }
}
