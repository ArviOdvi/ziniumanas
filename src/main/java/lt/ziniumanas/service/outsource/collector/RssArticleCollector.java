package lt.ziniumanas.service.outsource.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.model.*;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.*;
import lt.ziniumanas.service.ai_service.AiArticleCategorizationService;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssArticleCollector implements ArticleCollector{

    private final ArticleRepository articleRepository;
    private final ArticlePendingUrlRepository pendingUrlRepository;
    private final ArticleScrapingRuleRepository scrapingRuleRepository;
    private final AiArticleCategorizationService aiService;

    @Override
    public void collectArticles() {
        List<ArticlePendingUrl> pendingUrls = pendingUrlRepository.findAll();

        for (ArticlePendingUrl pending : pendingUrls) {
            try {
                URL feedUrl = new URL(pending.getUrl());
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(feedUrl.openStream());
                NodeList items = doc.getElementsByTagName("item");

                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);
                    String title = getTextContent(item, "title");
                    String link = getTextContent(item, "link");
                    String pubDate = getTextContent(item, "pubDate");
                    String description = getTextContent(item, "description");

                    LocalDate articleDate = parsePubDate(pubDate);
                    if (!articleDate.equals(LocalDate.now())) continue;

                    boolean exists = articleRepository.findByArticleNameAndArticleDate(title, articleDate).isPresent();
                    if (!exists) {
                        Article article = Article.builder()
                                .articleName(title)
                                .articleDate(articleDate)
                                .contents(description)
                                .articleStatus(ArticleStatus.DRAFT)
                                .verificationStatus(false)
                                .newsSource(pending.getNewsSource())
                                .build();

                        aiService.assignCategory(article);
                        articleRepository.save(article);
                        log.debug("📰 Įrašytas RSS straipsnis: {}", title);
                    }
                }
            } catch (Exception e) {
                log.debug("❌ Nepavyko apdoroti RSS URL '{}': {}", pending.getUrl(), e.getMessage());
            }
        }
    }

    private String getTextContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private LocalDate parsePubDate(String rawDate) {
        try {
            return LocalDate.parse(rawDate, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (Exception e) {
            log.debug("⚠️ Nepavyko perskaityti datos '{}', naudojama šiandienos data", rawDate);
            return LocalDate.now();
        }
    }
}
