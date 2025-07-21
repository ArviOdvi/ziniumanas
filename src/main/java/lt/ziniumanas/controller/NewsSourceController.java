package lt.ziniumanas.controller;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.NewsSourceDto;
import lt.ziniumanas.service.NewsSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news-sources")
public class NewsSourceController {
    private final NewsSourceService newsSourceService;

    @GetMapping
    public List<NewsSourceDto> getAllNewsSources() {
        return newsSourceService.getAllNewsSources();
    }
}

