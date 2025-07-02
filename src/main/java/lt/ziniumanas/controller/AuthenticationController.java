package lt.ziniumanas.controller;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.AuthResponseDto;
import lt.ziniumanas.dto.LoginRequestDto;
import lt.ziniumanas.dto.RegisterRequestDto;
import lt.ziniumanas.service.AuthenticationService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authService;

    @PostMapping("/register")
    public AuthResponseDto registerUser(@RequestBody RegisterRequestDto request) {
        // Kvieskite servisą registruoti naują vartotoją
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto loginUser(@RequestBody LoginRequestDto request) {
        // Kvieskite servisą autentifikuoti vartotoją ir generuoti tokeną
        return authService.login(request);
    }
}