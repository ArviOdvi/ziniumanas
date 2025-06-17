package lt.ziniumanas.service.outsource.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.model.*;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.ArticlePendingUrlRepository;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.NewsSourceRepository;
import lt.ziniumanas.service.ai_service.AiArticleCategorizationService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;



@Slf4j
@Component
@RequiredArgsConstructor
public class RssArticleCollector implements ArticleCollector{

    private final ArticleRepository articleRepository;
    private final AiArticleCategorizationService aiService;
    private final NewsSourceRepository newsSourceRepository;

    private static final List<String> RSS_FEEDS = List.of(
            "https://kurjeris.lt/feed/"
            // Galima pridėti daugiau šaltinių čia
    );

    @Override
    public void collectArticles() {
        for (String url : RSS_FEEDS) {
            collectFromFeed(url);
        }
    }

    private void collectFromFeed(String feedUrl) {
        try {
            Document doc = Jsoup.connect(feedUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");
            log.debug("🔍 Rasta {} RSS įrašų iš {}", items.size(), feedUrl);

            for (Element item : items) {
                String title = item.selectFirst("title") != null ? item.selectFirst("title").text() : "";
                String pubDate = item.selectFirst("pubDate") != null ? item.selectFirst("pubDate").text() : "";
                String content = item.selectFirst("content|encoded") != null ?
                        item.selectFirst("content|encoded").text() :
                        item.selectFirst("description") != null ? item.selectFirst("description").text() : "";

                LocalDate articleDate = parsePubDate(pubDate);
                if (!articleDate.equals(LocalDate.now())) continue;

                boolean exists = articleRepository.findByArticleNameAndArticleDate(title, articleDate).isPresent();
                if (!exists) {
                    Article article = Article.builder()
                            .articleName(title)
                            .articleDate(articleDate)
                            .contents(content)
                            .articleStatus(ArticleStatus.DRAFT)
                            .verificationStatus(false)
                            .articleCategory("Nežinoma")
                            .newsSource(getDefaultNewsSource())
                            .build();

                    aiService.assignCategory(article);
                    articleRepository.save(article);
                    log.info("💾 RSS straipsnis įrašytas: {}", title);
                }
            }

        } catch (Exception e) {
            log.warn("❌ Nepavyko apdoroti RSS URL '{}': {}", feedUrl, e.getMessage());
        }
    }

    private LocalDate parsePubDate(String pubDate) {
        try {
            return LocalDate.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private NewsSource getDefaultNewsSource() {
        String sourceName = "Tauragės kurjeris";
        String url = "https://kurjeris.lt";

        return newsSourceRepository.findBySourceName(sourceName)
                .orElseGet(() -> {
                    NewsSource created = NewsSource.builder()
                            .sourceName(sourceName)
                            .urlAddress(url)
                            .build();
                    return newsSourceRepository.save(created);
                });
    }
}