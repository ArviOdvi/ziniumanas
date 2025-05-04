package lt.ziniumanas.model;
//Tinklapio straipsnio duomenys: pavadinimas, turinys, sukūrimo data, būsena, redaktoriaus patvirtinimas
import lombok.*;
import jakarta.persistence.*;
import lt.ziniumanas.model.enums.ArticleStatus;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "article")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_name", nullable = false, length = 255)
    private String articleName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contents;

    @Column(name = "article_date", nullable = false)
    private LocalDate articleDate;

    @Column(name = "article_status")
    @Enumerated(EnumType.STRING)
    private ArticleStatus articleStatus;

    @Column(name = "verification_status")
    private boolean verificationStatus;

    @Column(name = "article_category", nullable = false, length = 255)
    private String articleCategory;

    @ManyToOne
    @JoinColumn(name = "news_source_id", nullable = false)
    private NewsSource newsSource;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleImage> images;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleVideo> videos;
}
