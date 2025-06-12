package lt.ziniumanas.service;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.ArticleVideo;
import lt.ziniumanas.repository.ArticleVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ArticleVideoService {
    private final ArticleVideoRepository articleVideoRepository;
    private final ArticleService articleService;

    public List<ArticleVideo> getVideosByArticleId(Long articleId) {
        return articleVideoRepository.findByArticleIdOrderByOrderAsc(articleId);
    }

    public ArticleVideo getVideoById(Long id) {
        return articleVideoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Video nerastas, id = " + id));
    }

    public ArticleVideo addVideoToArticle(Long articleId, ArticleVideo video) {
        Article article = articleService.getArticleById(articleId);
        video.setArticle(article);
        return articleVideoRepository.save(video);
    }

    public ArticleVideo updateVideo(Long id, ArticleVideo updatedVideo) {
        ArticleVideo existingVideo = articleVideoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Video nerastas, id = " + id));

        updatedVideo.setId(id);
        updatedVideo.setArticle(existingVideo.getArticle()); // išlaikome ryšį su straipsniu
        return articleVideoRepository.save(updatedVideo);
    }

    public void deleteVideo(Long id) {
        if (!articleVideoRepository.existsById(id)) {
            throw new IllegalArgumentException("Video nerastas, id = " + id);
        }
        articleVideoRepository.deleteById(id);
    }

    @Transactional
    public void deleteVideosByArticleId(Long articleId) {
        articleVideoRepository.deleteByArticleId(articleId);
    }
}