package lt.ziniumanas.service.outsource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.ArticlePendingUrl;
import lt.ziniumanas.model.ArticleScrapingRule;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.ArticlePendingUrlRepository;
import lt.ziniumanas.repository.ArticleScrapingRuleRepository;
import lt.ziniumanas.service.ai_service.AiArticleCategorizationService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutsourceArticleService {

    private final ArticleRepository articleRepository;
    private final ArticlePendingUrlRepository pendingUrlRepository;
    private final ArticleScrapingRuleRepository scrapingRuleRepository;
    private final AiArticleCategorizationService aiArticleCategorizationService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        log.debug("🚀 Pradedamas straipsnių apdorojimas aplikacijos paleidimo metu...");
        collectArticlesFromSources();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Async
    public void scheduledCollection() {
        log.debug("🕒 Periodinis straipsnių apdorojimas kas 30 min...");
        collectArticlesFromSources();
    }

    public void collectArticlesFromSources() {
        List<ArticlePendingUrl> pendingUrls = pendingUrlRepository.findAll();

        for (ArticlePendingUrl pending : pendingUrls) {
            NewsSource source = pending.getNewsSource();
            List<ArticleScrapingRule> rules = scrapingRuleRepository.findByNewsSourceId(source.getId());

            for (ArticleScrapingRule rule : rules) {
                try {
                    Document doc = Jsoup.connect(pending.getUrl())
                            .userAgent("Mozilla/5.0")
                            .timeout(10000)
                            .get();

                    String rawTitle = doc.title();
                    long raw_source = source.getId();
                    String title = cleanTitle(rawTitle, raw_source);

                    Element dateElement = doc.selectFirst(rule.getDateSelector());
                    if (dateElement == null) {
                        log.debug("⚠️ Nerasta data pagal selektorių '{}' URL: {}", rule.getDateSelector(), pending.getUrl());
                        continue;
                    }

                    String dateString = dateElement.text().trim();
                    LocalDateTime dateTime = parseDate(dateString);
                    if (dateTime == null) {
                        log.debug("⚠️ Nepavyko interpretuoti datos '{}' URL: {}", dateString, pending.getUrl());
                        continue;
                    }

                    LocalDate date = dateTime.toLocalDate();
                    if (!date.equals(LocalDate.now())) {
                        log.debug("⚠️ Praleista – straipsnio data {} nesutampa su šiandienos {}", date, LocalDate.now());
                        continue;
                    }

                    String content = extractArticleContent(doc, rule, pending.getUrl());
                    if (content.isEmpty()) {
                        log.debug("⚠️ Tuščias turinys URL: {}", pending.getUrl());
                        continue;
                    }

                    boolean exists = articleRepository.findByArticleNameAndArticleDate(title, date).isPresent();
                    if (!exists) {
                        Article article = Article.builder()
                                .articleName(title)
                                .articleDate(date)
                                .articleStatus(ArticleStatus.DRAFT)
                                .verificationStatus(false)
                                .contents(content)
                                .newsSource(source)
                                .build();

                        aiArticleCategorizationService.assignCategory(article);

                        try {
                            articleRepository.save(article);
                            log.debug("✅ Išsaugotas straipsnis: {} (Kategorija: {})", title, article.getArticleCategory());
                        } catch (DataIntegrityViolationException e) {
                            log.debug("⚠️ Straipsnis jau įrašytas (unikalumo apribojimas): {}", title);
                        }
                    } else {
                        log.debug("⚠️ Straipsnis '{}' jau egzistuoja, praleidžiamas", title);
                    }

                } catch (Exception e) {
                    log.debug("❌ Klaida apdorojant URL '{}': {}", pending.getUrl(), e.getMessage(), e);
                }
            }
        }
    }

    private String extractArticleContent(Document doc, ArticleScrapingRule rule, String url) {
        StringBuilder contentBuilder = new StringBuilder();
        Element containerElement = rule.getContentSelector() != null && !rule.getContentSelector().isEmpty()
                ? doc.selectFirst(rule.getContentSelector()) : doc;

        if (containerElement == null) {
            log.debug("⚠️ Nerastas turinio konteineris pagal selektorių '{}' URL: {}", rule.getContentSelector(), url);
            return "";
        }

        if (rule.getContentSelectorSummary() != null && !rule.getContentSelectorSummary().isEmpty()) {
            Element summaryElement = containerElement.selectFirst(rule.getContentSelectorSummary());
            if (summaryElement != null) {
                contentBuilder.append(summaryElement.text().trim()).append("\n---\n");
            }
        }

        if (rule.getContentSelectorParagraphs() != null && !rule.getContentSelectorParagraphs().isEmpty()) {
            Elements paragraphElements = containerElement.select(rule.getContentSelectorParagraphs());
            String paragraphs = paragraphElements.stream()
                    .map(e -> e.text().trim())
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.joining("\n---\n"));
            contentBuilder.append(paragraphs);
        }

        return contentBuilder.toString().trim();
    }

    private LocalDateTime parseDate(String rawDate) {
        List<String> patterns = List.of(
                "yyyy.MM.dd HH:mm", "yyyy.MM.dd  HH:mm", "yyyy.MM.ddHH:mm",
                "yyyy MM dd / HH:mm", "yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm",
                "yyyy-MM-dd HH:mm:ss", "dd MMMM yyyy HH:mm", "dd MMM yyyy HH:mm",
                "yyyy MMMM dd"
        );
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(rawDate, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        log.error("⚠️ Nepavyko konvertuoti datos su jokiu formatu: '{}'", rawDate);
        return null;
    }

    private String cleanTitle(String rawTitle, long source) {
        if (source == 4 && rawTitle.length() > 20) {
            rawTitle = rawTitle.substring(0, rawTitle.length() - 20);
        }
        return rawTitle.trim();
    }
}
