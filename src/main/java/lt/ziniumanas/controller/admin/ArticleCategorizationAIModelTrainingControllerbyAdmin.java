package lt.ziniumanas.controller.admin;

import jakarta.validation.Valid;
import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.service.adminservice.ArticleCategorizationAIModelTrainingServicebyAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/ai-training")
@Slf4j
public class ArticleCategorizationAIModelTrainingControllerbyAdmin {
    @Autowired
    private ArticleCategorizationAIModelTrainingServicebyAdmin trainingService;

    @GetMapping
    public String showTrainingPage(Model model) {
        model.addAttribute("trainingDto", new ArticleCategorizationAIModelTrainingDto());
        model.addAttribute("message", ""); // Nustatomas tuščias message, kad išvengtume null
        return "admin/ai-training";
    }

    @PostMapping
    public String handleTraining(@Valid @ModelAttribute("trainingDto") ArticleCategorizationAIModelTrainingDto dto,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.warn("Validacijos klaidos: {}", result.getAllErrors());
            model.addAttribute("message", "Neteisingi įvesties duomenys. Patikrinkite formą.");
            return "admin/ai-training";
        }

        log.info("Gauta {} tekstų ir {} etikečių", dto.getTexts().size(), dto.getLabels().size());

        try {
            long startTime = System.currentTimeMillis();
            trainingService.trainModel(dto.getTexts(), dto.getLabels());
            log.info("Modelio treniravimas užbaigtas per {} ms", System.currentTimeMillis() - startTime);
            model.addAttribute("message", "Modelio treniravimas sėkmingai užbaigtas.");
        } catch (IllegalArgumentException e) {
            log.error("Neteisingi įvesties duomenys: {}", e.getMessage());
            model.addAttribute("message", "Klaida: " + e.getMessage());
        } catch (Exception e) {
            log.error("Treniravimo klaida: ", e);
            model.addAttribute("message", "Nepavyko užbaigti modelio treniravimo: " + e.getMessage());
        }

        return "admin/ai-training";
    }
}