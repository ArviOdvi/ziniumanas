package lt.ziniumanas.dto;

import lombok.*;
import lt.ziniumanas.model.NewsSource;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsSourceDto {
    private Long id;
    private String urlAddress;
    private String sourceName;

    public NewsSourceDto(NewsSource newsSource) {
        this.id = newsSource.getId();
        this.urlAddress = newsSource.getUrlAddress();
        this.sourceName = newsSource.getSourceName();
    }
}
