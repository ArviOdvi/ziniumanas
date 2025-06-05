package lt.ziniumanas.controller.admin;
import lt.ziniumanas.service.adminservice.AdminScrapingRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminScrapingRuleController {

    private static final Logger log = LoggerFactory.getLogger(AdminScrapingRuleController.class);
    private final AdminScrapingRuleService scrapingRuleService;

    @Autowired
    public AdminScrapingRuleController(
            AdminScrapingRuleService scrapingRuleService) {
        this.scrapingRuleService = scrapingRuleService;
    }

    @GetMapping("/scraping-rules")
    public String showScrapingRulesAdmin(Model model) {
        model.addAttribute("scrapingRuleData", scrapingRuleService.getAllArticleScrapingRule());
        return "admin/article-scraping-rule-management";
    }
}
