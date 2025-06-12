package lt.ziniumanas.controller.admin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.service.adminservice.AdminScrapingRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminScrapingRuleController {

    private final AdminScrapingRuleService scrapingRuleService;

    @GetMapping("/scraping-rules")
    public String showScrapingRulesAdmin(Model model) {
        model.addAttribute("scrapingRuleData", scrapingRuleService.getAllArticleScrapingRule());
        return "admin/article-scraping-rule-management";
    }
}
