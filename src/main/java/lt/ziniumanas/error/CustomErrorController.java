package lt.ziniumanas.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;

public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null && status.toString().equals("404")) {
            return "redirect:/"; // arba į specialų 404 puslapį
        }

        return "error"; // bendrinis klaidų puslapis
    }
}
