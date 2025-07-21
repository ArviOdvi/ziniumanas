package lt.ziniumanas.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lt.ziniumanas.security.JwtSecretGenerator;
import lt.ziniumanas.dto.AuthResponseDto;
import lt.ziniumanas.dto.LoginRequestDto;
import lt.ziniumanas.dto.RegisterRequestDto;
import lt.ziniumanas.model.NewsmanUser;
import lt.ziniumanas.repository.NewsmanUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthenticationService {
    private final NewsmanUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtSecretGenerator jwtSecretGenerator;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final long JWT_EXPIRATION_MS = 86400000; // 24 val.

    public AuthenticationService(NewsmanUserRepository userRepo, PasswordEncoder passwordEncoder, JwtSecretGenerator jwtSecretGenerator) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecretGenerator = jwtSecretGenerator;
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        logger.info("Registering user: {}", request.getUsername());
        if (userRepo.existsByUsername(request.getUsername())) {
            logger.warn("Username already taken: {}", request.getUsername());
            throw new RuntimeException("Username already taken");
        }
        String encodedPwd = passwordEncoder.encode(request.getPassword());
        NewsmanUser user = new NewsmanUser();
        user.setUsername(request.getUsername());
        user.setPassword(encodedPwd);
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        userRepo.save(user);
        logger.info("User registered: {}", request.getUsername());
        String token = generateToken(user);
        return new AuthResponseDto(token, user.getRole(), user.getUsername());
    }

    public AuthResponseDto login(LoginRequestDto request) {
        logger.info("Attempting login for user: {}", request.getUsername());
        NewsmanUser user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getUsername());
                    return new RuntimeException("User not found");
                });
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.error("Invalid credentials for user: {}", request.getUsername());
            throw new RuntimeException("Invalid credentials");
        }
        logger.info("Login successful for user: {}", request.getUsername());
        String token = generateToken(user);
        return new AuthResponseDto(token, user.getRole(), user.getUsername());
    }

    private String generateToken(NewsmanUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + JWT_EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(jwtSecretGenerator.getJwtSecret().getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}