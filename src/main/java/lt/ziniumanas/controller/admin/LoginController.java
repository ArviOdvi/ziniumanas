package lt.ziniumanas.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
        @GetMapping("/login")
        public String showLoginPage() {
            return "login"; // Grąžins login.html šabloną
        }
}
