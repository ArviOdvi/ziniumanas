package lt.ziniumanas.service;

import lombok.RequiredArgsConstructor;
import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.ArticleImage;
import lt.ziniumanas.repository.ArticleImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ArticleImageService {
    private final ArticleImageRepository articleImageRepository;
    private final ArticleService articleService;

    public List<ArticleImage> getImagesByArticleId(Long articleId) {
        return articleImageRepository.findByArticleIdOrderByOrderAsc(articleId);
    }

    public ArticleImage getImageById(Long id) {
        return articleImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paveikslėlis nerastas, id = " + id));
    }

    public ArticleImage addImageToArticle(Long articleId, ArticleImage image) {
        Article article = articleService.getArticleById(articleId);
        image.setArticle(article);
        return articleImageRepository.save(image);
    }

    public ArticleImage updateImage(Long id, ArticleImage updatedImage) {
        ArticleImage existingImage = articleImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paveikslėlis nerastas, id = " + id));

        updatedImage.setId(id);
        updatedImage.setArticle(existingImage.getArticle()); // išlaikome ryšį
        return articleImageRepository.save(updatedImage);
    }

    public void deleteImage(Long id) {
        if (!articleImageRepository.existsById(id)) {
            throw new IllegalArgumentException("Paveikslėlis nerastas, id = " + id);
        }
        articleImageRepository.deleteById(id);
    }

    @Transactional
    public void deleteImagesByArticleId(Long articleId) {
        articleImageRepository.deleteByArticleId(articleId);
    }
}