package lt.ziniumanas.advice;

import lt.ziniumanas.exception.ArticleNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ArticleNotFoundException.class)
    public String handleArticleNotFound(ArticleNotFoundException ex) {
        return "redirect:/"; // <-- ČIA NUKREIPIAMA į pagrindinį puslapį
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404(NoHandlerFoundException ex) {
        return "redirect:/"; // <-- ČIA taip pat
    }
}

