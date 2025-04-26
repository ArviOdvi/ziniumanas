package lt.ziniumanas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZiniumanasApp {

    public static void main(String[] args) {
        SpringApplication.run(ZiniumanasApp.class, args);
    }

}
