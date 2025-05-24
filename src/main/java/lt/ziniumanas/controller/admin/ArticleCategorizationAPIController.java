package lt.ziniumanas.controller.admin;
import lt.ziniumanas.service.aiservice.ArticleCategorizationServicebyAI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/classification")
public class ArticleCategorizationAPIController {
    private final ArticleCategorizationServicebyAI categorizationService;

    @Autowired
    public ArticleCategorizationAPIController(ArticleCategorizationServicebyAI categorizationService) {
        this.categorizationService = categorizationService;
    }

    @PostMapping
    public ClassificationResponse classify(@RequestBody ClassificationRequest request) {
        String category = categorizationService.categorizeArticle(request.getText());
        return new ClassificationResponse(category);
    }

    // DTO klasės (gali būti perkeltos į atskirus failus)
    public static class ClassificationRequest {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class ClassificationResponse {
        private String category;

        public ClassificationResponse(String category) {
            this.category = category;
        }

        public String getCategory() {
            return category;
        }
    }
}
