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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
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
    @DisplayName("GET /api/articles - returns article list")
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
    }
}
