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
}
