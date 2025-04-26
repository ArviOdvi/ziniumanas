package lt.ziniumanas.service.outsource;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.repository.NewsSourceRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class OutsourcedArticleService {
    private final ArticleRepository articleRepository;
    private final NewsSourceRepository newsSourceRepository;

    public OutsourcedArticleService(ArticleRepository articleRepository,
                                    NewsSourceRepository newsSourceRepository) {
        this.articleRepository = articleRepository;
        this.newsSourceRepository = newsSourceRepository;
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void seedTodayArticles() {
//        articleRepository.deleteAll();
//        System.out.println("🧹 Išvalyta article lentelė.");
//        List<String> todayUrls = findArticleUrls(LocalDate.now(), null);
//        todayUrls.forEach(this::processUrl);
//    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void autoCheckAndProcessArticles() {
        System.out.println("⏰ Pradedamas naujų straipsnių tikrinimas...");
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<String> recentUrls = findArticleUrls(null, thirtyMinutesAgo);
        recentUrls.forEach(this::processUrl);
    }

    private void processUrl(String url) {
        System.out.println("🚀 Apdorojamas URL: " + url);
        try {
            URL parsedUrl = java.net.URI.create(url).toURL();
            String baseUrl = parsedUrl.getProtocol() + "://" + parsedUrl.getHost();

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
                    .articleDate(LocalDate.now())
                    .articleStatus(ArticleStatus.DRAFT)
                    .verificationStatus(false)
                    .newsSource(source)
                    .build();

            articleRepository.save(article);
            System.out.println("✅ Straipsnis išsaugotas: " + title);

        } catch (MalformedURLException e) {
            System.err.println("❌ Netinkamas URL: " + url);
        } catch (Exception e) {
            System.err.println("❌ Klaida apdorojant URL: " + url + " → " + e.getMessage());
        }
    }

    /**
     * Suranda straipsnių URL'us pagal datą arba paskutinį pusvalandį.
     * @param exactDate jei nurodyta, filtruoja pagal tikslų LocalDate.
     * @param since jei nurodyta, filtruoja straipsnius paskutinio laiko bėgyje.
     * @return straipsnių URL sąrašas.
     */
    private List<String> findArticleUrls(LocalDate exactDate, LocalDateTime since) {
        List<String> urls = new ArrayList<>();
        System.out.println("⏰ Startuoja findArticleUrls()...");
        try {
            Document doc = Jsoup.connect("https://www.kurjeris.lt/").get();
            Elements postTitles = doc.select("h2.post-title");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy MM dd");
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy MM dd / HH:mm");
            System.out.println(postTitles.size() + " 'post-title' elementai rasti.");

            for (Element postTitle : postTitles) {
                Element dateElement = postTitle.previousElementSibling(); // Paimame prieš tai esantį brolišką elementą
                String url = postTitle.selectFirst("a[href]").absUrl("href"); // Iš 'h2' paimame nuorodą

                if (dateElement != null && dateElement.hasClass("post-date") && url != null && !url.isEmpty()) {
                    String dateText = dateElement.text();
                    System.out.println("Rasta data (per previousElementSibling): " + dateText + ", URL: " + url);

                    try {
                        if (since != null) {
                            LocalDateTime articleDateTime = LocalDateTime.parse(dateText, fullFormatter);
                            if (articleDateTime.isAfter(since)) {
                                urls.add(url);
                            }
                        } else if (exactDate != null) {
                            String datePart = dateText.split("/")[0].trim();
                            LocalDate articleDate = LocalDate.parse(datePart, dateFormatter);
                            if (articleDate.equals(exactDate)) {
                                urls.add(url);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Nepavyko apdoroti datos: " + dateText + " → " + e.getMessage());
                    }
                } else {
                    System.err.println("⚠️ Nerastas datos elementas prieš 'post-title' arba nerasta URL. postTitle HTML: " + postTitle.outerHtml());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Klaida ieškant straipsnių: " + e.getMessage());
        }
        System.out.println("⏰ findArticleUrls() baigė darbą. Rastų URL'ų: " + urls.size());
        return urls;
    }
}