package lt.ziniumanas.service;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.ArticleVideo;
import lt.ziniumanas.repository.ArticleVideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
public class ArticleVideoService {
    private final ArticleVideoRepository articleVideoRepository;
    private final ArticleService articleService;

    @Autowired
    public ArticleVideoService(ArticleVideoRepository articleVideoRepository, ArticleService articleService) {
        this.articleVideoRepository = articleVideoRepository;
        this.articleService = articleService;
    }

    public List<ArticleVideo> getVideosByArticleId(Long articleId) {
        return articleVideoRepository.findByArticleIdOrderByOrderAsc(articleId);
    }

    public Optional<ArticleVideo> getVideoById(Long id) {
        return articleVideoRepository.findById(id);
    }

    public ArticleVideo addVideoToArticle(Long articleId, ArticleVideo video) {
        Optional<Article> articleOptional = articleService.getArticleById(articleId);
        if (articleOptional.isPresent()) {
            video.setArticle(articleOptional.get());
            return articleVideoRepository.save(video);
        }
        return null; // Arba galima mesti išimtį
    }

    public ArticleVideo updateVideo(Long id, ArticleVideo updatedVideo) {
        return articleVideoRepository.findById(id)
                .map(video -> {
                    updatedVideo.setId(id);
                    updatedVideo.setArticle(video.getArticle()); // Išlaikome ryšį su straipsniu
                    return articleVideoRepository.save(updatedVideo);
                })
                .orElse(null); // Arba galima mesti išimtį
    }

    public void deleteVideo(Long id) {
        articleVideoRepository.deleteById(id);
    }

    @Transactional
    public void deleteVideosByArticleId(Long articleId) {
        articleVideoRepository.deleteByArticleId(articleId);
    }

    // Papildoma logika video valdymui (pvz., tvarkymas pagal eilės numerį)
}