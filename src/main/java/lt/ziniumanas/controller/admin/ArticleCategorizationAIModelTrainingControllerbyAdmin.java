package lt.ziniumanas.controller.admin;

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
        public String trainModel(@RequestBody TrainModelRequest request) {
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

    // Papildoma klasė užklausoms, kurios siunčia tekstus ir kategorijas apmokymui
    class TrainModelRequest {
        private List<String> texts;
        private List<String> labels;

        // Getteriai ir setteriai
        public List<String> getTexts() {
            return texts;
        }

        public void setTexts(List<String> texts) {
            this.texts = texts;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }
}
