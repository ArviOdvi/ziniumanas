package lt.ziniumanas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaRedirectConfig implements WebMvcConfigurer{
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{spring:[\\w\\-]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:[\\w\\-]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:[\\w\\-]+}/**{spring:?!(\\.js|\\.css|\\.png)$}")
                .setViewName("forward:/index.html");
    }
}
