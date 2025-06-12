package lt.ziniumanas.service;
//Pagrindiniai aplikacijos veiksmai ir taisykles. Naudoja repozitorijas duomenims pasiekti ir manipuliuoti.

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.model.NewsSource;
import lt.ziniumanas.repository.NewsSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class NewsSourceService {
    private final NewsSourceRepository newsSourceRepository;

    public List<NewsSource> getAllNewsSources() {
        return newsSourceRepository.findAll();
    }

    public Optional<NewsSource> getNewsSourceById(Long id) {
        return newsSourceRepository.findById(id);
    }

    public NewsSource createNewsSource(NewsSource newsSource) {
        return newsSourceRepository.save(newsSource);
    }

    public NewsSource updateNewsSource(Long id, NewsSource updatedNewsSource) {
        return newsSourceRepository.findById(id)
                .map(newsSource -> {
                    updatedNewsSource.setId(id);
                    return newsSourceRepository.save(updatedNewsSource);
                })
                .orElse(null);
    }

    public void deleteNewsSource(Long id) {
        newsSourceRepository.deleteById(id);
    }

    // Galbūt papildoma verslo logika, susijusi su naujienų šaltiniais

}
