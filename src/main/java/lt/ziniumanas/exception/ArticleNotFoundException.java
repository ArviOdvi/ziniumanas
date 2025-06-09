package lt.ziniumanas.exception;

public class ArticleNotFoundException extends RuntimeException {
    public ArticleNotFoundException(Long id) {
        super("Straipsnis nerastas, ID: " + id);
    }
}
