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

    @Column(name = "date_selector", nullable = false, length = 255)
    private String dateSelector;

    @Column(name = "content_selector", nullable = false, length = 255)
    private String contentSelector;

    @Column(name = "content_selector_summary", nullable = false, length = 255)
    private String contentSelectorSummary;

    @Column(name = "content_selector_paragraphs", nullable = false, length = 255)
    private String contentSelectorParagraphs;
}

