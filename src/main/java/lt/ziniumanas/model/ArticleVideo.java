package lt.ziniumanas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_videos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_url", nullable = false, length = 2048)
    private String videoUrl;

    @Column(length = 255)
    private String caption;

    @Column(name = "order_num")
    private Integer order;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

}
