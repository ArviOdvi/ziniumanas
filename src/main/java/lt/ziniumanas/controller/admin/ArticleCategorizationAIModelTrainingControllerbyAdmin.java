package lt.ziniumanas.controller.admin;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.service.adminservice.ArticleCategorizationAIModelTrainingServicebyAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/ai-training")
@Slf4j
public class ArticleCategorizationAIModelTrainingControllerbyAdmin {
    @Autowired
    private ArticleCategorizationAIModelTrainingServicebyAdmin trainingService;

    @Autowired
    private Validator validator;

    @GetMapping
    public String showTrainingPage(Model model) {
        ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
        // Užtikriname, kad sąrašai nėra null
        if (dto.getTexts() == null) {
            dto.setTexts(new ArrayList<>());
        }
        if (dto.getLabels() == null) {
            dto.setLabels(new ArrayList<>());
        }
        model.addAttribute("trainingDto", dto);
        return "admin/ai-training";
    }

    @PostMapping
    public String handleTraining(@RequestParam("texts") String[] textsInput,
                                 @RequestParam("labels") String[] labelsInput,
                                 Model model, RedirectAttributes redirectAttributes) {
        // Konvertuojame masyvus į sąrašus, pašalindami tuščius įrašus
        List<String> texts = Arrays.stream(textsInput)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        List<String> labels = Arrays.stream(labelsInput)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // Patikriname, ar sąrašai nėra tušti
        if (texts.isEmpty() || labels.isEmpty()) {
            log.warn("Tušti tekstų arba etikečių sąrašai: tekstai={}, etiketės={}", texts.size(), labels.size());
            model.addAttribute("message", "Klaida: įveskite bent vieną tekstą ir kategoriją.");
            model.addAttribute("messageType", "danger");
            ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
            dto.setTexts(new ArrayList<>());
            dto.setLabels(new ArrayList<>());
            model.addAttribute("trainingDto", dto);
            return "admin/ai-training";
        }

        // Patikriname, ar tekstų ir etikečių skaičius sutampa
        if (texts.size() != labels.size()) {
            log.warn("Tekstų ir etikečių skaičius nesutampa: {} vs {}", texts.size(), labels.size());
            model.addAttribute("message", "Klaida: tekstų ir etikečių skaičius turi sutapti.");
            model.addAttribute("messageType", "danger");
            ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
            dto.setTexts(new ArrayList<>());
            dto.setLabels(new ArrayList<>());
            model.addAttribute("trainingDto", dto);
            return "admin/ai-training";
        }

        // Sukuriame DTO ir priskiriame sąrašus
        ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
        dto.setTexts(texts);
        dto.setLabels(labels);

        // Validacija
        BindingResult result = new BeanPropertyBindingResult(dto, "trainingDto");
        Set<ConstraintViolation<ArticleCategorizationAIModelTrainingDto>> violations = validator.validate(dto);
        for (ConstraintViolation<ArticleCategorizationAIModelTrainingDto> violation : violations) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            result.addError(new FieldError("trainingDto", field, message));
        }

        if (result.hasErrors()) {
            log.warn("Validacijos klaidos: {}", result.getAllErrors());
            model.addAttribute("message", "Neteisingi įvesties duomenys. Patikrinkite formą.");
            model.addAttribute("messageType", "danger");
            model.addAttribute("trainingDto", dto);
            return "admin/ai-training";
        }

        log.info("Gauta {} tekstų ir {} etikečių", texts.size(), labels.size());

        try {
            long startTime = System.currentTimeMillis();
            trainingService.trainModel(texts, labels);
            log.info("Modelio treniravimas užbaigtas per {} ms", System.currentTimeMillis() - startTime);
            redirectAttributes.addFlashAttribute("message", "Modelio treniravimas sėkmingai užbaigtas.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalArgumentException e) {
            log.error("Neteisingi įvesties duomenys: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("message", "Klaida: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        } catch (Exception e) {
            log.error("Treniravimo klaida: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("message", "Nepavyko užbaigti modelio treniravimo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }

        return "redirect:/admin/ai-training";
    }
}