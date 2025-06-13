package lt.ziniumanas.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.service.adminservice.AdminArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Straipsnių valdymas: peržiūra prieš publikavimą, redagavimas, patvirtinimas, atmetimas, šalinimas.
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminArticleController {
    private final AdminArticleService managementService;

    @GetMapping("/articles")
    public String showArticlesAdmin(Model model) {
        model.addAttribute("articleData", managementService.getAllArticles());
        return "admin/article-management";
    }
}
