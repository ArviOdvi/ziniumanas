package lt.ziniumanas.model.outsource;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pending_article_url")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutsourceArticlePendingUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 1000)
    private String url;

    @ManyToOne
    private lt.ziniumanas.model.NewsSource newsSource;
}

