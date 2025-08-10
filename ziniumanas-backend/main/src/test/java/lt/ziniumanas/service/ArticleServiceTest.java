package lt.ziniumanas.service;

import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.error.ArticleNotFoundException;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.model.enums.VerificationStatus;
import lt.ziniumanas.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {
    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService articleService;

    private Article article;
    private NewsSource newsSource;

    @BeforeEach
    void setUp() {
        newsSource = new NewsSource();
        newsSource.setId(100L);
        newsSource.setSourceName("15min");

        article = new Article();
        article.setId(1L);
        article.setArticleName("Straipsnis 1");
        article.setContents("Turinys 1");
        article.setArticleDate(LocalDate.of(2025, 8, 6));
        article.setArticleStatus(ArticleStatus.PUBLISHED);
        article.setVerificationStatus(VerificationStatus.TRUE);
        article.setArticleCategory("Technologijos");
        article.setNewsSource(newsSource);
    }

    @Test
    @DisplayName("findById - grąžina ArticleDto, kai straipsnis egzistuoja")
    void testFindByIdSuccess() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        Optional<ArticleDto> result = articleService.findById(1L);

        assertTrue(result.isPresent(), "Rezultatas turėtų būti ne tuščias");
        ArticleDto articleDto = result.get();
        assertEquals(1L, articleDto.getId(), "ID turėtų būti 1");
        assertEquals("Straipsnis 1", articleDto.getArticleName(), "Pavadinimas turėtų būti 'Straipsnis 1'");
        assertEquals("Turinys 1", articleDto.getContents(), "TurinTURINYS turėtų būti 'Turinys 1'");
        assertEquals(LocalDate.of(2025, 8, 6), articleDto.getArticleDate(), "Data turėtų būti 2025-08-06");
        assertEquals(ArticleStatus.PUBLISHED, articleDto.getArticleStatus(), "Statusas turėtų būti PUBLISHED");
        assertEquals(VerificationStatus.TRUE, articleDto.getVerificationStatus(), "Verifikacijos statusas turėtų būti TRUE");
        assertEquals("Technologijos", articleDto.getArticleCategory(), "Kategorija turėtų būti 'Technologijos'");
        assertEquals(100L, articleDto.getNewsSourceId(), "Šaltinio ID turėtų būti 100");
        assertEquals("15min", articleDto.getSourceName(), "Šaltinio pavadinimas turėtų būti '15min'");

        verify(articleRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - grąžina tuščią Optional, kai straipsnis nerastas")
    void testFindByIdNotFound() {
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ArticleDto> result = articleService.findById(999L);

        assertFalse(result.isPresent(), "Rezultatas turėtų būti tuščias");

        verify(articleRepository).findById(999L);
    }

    @Test
    @DisplayName("findById - grąžina ArticleDto su null newsSourceId, kai newsSource yra null")
    void testFindByIdWithNullNewsSource() {
        article.setNewsSource(null);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        Optional<ArticleDto> result = articleService.findById(1L);

        assertTrue(result.isPresent(), "Rezultatas turėtų būti ne tuščias");
        ArticleDto articleDto = result.get();
        assertEquals(1L, articleDto.getId(), "ID turėtų būti 1");
        assertEquals("Straipsnis 1", articleDto.getArticleName(), "Pavadinimas turėtų būti 'Straipsnis 1'");
        assertEquals("Turinys 1", articleDto.getContents(), "Turinys turėtų būti 'Turinys 1'");
        assertEquals(LocalDate.of(2025, 8, 6), articleDto.getArticleDate(), "Data turėtų būti 2025-08-06");
        assertEquals(ArticleStatus.PUBLISHED, articleDto.getArticleStatus(), "Statusas turėtų būti PUBLISHED");
        assertEquals(VerificationStatus.TRUE, articleDto.getVerificationStatus(), "Verifikacijos statusas turėtų būti TRUE");
        assertEquals("Technologijos", articleDto.getArticleCategory(), "Kategorija turėtų būti 'Technologijos'");
        assertNull(articleDto.getNewsSourceId(), "newsSourceId turėtų būti null");
        assertNull(articleDto.getSourceName(), "sourceName turėtų būti null");

        verify(articleRepository).findById(1L);
    }

    @Test
    @DisplayName("getArticleById - grąžina Article, kai straipsnis egzistuoja")
    void testGetArticleByIdSuccess() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        Article result = articleService.getArticleById(1L);

        assertNotNull(result, "Rezultatas turėtų būti ne null");
        assertEquals(1L, result.getId(), "ID turėtų būti 1");
        assertEquals("Straipsnis 1", result.getArticleName(), "Pavadinimas turėtų būti 'Straipsnis 1'");
        assertEquals("Turinys 1", result.getContents(), "Turinys turėtų būti 'Turinys 1'");
        assertEquals(LocalDate.of(2025, 8, 6), result.getArticleDate(), "Data turėtų būti 2025-08-06");
        assertEquals(ArticleStatus.PUBLISHED, result.getArticleStatus(), "Statusas turėtų būti PUBLISHED");
        assertEquals(VerificationStatus.TRUE, result.getVerificationStatus(), "Verifikacijos statusas turėtų būti TRUE");
        assertEquals("Technologijos", result.getArticleCategory(), "Kategorija turėtų būti 'Technologijos'");
        assertEquals(newsSource, result.getNewsSource(), "Šaltinis turėtų būti '15min'");

        verify(articleRepository).findById(1L);
    }

    @Test
    @DisplayName("getArticleById - meta ArticleNotFoundException, kai straipsnis nerastas")
    void testGetArticleByIdNotFound() {
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());

        ArticleNotFoundException exception = assertThrows(
                ArticleNotFoundException.class,
                () -> articleService.getArticleById(999L),
                "Turėtų būti išmesta ArticleNotFoundException"
        );

        assertEquals("Straipsnis nerastas, ID: 999", exception.getMessage(), "Išimties pranešimas turėtų atitikti");

        verify(articleRepository).findById(999L);
    }

    @Test
    @DisplayName("searchByQuery - grąžina ArticleDto sąrašą, kai straipsniai atitinka užklausą")
    void testSearchByQuerySuccess() {
        Article article2 = new Article();
        article2.setId(2L);
        article2.setArticleName("Straipsnis 2");
        article2.setContents("Turinys 2");
        article2.setArticleDate(LocalDate.of(2025, 8, 7));
        article2.setArticleStatus(ArticleStatus.PUBLISHED);
        article2.setVerificationStatus(VerificationStatus.TRUE);
        article2.setArticleCategory("Technologijos");
        article2.setNewsSource(newsSource);

        List<Article> articles = Arrays.asList(article, article2);
        when(articleRepository.findByArticleNameContainingIgnoreCaseAndVerificationStatus("straipsnis", VerificationStatus.TRUE))
                .thenReturn(articles);

        List<ArticleDto> result = articleService.searchByQuery("straipsnis");

        assertNotNull(result, "Rezultatas turėtų būti ne null");
        assertEquals(2, result.size(), "Turėtų būti grąžinti 2 straipsniai");
        assertEquals(1L, result.get(0).getId(), "Pirmo straipsnio ID turėtų būti 1");
        assertEquals("Straipsnis 1", result.get(0).getArticleName(), "Pirmo straipsnio pavadinimas turėtų būti 'Straipsnis 1'");
        assertEquals(2L, result.get(1).getId(), "Antro straipsnio ID turėtų būti 2");
        assertEquals("Straipsnis 2", result.get(1).getArticleName(), "Antro straipsnio pavadinimas turėtų būti 'Straipsnis 2'");

        verify(articleRepository).findByArticleNameContainingIgnoreCaseAndVerificationStatus("straipsnis", VerificationStatus.TRUE);
    }

    @Test
    @DisplayName("searchByQuery - grąžina tuščią sąrašą, kai straipsniai nerasti")
    void testSearchByQueryNotFound() {
        when(articleRepository.findByArticleNameContainingIgnoreCaseAndVerificationStatus("neegzistuoja", VerificationStatus.TRUE))
                .thenReturn(Collections.emptyList());

        List<ArticleDto> result = articleService.searchByQuery("neegzistuoja");

        assertNotNull(result, "Rezultatas turėtų būti ne null");
        assertTrue(result.isEmpty(), "Rezultatas turėtų būti tuščias sąrašas");

        verify(articleRepository).findByArticleNameContainingIgnoreCaseAndVerificationStatus("neegzistuoja", VerificationStatus.TRUE);
    }

    @Test
    @DisplayName("getAllArticles - grąžina ArticleDto sąrašą, kai yra publikuotų straipsnių")
    void testGetAllArticlesSuccess() {
        Article article2 = new Article();
        article2.setId(2L);
        article2.setArticleName("Straipsnis 2");
        article2.setContents("Turinys 2");
        article2.setArticleDate(LocalDate.of(2025, 8, 7));
        article2.setArticleStatus(ArticleStatus.PUBLISHED);
        article2.setVerificationStatus(VerificationStatus.TRUE);
        article2.setArticleCategory("Technologijos");
        article2.setNewsSource(newsSource);

        List<Article> articles = Arrays.asList(article, article2);
        when(articleRepository.findByArticleStatus(ArticleStatus.PUBLISHED)).thenReturn(articles);

        List<ArticleDto> result = articleService.getAllArticles();

        assertNotNull(result, "Rezultatas turėtų būti ne null");
        assertEquals(2, result.size(), "Turėtų būti grąžinti 2 straipsniai");
        assertEquals(1L, result.get(0).getId(), "Pirmo straipsnio ID turėtų būti 1");
        assertEquals("Straipsnis 1", result.get(0).getArticleName(), "Pirmo straipsnio pavadinimas turėtų būti 'Straipsnis 1'");
        assertEquals(2L, result.get(1).getId(), "Antro straipsnio ID turėtų būti 2");
        assertEquals("Straipsnis 2", result.get(1).getArticleName(), "Antro straipsnio pavadinimas turėtų būti 'Straipsnis 2'");

        verify(articleRepository).findByArticleStatus(ArticleStatus.PUBLISHED);
    }

    @Test
    @DisplayName("getAllArticles - grąžina tuščią sąrašą, kai nėra publikuotų straipsnių")
    void testGetAllArticlesEmpty() {
        when(articleRepository.findByArticleStatus(ArticleStatus.PUBLISHED)).thenReturn(Collections.emptyList());

        List<ArticleDto> result = articleService.getAllArticles();

        assertNotNull(result, "Rezultatas turėtų būti ne null");
        assertTrue(result.isEmpty(), "Rezultatas turėtų būti tuščias sąrašas");

        verify(articleRepository).findByArticleStatus(ArticleStatus.PUBLISHED);
    }

    @Test
    @DisplayName("getArticlesByCategory - grąžina ArticleDto sąrašą, kai yra publikuotų straipsnių nurodytoje kategorijoje")
    void testGetArticlesByCategorySuccess() {
        Article article2 = new Article();
        article2.setId(2L);
        article2.setArticleName("Straipsnis 2");
        article2.setContents("Turinys 2");
        article2.setArticleDate(LocalDate.of(2025, 8, 7));
        article2.setArticleStatus(ArticleStatus.PUBLISHED);
        article2.setVerificationStatus(VerificationStatus.TRUE);
        article2.setArticleCategory("Technologijos");
        article2.setNewsSource(newsSource);

        List<Article> articles = Arrays.asList(article2, article); // article2 pirmas dėl rūšiavimo pagal datą
        when(articleRepository.findByArticleCategoryIgnoreCaseAndArticleStatusOrderByArticleDateDesc("Technologijos", ArticleStatus.PUBLISHED))
                .thenReturn(articles);

        List<ArticleDto> result = articleService.getArticlesByCategory("Technologijos");

        assertNotNull(result, "Rezultatas turėtų būti ne null");
        assertEquals(2, result.size(), "Turėtų būti grąžinti 2 straipsniai");
        assertEquals(2L, result.get(0).getId(), "Pirmo straipsnio ID turėtų būti 2 (dėl rūšiavimo pagal datą)");
        assertEquals("Straipsnis 2", result.get(0).getArticleName(), "Pirmo straipsnio pavadas turėtų būti 'Straipsnis 2'");
        assertEquals(1L, result.get(1).getId(), "Antro straipsnio ID turėtų būti 1");
        assertEquals("Straipsnis 1", result.get(1).getArticleName(), "Antro straipsnio pavadinimas turėtų būti 'Straipsnis 1'");

        verify(articleRepository).findByArticleCategoryIgnoreCaseAndArticleStatusOrderByArticleDateDesc("Technologijos", ArticleStatus.PUBLISHED);
    }

    @Test
    @DisplayName("getArticlesByCategory - grąžina tuščią sąrašą, kai nėra publikuotų straipsnių nurodytoje kategorijoje")
    void testGetArticlesByCategoryEmpty() {
        when(articleRepository.findByArticleCategoryIgnoreCaseAndArticleStatusOrderByArticleDateDesc("Nekategorija", ArticleStatus.PUBLISHED))
                .thenReturn(Collections.emptyList());

        List<ArticleDto> result = articleService.getArticlesByCategory("Nekategorija");

        assertNotNull(result, "Rezultatas turėtų būti ne null");
        assertTrue(result.isEmpty(), "Rezultatas turėtų būti tuščias sąrašas");

        verify(articleRepository).findByArticleCategoryIgnoreCaseAndArticleStatusOrderByArticleDateDesc("Nekategorija", ArticleStatus.PUBLISHED);
    }
}