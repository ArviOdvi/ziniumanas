package lt.ziniumanas.controller.admin;

import lt.ziniumanas.service.adminservice.AdminArticleManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Straipsnių valdymas: peržiūra prieš publikavimą, redagavimas, patvirtinimas, atmetimas, šalinimas.
@Controller
@RequestMapping("/admin")
public class AdminArticleController {
    private static final Logger log = LoggerFactory.getLogger(AdminArticleController.class);
    private final AdminArticleManagementService manegementService;

    @Autowired
    public AdminArticleController(
            AdminArticleManagementService manegementService) {
        this.manegementService = manegementService;
    }

    @GetMapping("/articles")
    public String showArticlesAdmin(Model model) {
        model.addAttribute("articleData", manegementService.getAllArticles());
        return "admin/article-management";
    }
}
