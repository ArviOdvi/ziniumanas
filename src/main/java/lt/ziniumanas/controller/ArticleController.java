package lt.ziniumanas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class  ArticleController {
    private final ArticleService articleService;

    @GetMapping("/articles")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ArticleDto>> getArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @GetMapping("/straipsnis/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ArticleDto> getArticleById(@PathVariable Long id) {
        return articleService.findById(id)
                .map(ArticleDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/kategorija/{category}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ArticleDto>> getArticlesByCategory(@PathVariable String category) {
        if (category == null || category.trim().isEmpty() || category.equals("undefined")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(articleService.getArticlesByCategory(category));
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ArticleDto>> searchArticles(@RequestParam String q) {
        return ResponseEntity.ok(articleService.searchByQuery(q));
    }
}