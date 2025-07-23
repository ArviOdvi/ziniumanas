package lt.ziniumanas.controller.admin;

import lt.ziniumanas.dto.ArticleDto;
import lt.ziniumanas.service.admin.AdminArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static lt.ziniumanas.util.ApiEndPoint.ADMIN_ARTICLES;
import static lt.ziniumanas.util.ApiEndPoint.ADMIN_ARTICLE_BY_ID;

@RestController
@RequiredArgsConstructor
public class AdminArticleController {

    private final AdminArticleService adminArticleService;

    @GetMapping(ADMIN_ARTICLES)
    public ResponseEntity<List<ArticleDto>> getAllArticles() {
        List<ArticleDto> articles = adminArticleService.getAllArticles();
        return ResponseEntity.ok(articles);
    }

    @GetMapping(ADMIN_ARTICLE_BY_ID)
    public ResponseEntity<ArticleDto> getArticleById(@PathVariable Long id) {
        return adminArticleService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(ADMIN_ARTICLE_BY_ID)
    public ResponseEntity<ArticleDto> updateArticle(@PathVariable Long id, @RequestBody ArticleDto articleDto) {
        articleDto.setId(id);
        ArticleDto updated = adminArticleService.updateArticle(articleDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(ADMIN_ARTICLE_BY_ID)
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        adminArticleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}
