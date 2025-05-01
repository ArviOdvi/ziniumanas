package lt.ziniumanas.model.outsource;

import jakarta.persistence.*;
import lombok.*;
import lt.ziniumanas.model.NewsSource;

@Entity
@Table(name = "scraping_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutsourceArticleScrapingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "news_source_id", nullable = false)
    private NewsSource newsSource;

    @Column(name = "title_selector", nullable = false, length = 255)
    private String titleSelector;

    @Column(name = "url_selector", nullable = false, length = 255)
    private String urlSelector;

    @Column(name = "date_selector", nullable = false, length = 255)
    private String dateSelector;

    @Column(name = "date_position", nullable = false, length = 50)
    private String datePosition; // "before" arba "after"

    @Column(name = "date_format", nullable = false, length = 100)
    private String dateFormat;

    @Column(name = "alternative_date_attr", length = 100)
    private String alternativeDateAttr;
}

