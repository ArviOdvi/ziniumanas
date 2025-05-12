package lt.ziniumanas.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Straipsnių valdymas: peržiūra prieš publikavimą, redagavimas, patvirtinimas, atmetimas, šalinimas.
@Controller
@RequestMapping("/admin")
public class AdminArticleController {
    @GetMapping("/articles")
    public String showArticlesAdmin(Model model) {
        model.addAttribute("title", "Straipsnių Valdymas");
        model.addAttribute("content", "admin/articles :: main"); // Nurodome fragmentą iš articles.html
        return "admin/layout";
    }
}
