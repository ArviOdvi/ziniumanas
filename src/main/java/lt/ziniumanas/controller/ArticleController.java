package lt.ziniumanas.controller;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.service.ArticleService;
import lt.ziniumanas.repository.ArticleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class  ArticleController {
    private final ArticleService articleService;
    private final ArticleRepository articleRepository;

    @GetMapping("/articles")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ArticleDto>> getArticles() {
        List<ArticleDto> articles = articleService.getAllArticles();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/straipsnis/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ArticleDto> getArticleById(@PathVariable Long id) {
        return articleService.findById(id)
                .map(article -> ResponseEntity.ok(new ArticleDto(article)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/kategorija/{category}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ArticleDto>> getArticlesByCategory(@PathVariable String category) {
        if (category == null || category.trim().isEmpty() || category.equals("undefined")) {
            return ResponseEntity.badRequest().build();
        }
        List<ArticleDto> articles = articleService.getArticlesByCategory(category);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ArticleDto>> searchArticles(@RequestParam String q) {
        List<Article> foundArticles = articleRepository.findByArticleNameContainingIgnoreCase(q);
        List<ArticleDto> result = foundArticles.stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}