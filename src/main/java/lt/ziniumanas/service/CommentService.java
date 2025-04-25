package lt.ziniumanas.service;

import lt.ziniumanas.model.Article;
import lt.ziniumanas.model.Comment;
import lt.ziniumanas.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleService articleService; // Priklausomybė, jei reikia validuoti straipsnį

    @Autowired
    public CommentService(CommentRepository commentRepository, ArticleService articleService) {
        this.commentRepository = commentRepository;
        this.articleService = articleService;
    }

    public List<Comment> getCommentsByArticleId(Long articleId) {
        return commentRepository.findByArticleIdAndParentCommentIsNullOrderByCreatedAtAsc(articleId);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment createComment(Long articleId, Comment comment) {
        Optional<Article> articleOptional = articleService.getArticleById(articleId);
        if (articleOptional.isPresent()) {
            comment.setArticle(articleOptional.get());
            comment.setCreatedAt(LocalDateTime.now());
            return commentRepository.save(comment);
        }
        return null; // Arba galima mesti išimtį
    }

    public List<Comment> getRepliesByCommentId(Long parentCommentId) {
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentCommentId);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    // Galb8t papildoma logika komentarų valdymui
}

