package lt.ziniumanas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_image")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(length = 255)
    private String caption;

    @Column(name = "order_num") // Geresnis pavadinimas nei tiesiog "order"
    private Integer order;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

}
