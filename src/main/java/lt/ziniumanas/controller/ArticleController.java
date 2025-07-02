package lt.ziniumanas.controller;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.service.ArticleService;
import lt.ziniumanas.repository.ArticleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class  ArticleController {
    private final ArticleService articleService;

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
        List<ArticleDto> articles = articleService.getArticlesByCategory(category);
        return ResponseEntity.ok(articles);
    }
}