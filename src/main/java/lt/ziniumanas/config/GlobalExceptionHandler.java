package lt.ziniumanas.config;

import io.jsonwebtoken.JwtException;
import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LazyInitializationException.class)
public ResponseEntity<String> handleLazyInitializationException(LazyInitializationException ex) {
    return new ResponseEntity<>("Klaida kraunant duomenis: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return new ResponseEntity<>("Įvyko klaida: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>("Neteisingi duomenys: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<String> handleJwtException(JwtException ex) {
        return new ResponseEntity<>("Netinkamas JWT tokenas: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}