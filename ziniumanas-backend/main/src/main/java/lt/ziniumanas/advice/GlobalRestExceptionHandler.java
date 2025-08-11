package lt.ziniumanas.advice;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import lt.ziniumanas.dto.ApiError;
import lt.ziniumanas.error.ArticleNotFoundException;
import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ApiError> handleLazyInitializationException(LazyInitializationException ex) {
        log.error("LazyInitializationException:", ex);
        return new ResponseEntity<>(
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Klaida kraunant duomenis: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException:", ex);
        return new ResponseEntity<>(
                new ApiError(HttpStatus.BAD_REQUEST.value(), "Neteisingi duomenys: " + ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleJwtException(JwtException ex) {
        log.error("JwtException:", ex);
        return new ResponseEntity<>(
                new ApiError(HttpStatus.UNAUTHORIZED.value(), "Netinkamas JWT tokenas: " + ex.getMessage()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException ex) {
        log.error("AuthenticationException:", ex);
        return new ResponseEntity<>(
                new ApiError(HttpStatus.UNAUTHORIZED.value(), "Nepavyko autentifikuotis: " + ex.getMessage()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("AccessDeniedException:", ex);
        return new ResponseEntity<>(
                new ApiError(HttpStatus.FORBIDDEN.value(), "Prieiga uždrausta: " + ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
        log.error("Neapdorota klaida:", ex);
        return new ResponseEntity<>(
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Serverio klaida"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.debug("Validacijos klaida: {}", errors);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Validacijos klaida");
        body.put("errors", errors);
        body.put("timestamp", java.time.LocalDateTime.now().toString());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleArticleNotFoundException(ArticleNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}