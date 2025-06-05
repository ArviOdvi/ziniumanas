package lt.ziniumanas.controller.admin;
import lt.ziniumanas.dto.ArticleClassificationRequest;
import lt.ziniumanas.dto.ArticleClassificationResponse;
import lt.ziniumanas.service.ai_service.AiArticleCategorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/classification")
public class ArticleCategorizationAPIController {
    private final AiArticleCategorizationService categorizationService;

    @Autowired
    public ArticleCategorizationAPIController(AiArticleCategorizationService categorizationService) {
        this.categorizationService = categorizationService;
    }

    @PostMapping
    public ArticleClassificationResponse classify(@RequestBody ArticleClassificationRequest request) {
        String category = categorizationService.categorizeArticle(request.getText());
        return new ArticleClassificationResponse(category);
    }
}
