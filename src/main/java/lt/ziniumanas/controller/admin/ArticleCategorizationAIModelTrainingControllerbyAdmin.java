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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/ai-training")
public class ArticleCategorizationAIModelTrainingControllerbyAdmin {
    private static final Logger log = LoggerFactory.getLogger(ArticleCategorizationAIModelTrainingControllerbyAdmin.class);

    private final ArticleCategorizationAIModelTrainingServicebyAdmin trainingService;

    @Autowired
    public ArticleCategorizationAIModelTrainingControllerbyAdmin(
            ArticleCategorizationAIModelTrainingServicebyAdmin trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    public String showTrainingPage(Model model) {
        model.addAttribute("trainingDto", new ArticleCategorizationAIModelTrainingDto());
        model.addAttribute("categories", trainingService.getValidCategories()); // Gauname kategorijas iš serviso
        return "admin/ai-training";
    }

    @GetMapping("/data-info")
    @ResponseBody
    public String trainingDataInfo() {
        long count = trainingService.getTrainingDataCount(); //iskeliam logikai i service
        return "Šiuo metu treniravimo duomenų bazėje yra " + count + " įrašų.";
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

    @PostMapping
    public String handleTraining(@Valid @ModelAttribute("trainingDto") ArticleCategorizationAIModelTrainingDto dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            log.warn("Validacijos klaidos: {}", result.getAllErrors());
            return "admin/ai-training";
        }

        try {
            trainingService.handleTrainingData(dto); //perkeliam logikai i service
            redirectAttributes.addFlashAttribute("message", "Modelio treniravimas sėkmingai užbaigtas.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalArgumentException e) { //gaudom specifinę klaidą
            log.warn("Blogi duomenys treniravimui: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/ai-training";
        } catch (Exception e) {
            log.error("Treniravimo klaida: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("message", "Nepavyko užbaigti modelio treniravimo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/ai-training";
        }

        return "redirect:/admin/ai-training";
    }
}