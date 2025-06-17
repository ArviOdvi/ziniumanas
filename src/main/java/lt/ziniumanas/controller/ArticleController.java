package lt.ziniumanas.controller;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

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
    public String getArticle(@PathVariable Long id, Model model) {
        Optional<Article> optionalArticle = articleService.findById(id);

        if (optionalArticle.isEmpty()) {
            model.addAttribute("errorMessage", "Straipsnis nerastas, ID: " + id);
            model.addAttribute("article", null);
            return "single-article";
        }
        model.addAttribute("article", optionalArticle.get());
        return "single-article";
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
