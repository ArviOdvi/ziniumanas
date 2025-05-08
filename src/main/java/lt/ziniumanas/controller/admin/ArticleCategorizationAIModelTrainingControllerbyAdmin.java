package lt.ziniumanas.controller.admin;

import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.service.adminservice.ArticleCategorizationAIModelTrainingServicebyAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/ai-training")
@Slf4j
public class ArticleCategorizationAIModelTrainingControllerbyAdmin {
    @Autowired
    private ArticleCategorizationAIModelTrainingServicebyAdmin trainingService;

    @GetMapping
    public String showTrainingPage(Model model) {
        model.addAttribute("trainingDto", new ArticleCategorizationAIModelTrainingDto());
        return "admin/ai-training"; // atitinkamas Thymeleaf HTML šablonas
    }

    @PostMapping
    public String handleTraining(@ModelAttribute("trainingDto") ArticleCategorizationAIModelTrainingDto dto, Model model) {
        log.info("Received {} texts and {} labels",
                dto.getTexts() != null ? dto.getTexts().size() : 0,
                dto.getLabels() != null ? dto.getLabels().size() : 0);

        // Paruoškite tekstų ir etikečių žemėlapį
        Map<String, String> textsWithLabels = prepareTextsWithLabels(dto.getTexts(), dto.getLabels());

        // Išsikviečiame paslaugą modelio treniravimui
        try {
            trainingService.trainModel(dto.getTexts(), dto.getLabels());  // Nebereikia modelio išsaugojimo kelio, jis jau nurodytas konfigūracijoje
            model.addAttribute("message", "Modelio treniravimas sėkmingai užbaigtas.");
        } catch (Exception e) {
            log.error("Treniravimo klaida: ", e);
            model.addAttribute("message", "Nepavyko užbaigti modelio treniravimo.");
        }

        return "admin/ai-training"; // lieka tame pačiame puslapyje su žinute
    }

    // Papildomas metodas, kuris sukuria žemėlapį iš tekstų ir etikečių
    private Map<String, String> prepareTextsWithLabels(List<String> texts, List<String> labels) {
        Map<String, String> textsWithLabels = new HashMap<>();
        if (texts != null && labels != null && texts.size() == labels.size()) {
            for (int i = 0; i < texts.size(); i++) {
                textsWithLabels.put(labels.get(i), texts.get(i));  // Label -> Text
            }
        }
        return textsWithLabels;
    }
}