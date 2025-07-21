package lt.ziniumanas.security;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.Base64;

@Getter
@Component
public class JwtSecretGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JwtSecretGenerator.class);
    private String jwtSecret;

    @PostConstruct
    public void init() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32]; // 256 bitai
        random.nextBytes(key);
        jwtSecret = Base64.getEncoder().encodeToString(key);
        logger.info("Generated JWT secret: {}", jwtSecret);
    }

}
