package lt.ziniumanas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scraping_rule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleScrapingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "news_source_id", nullable = false)
    private NewsSource newsSource;

    @Column(name = "title_selector", nullable = false)
    private String titleSelector;

    @Column(name = "date_selector", nullable = false)
    private String dateSelector;

    @Column(name = "content_selector", nullable = false)
    private String contentSelector;

    @Column(name = "content_selector_summary", nullable = false)
    private String contentSelectorSummary;

    @Column(name = "content_selector_paragraphs", nullable = false)
    private String contentSelectorParagraphs;
}

