package lt.ziniumanas.controller.openpublic;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.repository.ArticleRepository;
import lt.ziniumanas.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//Straipsnių sąrašo rodymas, konkretaus straipsnio peržiūra, paieška ir pan.
@Controller
public class  ArticleController {
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

    public ArticleController(ArticleRepository articleRepository, ArticleService articleService) {
        this.articleRepository = articleRepository;
        this.articleService = articleService;
    }

    @GetMapping("/")
    public String showArticles(Model model) {
        List<Article> articles = articleRepository.findAll(); // ištraukiam visus straipsnius
        model.addAttribute("articles", articles); // pridedam į modelį
        return "index"; // šablonas bus index.html
    }
    @GetMapping("/straipsnis/{id}")
    public String showSingleArticle(@PathVariable Long id, Model model) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Straipsnis nerastas, id=" + id));
        model.addAttribute("article", article);
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
