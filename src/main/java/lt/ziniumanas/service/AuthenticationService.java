package lt.ziniumanas.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lt.ziniumanas.dto.AuthResponseDto;
import lt.ziniumanas.dto.LoginRequestDto;
import lt.ziniumanas.dto.RegisterRequestDto;
import lt.ziniumanas.model.NewsmanUser;
import lt.ziniumanas.repository.NewsmanUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthenticationService {
    private final NewsmanUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // JWT nustatymai (geriau būtų injectinti per config @Value)
    private static final String JWT_SECRET = "5367566859703373367639792F423F45"; // pvz., 256-bit secret
    private static final long   JWT_EXPIRATION_MS = 86400000; // 24 val. (ms)

    // Konstruktorius injekcijai
    public AuthenticationService(NewsmanUserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        // 1. Patikriname, ar vartotojas neegzistuoja
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        // 2. Užšifruojame slaptažodį prieš saugodami
        String encodedPwd = passwordEncoder.encode(request.getPassword());
        // 3. Sukuriame ir išsaugome naują User entitetą
        NewsmanUser user = new NewsmanUser();
        user.setUsername(request.getUsername());
        user.setPassword(encodedPwd);
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        userRepo.save(user);
        // 4. Generuojame JWT tokeną naujam vartotojui
        String token = generateToken(user);
        return new AuthResponseDto(token, user.getRole());
    }

    public AuthResponseDto login(LoginRequestDto request) {
        // 1. Surandame vartotoją pagal username
        NewsmanUser user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 2. Patikriname slaptažodį (raw vs encoded)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        // 3. Jei slaptažodis teisingas – generuojame JWT
        String token = generateToken(user);
        return new AuthResponseDto(token, user.getRole());
    }

    // Pagalbinis metodas JWT tokeno sukūrimui naudojant JJWT
    private String generateToken(NewsmanUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + JWT_EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(user.getUsername())              // sub: vartotojo vardas
                .claim("role", user.getRole())               // custom claim: vartotojo rolė
                .setIssuedAt(now)                            // iat: išdavimo laikas
                .setExpiration(expiry)                       // exp: galiojimo laikas
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()),
                        SignatureAlgorithm.HS256)         // pasirašome HMAC-SHA256
                .compact();
    }
}
