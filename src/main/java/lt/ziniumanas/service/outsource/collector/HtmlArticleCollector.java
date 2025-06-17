package lt.ziniumanas.service.outsource.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.model.*;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.ArticlePendingUrlRepository;
import lt.ziniumanas.repository.ArticleScrapingRuleRepository;
import lt.ziniumanas.service.ai_service.AiArticleCategorizationService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlArticleCollector implements ArticleCollector {

    private final ArticleRepository articleRepository;
    private final ArticlePendingUrlRepository pendingUrlRepository;
    private final ArticleScrapingRuleRepository scrapingRuleRepository;
    private final AiArticleCategorizationService aiService;

    @Override
    public void collectArticles() {
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
                    String title = cleanTitle(rawTitle, source.getId());

                    Element dateElement = doc.selectFirst(rule.getDateSelector());
                    if (dateElement == null) {
                        log.debug("⚠️ Data nerasta: {} ({})", pending.getUrl(), rule.getDateSelector());
                        continue;
                    }

                    LocalDateTime dateTime = parseDate(dateElement.text().trim());
                    if (dateTime == null) continue;

                    LocalDate date = dateTime.toLocalDate();
                    if (!date.equals(LocalDate.now())) continue;

                    String content = extractArticleContent(doc, rule, pending.getUrl());
                    if (content.isEmpty()) continue;

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

                        aiService.assignCategory(article);
                        articleRepository.save(article);

                        log.debug("💾 Išsaugotas HTML straipsnis: {}", title);
                    } else {
                        log.debug("🟡 Straipsnis jau egzistuoja: {}", title);
                    }

                } catch (Exception e) {
                    log.debug("❌ Klaida apdorojant HTML URL '{}': {}", pending.getUrl(), e.getMessage(), e);
                }
            }
        }
    }

    private String extractArticleContent(Document doc, ArticleScrapingRule rule, String url) {
        StringBuilder contentBuilder = new StringBuilder();
        Element container = rule.getContentSelector() != null && !rule.getContentSelector().isEmpty()
                ? doc.selectFirst(rule.getContentSelector()) : doc;

        if (container == null) {
            log.debug("⚠️ Turinio selektorius nerastas: {}", rule.getContentSelector());
            return "";
        }

        if (rule.getContentSelectorSummary() != null) {
            Element summary = container.selectFirst(rule.getContentSelectorSummary());
            if (summary != null) contentBuilder.append(summary.text().trim()).append("\n---\n");
        }

        if (rule.getContentSelectorParagraphs() != null) {
            Elements paragraphs = container.select(rule.getContentSelectorParagraphs());
            String joined = paragraphs.stream()
                    .map(e -> e.text().trim())
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.joining("\n---\n"));
            contentBuilder.append(joined);
        }

        return contentBuilder.toString().trim();
    }

    private LocalDateTime parseDate(String rawDate) {
        List<String> patterns = List.of(
                "yyyy.MM.dd HH:mm", "yyyy-MM-dd HH:mm", "dd MMMM yyyy HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss", "EEE, dd MMM yyyy HH:mm:ss Z"
        );

        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(rawDate, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {}
        }

        log.debug("❗ Nepavyko interpretuoti datos: '{}'", rawDate);
        return null;
    }

    private String cleanTitle(String rawTitle, long sourceId) {
        if (sourceId == 4 && rawTitle.length() > 20) {
            rawTitle = rawTitle.substring(0, rawTitle.length() - 20);
        }
        return rawTitle.trim();
    }
}
