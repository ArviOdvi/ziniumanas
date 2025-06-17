package lt.ziniumanas.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.util.HttpEndpoint;
import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.service.adminservice.AdminArticleCategorizationAIModelTrainingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(HttpEndpoint.ADMIN_AI_TRAINING)
public class AdminArticleCategorizationAIModelTrainingController {

    private final AdminArticleCategorizationAIModelTrainingService trainingService;

    @GetMapping(HttpEndpoint.BASE)
    public String showTrainingPage(Model model) {
        model.addAttribute("trainingDto", new ArticleCategorizationAIModelTrainingDto());
        model.addAttribute("categories", trainingService.getValidCategories());
        long count = trainingService.getTrainingDataCount();
        model.addAttribute("count", count);
        if (count == 0) {
            model.addAttribute("message", "Treniravimo duomenų bazėje nėra įrašų.");
            model.addAttribute("messageType", "warning");
        }
        return HttpEndpoint.VIEW_AI_TRAINING;
    }

    @GetMapping(HttpEndpoint.ADMIN_AI_TRAINING_ENDING_DATA)
    public String showTrainingData(Model model) {
        model.addAttribute("trainingData", trainingService.getAllTrainingData());
        model.addAttribute("categories", trainingService.getValidCategories());
        return HttpEndpoint.VIEW_AI_TRAINING_DATA;
    }

    @GetMapping(HttpEndpoint.ADMIN_AI_TRAINING_ENDING_DATA_INFO)
    @ResponseBody
    public String trainingDataInfo() {
        long count = trainingService.getTrainingDataCount();
        return "Šiuo metu treniravimo duomenų bazėje yra " + count + " įrašų.";
    }

    @GetMapping(HttpEndpoint.ADMIN_AI_TRAINING_ENDING_METRICS)
    public String showTestMetrics(Model model) {
        try {
            Map<String, Object> metrics = trainingService.getTestMetrics();
            model.addAttribute("metrics", metrics);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return HttpEndpoint.VIEW_AI_TRAINING_METRICS;
    }

    @PostMapping(HttpEndpoint.BASE)
    public String addTrainingData(
            @Valid @ModelAttribute("trainingDto") ArticleCategorizationAIModelTrainingDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", trainingService.getValidCategories());
            model.addAttribute("message", "Neteisingi duomenys");
            model.addAttribute("messageType", "danger");
            return HttpEndpoint.VIEW_AI_TRAINING;
        }

        trainingService.handleTrainingData(dto);
        redirectAttributes.addFlashAttribute("message", "Įrašas pridėtas!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:" + HttpEndpoint.ADMIN_AI_TRAINING;
    }

    @PostMapping(HttpEndpoint.ADMIN_AI_TRAINING_ENDING_TRAIN)
    @ResponseBody
    public String trainModelDirectly() {
        try {
            trainingService.trainModel();
            return "Modelis sėkmingai ištreniruotas.";
        } catch (Exception e) {
            log.debug("Treniravimo klaida: {}", e.getMessage(), e);
            return "Klaida: " + e.getMessage();
        }
    }
}