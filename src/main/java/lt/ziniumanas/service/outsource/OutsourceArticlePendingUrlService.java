package lt.ziniumanas.service.outsource;

import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.outsource.OutsourceArticleScrapingRule;
import lt.ziniumanas.model.outsource.OutsourceArticlePendingUrl;
import lt.ziniumanas.repository.NewsSourceRepository;
import lt.ziniumanas.repository.outsource.OutsourceArticleScrapingRuleRepository;
import lt.ziniumanas.repository.outsource.OutsourceArticlePendingUrlRepository;
import lt.ziniumanas.util.PendingArticleUrlTableSequenceResetUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OutsourceArticlePendingUrlService {
    private static final Logger logger = LoggerFactory.getLogger(OutsourceArticlePendingUrlService.class);

    private final NewsSourceRepository newsSourceRepository;
    private final OutsourceArticleScrapingRuleRepository scrapingRuleRepository;
    private final OutsourceArticlePendingUrlRepository pendingUrlRepository;
    private final PendingArticleUrlTableSequenceResetUtil sequenceResetUtil; // <- pridėta

    public OutsourceArticlePendingUrlService(
            NewsSourceRepository newsSourceRepository,
            OutsourceArticleScrapingRuleRepository scrapingRuleRepository,
            OutsourceArticlePendingUrlRepository pendingUrlRepository,
            PendingArticleUrlTableSequenceResetUtil sequenceResetUtil // <- injekcija
    ) {
        this.newsSourceRepository = newsSourceRepository;
        this.scrapingRuleRepository = scrapingRuleRepository;
        this.pendingUrlRepository = pendingUrlRepository;
        this.sequenceResetUtil = sequenceResetUtil;
    }

    /**
     * Bendras metodas, kuris surenka straipsnių URL iš visų šaltinių pagal jų taisykles.
     */


    /**
     * Programos užkrovimo metu surenkame straipsnių nuorodas iš visų šaltinių.
     */

    public void collectArticleUrlsOnStart() {
        logger.info("🧹 Trinami seni įrašai ir restartuojama seka...");
        pendingUrlRepository.deleteAll();
        sequenceResetUtil.resetPendingUrlSequence(); // <- restartavimas
        logger.info("🌐 Pradedamas URL surinkimas iš visų šaltinių...");
        collectUrlsFromAllSources();
        logger.info("Pradinis straipsnių URL surinkimas užbaigtas.");
    }

    /**
     * Periodinis straipsnių URL surinkimas kas 30 min.
     */
    public void scheduleCollectArticleUrls() {
        logger.info("🕒 Pusvalandinis straipsnių URL surinkimas...");
        collectUrlsFromAllSources(); // Kvietimas į bendrą metodą
        logger.info("Pusvalandinis straipsnių URL surinkimas užbaigtas.");
    }

    private void collectUrlsFromAllSources() {
        List<NewsSource> sources = newsSourceRepository.findAll();
        for (NewsSource source : sources) {
            collectArticleUrls(source); // Šis metodas apdoroja kiekvieną šaltinį
        }
    }
    /**
     * Metodas, kuris surenka straipsnių nuorodas pagal šaltinio taisykles.
     * @param source Šaltinis, kurio URL ir taisyklės bus naudojamos.
     */
    private void collectArticleUrls(NewsSource source) {
        logger.info("🔍 Tikrinamas šaltinis: {}", source.getSourceName());

        List<OutsourceArticleScrapingRule> rules = scrapingRuleRepository.findByNewsSourceId(source.getId());
        if (rules.isEmpty()) {
            logger.warn("⚠️ Nerasta scraping taisyklių šaltiniui: {}", source.getSourceName());
            return;
        }

        Set<String> foundUrls = new HashSet<>();

        for (OutsourceArticleScrapingRule rule : rules) {
            try {
                // Susiekimas su šaltiniu pagal jo URL ir HTML turinio gavimas
                Document doc = Jsoup.connect(source.getUrlAddress())
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                // Pasirenkame straipsnių pavadinimus pagal title_selector
                Elements postTitles = doc.select(rule.getTitleSelector());

                // Iteruojame per rastus pavadinimus ir ištraukime straipsnio nuorodas
                for (Element postTitle : postTitles) {
                    Element linkElement = postTitle.selectFirst("a[href]"); // Randa pirmą <a> su href
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

    /**
     * Metodas, kuris išsaugo naujas straipsnių nuorodas į duomenų bazę.
     * @param url Straipsnio URL.
     * @param source Šaltinis, iš kurio nuoroda buvo paimta.
     */
    private void savePendingUrl(String url, NewsSource source) {
        if (pendingUrlRepository.findByUrl(url).isEmpty()) {
            OutsourceArticlePendingUrl pending = OutsourceArticlePendingUrl.builder()
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
