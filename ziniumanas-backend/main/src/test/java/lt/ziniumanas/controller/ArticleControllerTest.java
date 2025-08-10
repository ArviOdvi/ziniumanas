package lt.ziniumanas.controller;

import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.model.enums.ArticleStatus;
import lt.ziniumanas.model.enums.VerificationStatus;
import lt.ziniumanas.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ArticleControllerTest {
    private MockMvc mockMvc;

    @Mock
    private ArticleService articleService;

    private ArticleController articleController;

    @BeforeEach
    void setUp() {
        articleController = new ArticleController(articleService);
        mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();
    }

    @Test
    @DisplayName("GET /api/articles - grąžina straipsnių sąrašą")
    void testGetArticles() throws Exception {
        ArticleDto article1 = ArticleDto.builder()
                .id(1L)
                .articleName("Straipsnis 1")
                .contents("Turinys 1")
                .articleDate(LocalDate.of(2025, 8, 6))
                .articleStatus(ArticleStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.TRUE)
                .articleCategory("Technologijos")
                .newsSourceId(100L)
                .sourceName("15min")
                .build();

        ArticleDto article2 = ArticleDto.builder()
                .id(2L)
                .articleName("Straipsnis 2")
                .contents("Turinys 2")
                .articleDate(LocalDate.of(2025, 8, 5))
                .articleStatus(ArticleStatus.DRAFT)
                .verificationStatus(VerificationStatus.FALSE)
                .articleCategory("Sportas")
                .newsSourceId(101L)
                .sourceName("Delfi")
                .build();

        when(articleService.getAllArticles()).thenReturn(List.of(article1, article2));

        mockMvc.perform(get("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].articleName", is("Straipsnis 1")))
                .andExpect(jsonPath("$[0].articleStatus", is("PUBLISHED")))
                .andExpect(jsonPath("$[0].verificationStatus", is("TRUE")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].sourceName", is("Delfi")));

        verify(articleService).getAllArticles();
    }

    @Test
    @DisplayName("GET /api/straipsnis/{id} - grąžina straipsnį pagal ID")
    void testGetArticleById() throws Exception {
        ArticleDto article = ArticleDto.builder()
                .id(1L)
                .articleName("Straipsnis 1")
                .contents("Turinys 1")
                .articleDate(LocalDate.of(2025, 8, 6))
                .articleStatus(ArticleStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.TRUE)
                .articleCategory("Technologijos")
                .newsSourceId(100L)
                .sourceName("15min")
                .build();

        when(articleService.findById(1L)).thenReturn(Optional.of(article));

        mockMvc.perform(get("/api/straipsnis/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.articleName", is("Straipsnis 1")))
                .andExpect(jsonPath("$.contents", is("Turinys 1")))
                .andExpect(jsonPath("$.articleStatus", is("PUBLISHED")))
                .andExpect(jsonPath("$.verificationStatus", is("TRUE")))
                .andExpect(jsonPath("$.articleCategory", is("Technologijos")))
                .andExpect(jsonPath("$.sourceName", is("15min")));

        verify(articleService).findById(1L);
    }

    @Test
    @DisplayName("GET /api/straipsnis/{id} - grąžina 404, kai straipsnis nerastas")
    void testGetArticleByIdNotFound() throws Exception {
        when(articleService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/straipsnis/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(articleService).findById(999L);
    }

    @Test
    @DisplayName("GET /api/kategorija/{category} - grąžina straipsnius pagal kategoriją")
    void testGetArticlesByCategory() throws Exception {
        ArticleDto article = ArticleDto.builder()
                .id(1L)
                .articleName("Straipsnis 1")
                .contents("Turinys 1")
                .articleDate(LocalDate.of(2025, 8, 6))
                .articleStatus(ArticleStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.TRUE)
                .articleCategory("Technologijos")
                .newsSourceId(100L)
                .sourceName("15min")
                .build();

        when(articleService.getArticlesByCategory("Technologijos")).thenReturn(List.of(article));

        mockMvc.perform(get("/api/kategorija/Technologijos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].articleName", is("Straipsnis 1")))
                .andExpect(jsonPath("$[0].articleCategory", is("Technologijos")));

        verify(articleService).getArticlesByCategory("Technologijos");
    }

    @Test
    @DisplayName("GET /api/kategorija/{category} - grąžina 400, kai kategorija netinkama")
    void testGetArticlesByCategoryInvalid() throws Exception {
        mockMvc.perform(get("/api/kategorija/undefined")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(articleService, never()).getArticlesByCategory(anyString());
    }

    @Test
    @DisplayName("GET /api/kategorija/{category} - grąžina tuščią sąrašą, kai nėra straipsnių")
    void testGetArticlesByCategoryEmptyList() throws Exception {
        when(articleService.getArticlesByCategory("Technologijos")).thenReturn(List.of());

        mockMvc.perform(get("/api/kategorija/Technologijos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(articleService).getArticlesByCategory("Technologijos");
    }

    @Test
    @DisplayName("GET /api/search?q={query} - grąžina straipsnius pagal paieškos užklausą")
    void testSearchArticles() throws Exception {
        ArticleDto article = ArticleDto.builder()
                .id(1L)
                .articleName("Straipsnis apie technologijas")
                .contents("Turinys 1")
                .articleDate(LocalDate.of(2025, 8, 6))
                .articleStatus(ArticleStatus.PUBLISHED)
                .verificationStatus(VerificationStatus.TRUE)
                .articleCategory("Technologijos")
                .newsSourceId(100L)
                .sourceName("15min")
                .build();

        when(articleService.searchByQuery("technolog")).thenReturn(List.of(article));

        mockMvc.perform(get("/api/search?q=technolog")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].articleName", is("Straipsnis apie technologijas")))
                .andExpect(jsonPath("$[0].articleCategory", is("Technologijos")));

        verify(articleService).searchByQuery("technolog");
    }

    @Test
    @DisplayName("GET /api/search?q={query} - grąžina tuščią sąrašą, kai užklausa tuščia")
    void testSearchArticlesEmptyQuery() throws Exception {
        when(articleService.searchByQuery("")).thenReturn(List.of());

        mockMvc.perform(get("/api/search?q=")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(articleService).searchByQuery("");
    }

    @Test
    @DisplayName("GET /api/search?q={query} - grąžina tuščią sąrašą, kai nėra atitinkančių straipsnių")
    void testSearchArticlesNoResults() throws Exception {
        when(articleService.searchByQuery("neegzistuoja")).thenReturn(List.of());

        mockMvc.perform(get("/api/search?q=neegzistuoja")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(articleService).searchByQuery("neegzistuoja");
    }
}