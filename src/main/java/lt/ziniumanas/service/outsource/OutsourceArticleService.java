package lt.ziniumanas.service.outsource;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.outsource.OutsourceArticlePendingUrl;
import lt.ziniumanas.model.outsource.OutsourceArticleScrapingRule;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.outsource.OutsourceArticlePendingUrlRepository;
import lt.ziniumanas.repository.outsource.OutsourceArticleScrapingRuleRepository;
import lt.ziniumanas.service.ai_service.ArticleCategorizationServicebyAI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    private final ArticleCategorizationServicebyAI articleCategorizationServicebyAI;

    public OutsourceArticleService(
            OutsourceArticlePendingUrlRepository pendingUrlRepository,
            OutsourceArticleScrapingRuleRepository scrapingRuleRepository,
            OutsourceArticlePendingUrlService pendingUrlService,
            ArticleRepository articleRepository,
            ArticleCategorizationServicebyAI articleCategorizationServicebyAI
    ) {
        this.pendingUrlRepository = pendingUrlRepository;
        this.scrapingRuleRepository = scrapingRuleRepository;
        this.pendingUrlService = pendingUrlService;
        this.articleRepository = articleRepository;
        this.articleCategorizationServicebyAI = articleCategorizationServicebyAI;
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
        if (pendingUrls.isEmpty()) {
            logger.info("ℹ️ Nėra laukiančių URL apdorojimui.");
            return;
        }

        for (OutsourceArticlePendingUrl pending : pendingUrls) {
            NewsSource source = pending.getNewsSource();
            List<OutsourceArticleScrapingRule> rules = scrapingRuleRepository.findByNewsSourceId(source.getId());

            for (OutsourceArticleScrapingRule rule : rules) {
                try {
                    Document doc = Jsoup.connect(pending.getUrl())
                            .userAgent("Mozilla/5.0")
                            .timeout(10000)
                            .get();

                    String rawTitle = doc.title();
                    String title = cleanTitle(rawTitle);
                    Element dateElement = doc.selectFirst(rule.getDateSelector());
                    if (dateElement == null) {
                        logger.warn("⚠️ Nerasta data pagal selektorių '{}' URL: {}", rule.getDateSelector(), pending.getUrl());
                        continue;
                    }

                    String dateString = dateElement.text().trim();
                    logger.debug("📅 Datos tekstas: '{}'", dateString);
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

                    String content = extractArticleContent(doc, rule, pending.getUrl());
                    if (content.isEmpty()) {
                        logger.warn("⚠️ Turinio išgauti nepavyko URL: {}", pending.getUrl());
                        continue;
                    }

                    boolean exists = articleRepository.findByArticleNameAndArticleDate(title, date).isPresent();
                    if (!exists) {
                        String category = articleCategorizationServicebyAI.categorizeArticle(content);
                        Article article = Article.builder()
                                .articleName(title)
                                .articleDate(date)
                                .articleStatus(ArticleStatus.DRAFT)
                                .verificationStatus(false)
                                .contents(content)
                                .newsSource(source)
                                .articleCategory(category)
                                .build();
                        articleRepository.save(article);
                        logger.info("💾 Išsaugotas straipsnis: {} (Kategorija: {})", title, category);
                    } else {
                        logger.debug("📑 Straipsnis '{}' jau egzistuoja, praleidžiamas", title);
                    }

                } catch (Exception e) {
                    logger.error("❌ Klaida apdorojant URL '{}': {}", pending.getUrl(), e.getMessage(), e);
                }
            }
        }
    }

    private String extractArticleContent(Document doc, OutsourceArticleScrapingRule rule, String url) {
        StringBuilder contentBuilder = new StringBuilder();

        Element containerElement = null;
        if (rule.getContentSelector() != null && !rule.getContentSelector().isEmpty()) {
            containerElement = doc.selectFirst(rule.getContentSelector());
            if (containerElement == null) {
                logger.warn("⚠️ Nerastas turinio konteineris pagal selektorių '{}' URL: {}", rule.getContentSelector(), url);
                return "";
            }
        } else {
            containerElement = doc;
        }

        if (rule.getContentSelectorSummary() != null && !rule.getContentSelectorSummary().isEmpty()) {
            Element summaryElement = containerElement.selectFirst(rule.getContentSelectorSummary());
            if (summaryElement != null) {
                contentBuilder.append(summaryElement.text().trim()).append("\n\n");
            } else {
                logger.warn("⚠️ Nerasta straipsnio santrauka pagal selektorių '{}' URL: {}", rule.getContentSelectorSummary(), url);
            }
        }

        if (rule.getContentSelectorParagraphs() != null && !rule.getContentSelectorParagraphs().isEmpty()) {
            Elements paragraphElements = containerElement.select(rule.getContentSelectorParagraphs());
            if (paragraphElements != null && !paragraphElements.isEmpty()) {
                for (Element paragraph : paragraphElements) {
                    contentBuilder.append(paragraph.text().trim()).append("\n\n");
                }
            } else {
                logger.warn("⚠️ Nerasta straipsnio pastraipos pagal selektorių '{}' URL: {}", rule.getContentSelectorParagraphs(), url);
            }
        }
        return contentBuilder.toString().trim();
    }

    private LocalDateTime parseDate(String rawDate) {
        List<String> patterns = List.of(
                "yyyy.MM.dd HH:mm",
                "yyyy.MM.dd  HH:mm",
                "yyyy.MM.ddHH:mm",
                "yyyy MM dd / HH:mm",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "dd MMMM yyyy HH:mm",
                "dd MMM yyyy HH:mm",
                "yyyy MMMM dd"
        );

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(rawDate, formatter);
            } catch (DateTimeParseException e) {
                // Ignore and try the next pattern
            }
        }
        logger.error("❗ Nepavyko konvertuoti datos su jokiu formatu: '{}'", rawDate);
        return null;
    }

    private String cleanTitle(String rawTitle) {
        String[] separators = {"\\|", "-", "–", ":"};

        for (String sep : separators) {
            if (rawTitle.contains(sep)) {
                String[] parts = rawTitle.split(sep);
                if (parts.length > 1) {
                    return parts[0].trim();
                }
            }
        }
        return rawTitle.trim();
    }
}