package lt.ziniumanas.controller.admin;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.model.aimodel.TrainingData;
import lt.ziniumanas.repository.airepository.TrainingDataRepository;
import lt.ziniumanas.service.adminservice.ArticleCategorizationAIModelTrainingServicebyAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ArticleCategorizationAIModelTrainingControllerbyAdmin {
    private static final Logger log = LoggerFactory.getLogger(ArticleCategorizationAIModelTrainingControllerbyAdmin.class);

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "Sportas", "Ekonomika", "Politika", "Kultūra", "Technologijos", "Sveikata",
            "Mokslas", "Istorija", "Pasaulyje", "Lietuvoje", "Vaikams", "Muzika", "Maistas"
    );

    private final ArticleCategorizationAIModelTrainingServicebyAdmin trainingService;
    private final TrainingDataRepository trainingDataRepository;
    private final Validator validator;

    @Autowired
    public ArticleCategorizationAIModelTrainingControllerbyAdmin(
            ArticleCategorizationAIModelTrainingServicebyAdmin trainingService,
            TrainingDataRepository trainingDataRepository,
            Validator validator) {
        this.trainingService = trainingService;
        this.trainingDataRepository = trainingDataRepository;
        this.validator = validator;
    }

    @GetMapping
    public String showTrainingPage(Model model) {
        ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
        if (dto.getTexts() == null) {
            dto.setTexts(new ArrayList<>());
        }
        if (dto.getLabels() == null) {
            dto.setLabels(new ArrayList<>());
        }
        model.addAttribute("trainingDto", dto);
        model.addAttribute("categories", VALID_CATEGORIES);
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
            model.addAttribute("categories", VALID_CATEGORIES);
            ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
            dto.setTexts(new ArrayList<>());
            dto.setLabels(new ArrayList<>());
            model.addAttribute("trainingDto", dto);
            return "admin/ai-training";
        }

        // Patikriname, ar tekstų ir etikečių skaičius sutampa
        if (texts.size() != labels.size()) {
            log.warn("Tekstų ir etikečių skaičius nesutampa: {} vs {}", texts.size(), labels.size());
            model.addAttribute("message", "Klaida: tekstų ir kategorijų skaičius turi sutapti.");
            model.addAttribute("messageType", "danger");
            model.addAttribute("categories", VALID_CATEGORIES);
            ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
            dto.setTexts(new ArrayList<>());
            dto.setLabels(new ArrayList<>());
            model.addAttribute("trainingDto", dto);
            return "admin/ai-training";
        }

        // Patikriname, ar kategorijos galiojančios
        for (String label : labels) {
            if (!VALID_CATEGORIES.contains(label)) {
                log.warn("Neteisinga kategorija: {}", label);
                model.addAttribute("message", "Klaida: neteisinga kategorija: " + label);
                model.addAttribute("messageType", "danger");
                model.addAttribute("categories", VALID_CATEGORIES);
                ArticleCategorizationAIModelTrainingDto dto = new ArticleCategorizationAIModelTrainingDto();
                dto.setTexts(new ArrayList<>());
                dto.setLabels(new ArrayList<>());
                model.addAttribute("trainingDto", dto);
                return "admin/ai-training";
            }
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
            model.addAttribute("categories", VALID_CATEGORIES);
            model.addAttribute("trainingDto", dto);
            return "admin/ai-training";
        }

        // Išsaugome naujus įrašus į article_categorization_training_data
        for (int i = 0; i < texts.size(); i++) {
            TrainingData data = new TrainingData();
            data.setText(texts.get(i));
            data.setCategory(labels.get(i));
            trainingDataRepository.save(data);
            log.info("Išsaugotas įrašas: tekstas='{}...', kategorija='{}'",
                    texts.get(i).substring(0, Math.min(texts.get(i).length(), 50)), labels.get(i));
        }

        // Treniruojame modelį
        try {
            long startTime = System.currentTimeMillis();
            trainingService.trainModel();
            log.info("Modelio treniravimas užbaigtas per {} ms", System.currentTimeMillis() - startTime);
            redirectAttributes.addFlashAttribute("message", "Modelio treniravimas sėkmingai užbaigtas.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalStateException e) {
            log.error("Neteisingi treniravimo duomenys: {}", e.getMessage(), e);
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