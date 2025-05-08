package lt.ziniumanas.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("title", "Administracijos Skydelis");
        model.addAttribute("content", "admin/dashboard :: content"); // Fragmentas iš dashboard.html
        return "admin/layout"; // Pagrindinis išdėstymo šablonas
    }
}