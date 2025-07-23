package lt.ziniumanas.service.security;

import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.dto.AuthResponseDto;
import lt.ziniumanas.dto.LoginRequestDto;
import lt.ziniumanas.dto.RegisterRequestDto;
import lt.ziniumanas.model.NewsmanUser;
import lt.ziniumanas.repository.NewsmanUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final NewsmanUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponseDto register(RegisterRequestDto request) {
        log.debug("Registering user: {}", request.getUsername());
        if (userRepo.existsByUsername(request.getUsername())) {
            log.debug("Username already taken: {}", request.getUsername());
            throw new RuntimeException("Username already taken");
        }
        String encodedPwd = passwordEncoder.encode(request.getPassword());
        NewsmanUser user = new NewsmanUser();
        user.setUsername(request.getUsername());
        user.setPassword(encodedPwd);
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        userRepo.save(user);
        log.debug("User registered: {}", request.getUsername());
        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token, user.getRole(), user.getUsername());
    }

    public AuthResponseDto login(LoginRequestDto request) {
        log.debug("Attempting login for user: {}", request.getUsername());
        NewsmanUser user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.debug("User not found: {}", request.getUsername());
                    return new RuntimeException("User not found");
                });
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.debug("Invalid credentials for user: {}", request.getUsername());
            throw new RuntimeException("Invalid credentials");
        }
        log.debug("Login successful for user: {}", request.getUsername());
        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token, user.getRole(), user.getUsername());
    }
}
