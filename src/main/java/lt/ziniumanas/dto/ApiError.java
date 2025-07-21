package lt.ziniumanas.dto;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private int status;
    private String message;
    private String timestamp;

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }

}
