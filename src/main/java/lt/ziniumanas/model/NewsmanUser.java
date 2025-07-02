package lt.ziniumanas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsmanUser {@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
    private String username;
    private String password;
    private String role;
}
