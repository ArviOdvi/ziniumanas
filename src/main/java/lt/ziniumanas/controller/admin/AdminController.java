package lt.ziniumanas.controller.admin;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/articles")
    public String showArticlesAdmin(Model model) {
        model.addAttribute("title", "Straipsnių Valdymas");
        model.addAttribute("content", "admin/articles :: main"); // Nurodome fragmentą iš articles.html
        return "admin/layout";
    }

    @GetMapping("/scraping-rules")
    public String showScrapingRulesAdmin(Model model) {
        model.addAttribute("title", "Scraping Taisyklės");
        model.addAttribute("content", "admin/scraping-rules :: main"); // Nurodome fragmentą iš scraping-rules.html
        return "admin/layout";
    }

    @GetMapping("/ai-training")
    public String showAiTrainingAdmin(Model model) {
        model.addAttribute("title", "AI Treniravimas");
        model.addAttribute("content", "admin/ai-training :: main"); // Nurodome fragmentą iš ai-training.html
        return "admin/layout";
    }
}
