package lt.ziniumanas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "lt.ziniumanas.repository")
public class ZiniumanasApp {

    public static void main(String[] args) {
        SpringApplication.run(ZiniumanasApp.class, args);
    }

}
