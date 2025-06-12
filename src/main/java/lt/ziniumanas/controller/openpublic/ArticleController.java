package lt.ziniumanas.controller.openpublic;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
@RequiredArgsConstructor
//Straipsnių sąrašo rodymas, konkretaus straipsnio peržiūra, paieška ir pan.
@Controller
public class  ArticleController {
    private final ArticleService articleService;

    @GetMapping("/")
    public String showArticles(Model model) {
        List<Article> articles = articleService.getAllArticles();
        model.addAttribute("articles", articles);
        return "index";
    }
    @GetMapping("/straipsnis/{id}")
    public String showSingleArticle(@PathVariable Long id, Model model) {
        Article article = articleService.getArticleById(id);
        model.addAttribute("article", article);
        return "single-article"; // šablonas atvaizduos vieną straipsnį
    }
    @GetMapping("/kategorija/{category}")
    public String getArticlesByCategory(@PathVariable String category, Model model) {
        List<Article> articles = articleService.getArticlesByCategory(category);
        if (articles.isEmpty()) {
            return "redirect:/"; // jei nėra straipsnių – grąžina į pradžią
        }
        model.addAttribute("articles", articles);
        return "index"; // jei naudojate tą patį šabloną
    }
}
