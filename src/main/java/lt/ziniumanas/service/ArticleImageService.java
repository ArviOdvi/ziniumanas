package lt.ziniumanas.service;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.ArticleImage;
import lt.ziniumanas.repository.ArticleImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
public class ArticleImageService {
    private final ArticleImageRepository articleImageRepository;
    private final ArticleService articleService;

    @Autowired
    public ArticleImageService(ArticleImageRepository articleImageRepository, ArticleService articleService) {
        this.articleImageRepository = articleImageRepository;
        this.articleService = articleService;
    }

    public List<ArticleImage> getImagesByArticleId(Long articleId) {
        return articleImageRepository.findByArticleIdOrderByOrderAsc(articleId);
    }

    public Optional<ArticleImage> getImageById(Long id) {
        return articleImageRepository.findById(id);
    }

    public ArticleImage addImageToArticle(Long articleId, ArticleImage image) {
        Optional<Article> articleOptional = articleService.getArticleById(articleId);
        if (articleOptional.isPresent()) {
            image.setArticle(articleOptional.get());
            return articleImageRepository.save(image);
        }
        return null; // Arba galima mesti išimtį
    }

    public ArticleImage updateImage(Long id, ArticleImage updatedImage) {
        return articleImageRepository.findById(id)
                .map(image -> {
                    updatedImage.setId(id);
                    updatedImage.setArticle(image.getArticle()); // Išlaikome ryšį su straipsniu
                    return articleImageRepository.save(updatedImage);
                })
                .orElse(null); // Arba galima mesti išimtį
    }

    public void deleteImage(Long id) {
        articleImageRepository.deleteById(id);
    }

    @Transactional
    public void deleteImagesByArticleId(Long articleId) {
        articleImageRepository.deleteByArticleId(articleId);
    }

    // Papildoma logika paveikslėlių valdymui (pvz., tvarkymas pagal eilės numerį)
}
