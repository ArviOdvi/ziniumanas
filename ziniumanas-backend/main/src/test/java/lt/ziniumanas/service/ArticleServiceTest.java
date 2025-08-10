package lt.ziniumanas.service;

import lt.ziniumanas.dto.ArticleDto;
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
        assertEquals("Turinys 1", articleDto.getContents(), "Turinys turėtų būti 'Turinys 1'");
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
}