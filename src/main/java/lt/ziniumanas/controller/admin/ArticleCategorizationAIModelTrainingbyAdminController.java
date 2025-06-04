package lt.ziniumanas.controller.admin;

import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.service.adminservice.ArticleCategorizationAIModelTrainingbyAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/ai-training")
public class ArticleCategorizationAIModelTrainingbyAdminController {
    private static final Logger log = LoggerFactory.getLogger(ArticleCategorizationAIModelTrainingbyAdminController.class);

    private final ArticleCategorizationAIModelTrainingbyAdminService trainingService;

    @Autowired
    public ArticleCategorizationAIModelTrainingbyAdminController(
            ArticleCategorizationAIModelTrainingbyAdminService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    public String showTrainingPage(Model model) {
        model.addAttribute("trainingDto", new ArticleCategorizationAIModelTrainingDto());
        model.addAttribute("categories", trainingService.getValidCategories());
        long count = trainingService.getTrainingDataCount();
        model.addAttribute("count", count);
        if (count == 0) {
            model.addAttribute("message", "Treniravimo duomenų bazėje nėra įrašų.");
            model.addAttribute("messageType", "warning");
        }
        return "admin/ai-training";
    }

    @GetMapping("/data")
    public String showTrainingData(Model model) {
        model.addAttribute("trainingData", trainingService.getAllTrainingData());
        model.addAttribute("categories", trainingService.getValidCategories());
        return "admin/ai-training-data";
    }

    @GetMapping("/data-info")
    @ResponseBody
    public String trainingDataInfo() {
        long count = trainingService.getTrainingDataCount();
        return "Šiuo metu treniravimo duomenų bazėje yra " + count + " įrašų.";
    }

    @GetMapping("/metrics")
    public String showTestMetrics(Model model) {
        try {
            Map<String, Object> metrics = trainingService.getTestMetrics();
            model.addAttribute("metrics", metrics);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "admin/ai-metrics";
    }

    @PostMapping("/add-single")
    @ResponseBody
    public ResponseEntity<String> addSingleTrainingData(
            @RequestParam("text") String text,
            @RequestParam("category") String category) {
        try {
            log.info("Gautas vienas įrašas: tekstas='{}', kategorija='{}'", text, category);
            ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
            dto.setTexts(List.of(text));
            dto.setLabels(List.of(category));
            trainingService.handleTrainingData(dto);
            return ResponseEntity.ok("Įrašas sėkmingai pridėtas.");
        } catch (IllegalArgumentException e) {
            log.warn("Blogi duomenys: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Įrašymo klaida: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Nepavyko pridėti įrašo: " + e.getMessage());
        }
    }

    @PostMapping("/train")
    @ResponseBody
    public String trainModelDirectly() {
        try {
            trainingService.trainModel();
            return "Modelis sėkmingai ištreniruotas.";
        } catch (Exception e) {
            log.error("Treniravimo klaida: {}", e.getMessage(), e);
            return "Klaida: " + e.getMessage();
        }
    }
}