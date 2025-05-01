package lt.ziniumanas.service.outsource;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.outsource.OutsourceArticleScrapingRule;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.NewsSourceRepository;
import lt.ziniumanas.repository.outsource.OutsourceArticleScrapingRuleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class OutsourceArticleService {
    private final ArticleRepository articleRepository;
    private final NewsSourceRepository newsSourceRepository;
    private final OutsourceArticleScrapingRuleRepository scrapingRuleRepository;

    private static final Logger logger = LoggerFactory.getLogger(OutsourceArticleService.class);

    public OutsourceArticleService(ArticleRepository articleRepository,
                                   NewsSourceRepository newsSourceRepository,
                                   OutsourceArticleScrapingRuleRepository scrapingRuleRepository) {
        this.articleRepository = articleRepository;
        this.newsSourceRepository = newsSourceRepository;
        this.scrapingRuleRepository = scrapingRuleRepository;
    }

    public record ArticleUrlWithDate(String url, LocalDateTime dateTime) {}

    @EventListener(ApplicationReadyEvent.class)
    public void seedTodayArticles() {
        articleRepository.deleteAll();
        logger.info("🧹 Išvalyta article lentelė.");
        List<NewsSource> sources = newsSourceRepository.findAll();
        for (NewsSource source : sources) {
            List<ArticleUrlWithDate> todayUrls = findArticleUrls(source.getSourceName(), LocalDate.now(), null);
            todayUrls.forEach(urlWithDate -> processUrl(urlWithDate.url(), urlWithDate.dateTime()));
        }
        logger.info("✅ Surinkti šios dienos straipsniai");
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Async
    public void autoCheckAndProcessArticles() {
        logger.info("⏰ Pradedamas naujų straipsnių tikrinimas...");
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<NewsSource> sources = newsSourceRepository.findAll();
        for (NewsSource source : sources) {
            List<ArticleUrlWithDate> recentUrls = findArticleUrls(source.getSourceName(), null, thirtyMinutesAgo);
            recentUrls.forEach(urlWithDate -> processUrl(urlWithDate.url(), urlWithDate.dateTime()));
        }
    }

    private void processUrl(String url, LocalDateTime articleDateTime) {
        logger.info("🚀 Apdorojamas URL: {}", url);
        try {
            URL parsedUrl = java.net.URI.create(url).toURL();
            logger.info("URL objektas: {}", parsedUrl);
            String baseUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
            logger.info("Bazinis URL: {}", baseUrl);

            Document doc = Jsoup.connect(url).get();
            String title = doc.title();

            String content = Optional.ofNullable(doc.selectFirst("article"))
                    .or(() -> Optional.ofNullable(doc.selectFirst("main")))
                    .or(() -> Optional.of(doc.body()))
                    .map(Element::text)
                    .orElse("");

            NewsSource source = newsSourceRepository.findByUrlAddressContainingIgnoreCase(baseUrl)
                    .orElseThrow(() -> new RuntimeException("⚠️ NewsSource nerastas su adresu: " + baseUrl));

            Article article = Article.builder()
                    .articleName(title)
                    .contents(content)
                    .articleDate(articleDateTime.toLocalDate())
                    .articleStatus(ArticleStatus.DRAFT)
                    .verificationStatus(false)
                    .newsSource(source)
                    .build();

            articleRepository.save(article);
            logger.info("✅ Straipsnis išsaugotas: {}", title);

        } catch (MalformedURLException e) {
            logger.error("❌ Netinkamas URL: {}", url);
        } catch (Exception e) {
            logger.error("❌ Klaida apdorojant URL: {} → {}", url, e.getMessage());
        }
    }

    /**
     * Suranda straipsnių URL'us pagal datą arba paskutinį pusvalandį.
     * @param sourceName šaltinio pavadinimas (pvz., "kurjeris.lt").
     * @param exactDate jei nurodyta, filtruoja pagal tikslų LocalDate.
     * @param since jei nurodyta, filtruoja straipsnius paskutinio laiko bėgyje.
     * @return straipsnių URL sąrašas su datomis.
     */
    @Cacheable("articleUrls")
    public List<ArticleUrlWithDate> findArticleUrls(String sourceName, LocalDate exactDate, LocalDateTime since) {
        List<ArticleUrlWithDate> urlsWithDates = new ArrayList<>();
        logger.info("⏰ Startuoja findArticleUrls() for source: {}", sourceName);

        Optional<NewsSource> newsSourceOptional = newsSourceRepository.findBySourceName(sourceName);
        if (newsSourceOptional.isEmpty()) {
            logger.error("❌ Nerastas šaltinis: {}", sourceName);
            return urlsWithDates;
        }
        NewsSource newsSource = newsSourceOptional.get(); // Gauname NewsSource objektą

        List<OutsourceArticleScrapingRule> rules = scrapingRuleRepository.findByNewsSourceId(newsSource.getId()); // Dabar getId() veiks
        if (rules.isEmpty()) {
            logger.error("❌ Nerasta taisyklių šaltiniui: {}", sourceName);
            return urlsWithDates;
        }

        Set<ArticleUrlWithDate> uniqueUrlsWithDates = new HashSet<>();

        for (OutsourceArticleScrapingRule rule : rules) {
            try {
                Document doc = Jsoup.connect(newsSource.getUrlAddress())
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();
                Elements postTitles = doc.select(rule.getTitleSelector());

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(rule.getDateFormat().split("/")[0].trim());
                DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern(rule.getDateFormat());
                logger.info("{} '{}' elementai rasti.", postTitles.size(), rule.getTitleSelector());

                for (Element postTitle : postTitles) {
                    Element linkElement = postTitle.selectFirst(rule.getUrlSelector());
                    String url = linkElement != null ? linkElement.absUrl("href") : null;

                    Element dateElement = null;
                    String dateText = null;
                    LocalDateTime articleDateTime = null;

                    if ("before".equalsIgnoreCase(rule.getDatePosition())) {
                        Element previousSibling = postTitle.previousElementSibling();
                        if (previousSibling != null && previousSibling.is(rule.getDateSelector())) {
                            dateElement = previousSibling;
                            dateText = dateElement.text();
                            logger.info("Rasta data PRIEŠ {}: {}, URL: {}", rule.getTitleSelector(), dateText, url);
                        }
                    } else if ("after".equalsIgnoreCase(rule.getDatePosition())) {
                        Element nextSibling = postTitle.nextElementSibling();
                        if (nextSibling != null && nextSibling.is("div.cf.listing-meta")) {
                            dateElement = nextSibling.selectFirst(rule.getDateSelector());
                            if (dateElement != null) {
                                dateText = dateElement.text();
                                logger.info("Rasta data PO {}: {}, URL: {}", rule.getTitleSelector(), dateText, url);
                            }
                        }
                    }

                    if (dateElement != null && url != null && !url.isEmpty()) {
                        try {
                            articleDateTime = LocalDateTime.parse(dateText, fullFormatter);
                            if (since != null) {
                                if (articleDateTime.isAfter(since)) {
                                    uniqueUrlsWithDates.add(new ArticleUrlWithDate(url, articleDateTime));
                                }
                            } else if (exactDate != null) {
                                LocalDate articleDate = LocalDate.parse(dateText.split("/")[0].trim(), dateFormatter);
                                if (articleDate.equals(exactDate)) {
                                    uniqueUrlsWithDates.add(new ArticleUrlWithDate(url, articleDateTime));
                                }
                            }
                        } catch (DateTimeParseException e) {
                            logger.error("❌ Nepavyko apdoroti datos: {} → {}", dateText, e.getMessage(), e);
                        }
                    } else {
                        logger.warn("⚠️ Nerastas datos elementas arba nuoroda. {} HTML: {}", rule.getTitleSelector(), postTitle.outerHtml());
                    }
                }
            } catch (Exception e) {
                logger.error("❌ Klaida ieškant straipsnių pagal taisyklę '{}': {}", rule.getId(), e.getMessage(), e);
            }
        }

        urlsWithDates.addAll(uniqueUrlsWithDates);
        logger.info("⏰ findArticleUrls() baigė darbą. Rastų URL'ų: {}", urlsWithDates.size());
        return urlsWithDates;
    }
}