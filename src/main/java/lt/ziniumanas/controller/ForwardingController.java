package lt.ziniumanas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardingController {
    @RequestMapping("/{path:^(?!api|static|.*\\..*).*$}")
    public String redirect(@PathVariable String path) {
        return "forward:/index.html";
    }
}
