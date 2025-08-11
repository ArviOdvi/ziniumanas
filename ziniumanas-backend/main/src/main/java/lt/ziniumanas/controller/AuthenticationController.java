package lt.ziniumanas.controller;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.AuthResponseDto;
import lt.ziniumanas.dto.LoginRequestDto;
import lt.ziniumanas.dto.RegisterRequestDto;
import lt.ziniumanas.service.security.AuthenticationService;
import lt.ziniumanas.util.ApiEndPoint;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authService;

    @PostMapping(ApiEndPoint.REGISTER)
    public AuthResponseDto registerUser(@RequestBody RegisterRequestDto request) {
        // Serviso kvietimas registruoti naują vartotoją
        return authService.register(request);
    }

    @PostMapping(ApiEndPoint.LOGIN)
    public AuthResponseDto loginUser(@RequestBody LoginRequestDto request) {
        // Serviso kvietimas autentifikuoti vartotoją ir generuoti tokeną
        return authService.login(request);
    }
}