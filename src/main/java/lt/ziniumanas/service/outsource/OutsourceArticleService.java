package lt.ziniumanas.service.outsource;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.outsource.OutsourceArticlePendingUrl;
import lt.ziniumanas.model.outsource.OutsourceArticleScrapingRule;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.outsource.OutsourceArticlePendingUrlRepository;
import lt.ziniumanas.repository.outsource.OutsourceArticleScrapingRuleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.List;



@Service
public class OutsourceArticleService {
    private static final Logger logger = LoggerFactory.getLogger(OutsourceArticleService.class);

    private final OutsourceArticlePendingUrlRepository pendingUrlRepository;
    private final OutsourceArticleScrapingRuleRepository scrapingRuleRepository;
    private final OutsourceArticlePendingUrlService pendingUrlService;
    private final ArticleRepository articleRepository;

    public OutsourceArticleService(
            OutsourceArticlePendingUrlRepository pendingUrlRepository,
            OutsourceArticleScrapingRuleRepository scrapingRuleRepository,
            OutsourceArticlePendingUrlService pendingUrlService,
            ArticleRepository articleRepository
    ) {
        this.pendingUrlRepository = pendingUrlRepository;
        this.scrapingRuleRepository = scrapingRuleRepository;
        this.pendingUrlService = pendingUrlService;
        this.articleRepository = articleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        pendingUrlService.collectArticleUrlsOnStart();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Async
    public void processPendingUrls() {
        logger.info("🚀 Pradedamas straipsnių apdorojimas iš laukiančių URL...");
        pendingUrlService.scheduleCollectArticleUrls();

        List<OutsourceArticlePendingUrl> pendingUrls = pendingUrlRepository.findAll();
        for (OutsourceArticlePendingUrl pending : pendingUrls) {
            NewsSource source = pending.getNewsSource();
            List<OutsourceArticleScrapingRule> rules = scrapingRuleRepository.findByNewsSourceId(source.getId());

            for (OutsourceArticleScrapingRule rule : rules) {
                try {
                    Document doc = Jsoup.connect(pending.getUrl())
                            .userAgent("Mozilla/5.0")
                            .timeout(10000)
                            .get();

                    String title = doc.title();
                    Element dateElement = doc.selectFirst(rule.getDateSelector());
                    if (dateElement == null) {
                        logger.warn("⚠️ Nerasta data pagal selektorių '{}' URL: {}", rule.getDateSelector(), pending.getUrl());
                        continue;
                    }

                    String dateString = dateElement.text().trim();
                    LocalDateTime dateTime = parseDate(dateString);
                    if (dateTime == null) {
                        logger.warn("⚠️ Nepavyko interpretuoti datos '{}' URL: {}", dateString, pending.getUrl());
                        continue;
                    }

                    LocalDate date = dateTime.toLocalDate();

                    if (!date.equals(LocalDate.now())) {
                        logger.debug("📅 Praleista – straipsnio data {} nesutampa su šiandienos {}", date, LocalDate.now());
                        continue;
                    }

                    boolean exists = articleRepository.findByArticleNameAndArticleDate(title, date).isPresent();
                    if (!exists) {
                        Article article = Article.builder()
                                .articleName(title)
                                .articleDate(date)
                                .articleStatus(ArticleStatus.DRAFT)
                                .verificationStatus(false)
                                .contents(doc.body().text())
                                .newsSource(source)
                                .build();
                        articleRepository.save(article);
                        logger.info("💾 Išsaugotas straipsnis: {}", title);
                    }

                } catch (Exception e) {
                    logger.error("❌ Klaida apdorojant URL '{}': {}", pending.getUrl(), e.getMessage());
                }
            }
        }
    }

    private LocalDateTime parseDate(String rawDate) {
        List<String> patterns = List.of(
                "yyyy.MM.dd HH:mm",
                "yyyy MM dd / HH:mm",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd HH:mm"
        );

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(rawDate, formatter);
            } catch (DateTimeParseException e) {
                // Tyliai ignoruoti ir bandyti kitą
            }
        }
        logger.error("❗ Nepavyko konvertuoti datos su jokiu formatu: '{}'", rawDate);
        return null;
    }
}