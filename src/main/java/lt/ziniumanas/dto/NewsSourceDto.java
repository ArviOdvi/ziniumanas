package lt.ziniumanas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsSourceDto {
    private Long id;
    private String sourceName;
    private String urlAddress;
}
