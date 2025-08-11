package lt.ziniumanas.model;
// Informaciją apie žinių šaltinius, iš kurių renkami straipsniai

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "news_source")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class NewsSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_address", nullable = false, length = 2048) // URL gali būti ilgas
    private String urlAddress;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

}

