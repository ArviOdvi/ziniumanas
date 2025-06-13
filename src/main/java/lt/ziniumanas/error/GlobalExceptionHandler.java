package lt.ziniumanas.error;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ArticleNotFoundException.class)
    public String handleArticleNotFound(ArticleNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("article", null); // jei šablonas tikrina article != null
        return "articles/single";
    }
}
