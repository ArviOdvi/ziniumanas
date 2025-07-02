package lt.ziniumanas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "newsman_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsmanUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;
}
