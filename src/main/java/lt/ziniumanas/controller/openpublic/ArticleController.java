package lt.ziniumanas.controller.openpublic;

import lt.ziniumanas.HttpEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//Straipsnių sąrašo rodymas, konkretaus straipsnio peržiūra, paieška ir pan.
@Controller
public class ArticleController {
    @GetMapping(HttpEndpoint.MAIN)
    public String openMainPage() {
        return "main";
    }

}
