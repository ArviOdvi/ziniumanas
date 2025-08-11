package lt.ziniumanas.controller;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.NewsSourceDto;
import lt.ziniumanas.service.NewsSourceService;
import lt.ziniumanas.util.ApiEndPoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NewsSourceController {
    private final NewsSourceService newsSourceService;

    @GetMapping(ApiEndPoint.NEWS_SOURCE)
    public List<NewsSourceDto> getAllNewsSources() {
        return newsSourceService.getAllNewsSources();
    }
}

