package lt.ziniumanas.controller.admin;

import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.service.adminservice.ArticleCategorizationAIModelTrainingServicebyAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class ArticleCategorizationAIModelTrainingControllerbyAdmin {

    private final ArticleCategorizationAIModelTrainingServicebyAdmin modelTrainingService;

    @Autowired
    public ArticleCategorizationAIModelTrainingControllerbyAdmin(ArticleCategorizationAIModelTrainingServicebyAdmin modelTrainingService) {
        this.modelTrainingService = modelTrainingService;
    }

    // Endpointas modelio apmokymui
    @PostMapping("/train")
    public String trainModel(@RequestBody ArticleCategorizationAIModelTrainingDto request) {
        try {
            modelTrainingService.trainModel(request.getTexts(), request.getLabels());
            return "Modelis apmokytas sėkmingai.";
        } catch (Exception e) {
            return "Apmokymo klaida: " + e.getMessage();
        }
    }

    // Papildomi metodai, jei reikia atlikti kitas administravimo užduotis
    @GetMapping("/status")
    public String getStatus() {
        // Galima patikrinti modelio būseną, jei reikia
        return "Modelis veikia.";
    }
}
