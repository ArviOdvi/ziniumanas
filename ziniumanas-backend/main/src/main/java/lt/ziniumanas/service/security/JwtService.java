package lt.ziniumanas.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lt.ziniumanas.model.NewsmanUser;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private byte[] secretKey;
    private static final long JWT_EXPIRATION_MS = 900000; // 15 min.

    public String createToken(NewsmanUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + JWT_EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    @PostConstruct
    protected void init() throws NoSuchAlgorithmException {
        // Generate a random secret key for HMAC SHA-512
        secretKey = KeyGenerator.getInstance("HmacSHA512").generateKey().getEncoded();
    }
}