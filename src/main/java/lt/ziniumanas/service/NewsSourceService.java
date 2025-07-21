package lt.ziniumanas.service;
//Pagrindiniai aplikacijos veiksmai ir taisykles. Naudoja repozitorijas duomenims pasiekti ir manipuliuoti.

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.dto.NewsSourceDto;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.repository.NewsSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsSourceService {
    private final NewsSourceRepository newsSourceRepository;

    public List<NewsSourceDto> getAllNewsSources() {
        return newsSourceRepository.findAll().stream()
                .map(source -> new NewsSourceDto(source.getId(), source.getSourceName(), source.getUrlAddress()))
                .collect(Collectors.toList());
    }
}