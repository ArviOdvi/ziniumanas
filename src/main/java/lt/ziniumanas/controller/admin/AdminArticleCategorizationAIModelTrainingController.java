package lt.ziniumanas.controller.admin;

import jakarta.validation.Valid;
import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.service.adminservice.AdminArticleCategorizationAIModelTrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin/ai-training")
public class AdminArticleCategorizationAIModelTrainingController {
    private static final Logger log = LoggerFactory.getLogger(AdminArticleCategorizationAIModelTrainingController.class);

    private final AdminArticleCategorizationAIModelTrainingService trainingService;

    @Autowired
    public AdminArticleCategorizationAIModelTrainingController(
            AdminArticleCategorizationAIModelTrainingService trainingService) {
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

    @PostMapping
    public String addTrainingData(
            @Valid @ModelAttribute("trainingDto") ArticleCategorizationAIModelTrainingDto dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", trainingService.getValidCategories());
            model.addAttribute("message", "Neteisingi duomenys");
            model.addAttribute("messageType", "danger");
            return "admin/ai-training";
        }

        trainingService.handleTrainingData(dto);
        model.addAttribute("message", "Įrašas pridėtas!");
        model.addAttribute("messageType", "success");
        return "redirect:/admin/ai-training"; // arba grąžinti tą pačią formą su švariais laukais
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