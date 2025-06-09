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
    private final ArticleService articleService;

    @Autowired
    public CommentService(CommentRepository commentRepository, ArticleService articleService) {
        this.commentRepository = commentRepository;
        this.articleService = articleService;
    }

    public List<Comment> getCommentsByArticleId(Long articleId) {
        return commentRepository.findByArticleIdAndParentCommentIsNullOrderByCreatedAtAsc(articleId);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Komentaras nerastas, id = " + id));
    }

    public Comment createComment(Long articleId, Comment comment) {
        Article article = articleService.getArticleById(articleId);
        comment.setArticle(article);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public List<Comment> getRepliesByCommentId(Long parentCommentId) {
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentCommentId);
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new IllegalArgumentException("Komentaras nerastas, id = " + id);
        }
        commentRepository.deleteById(id);
    }
}