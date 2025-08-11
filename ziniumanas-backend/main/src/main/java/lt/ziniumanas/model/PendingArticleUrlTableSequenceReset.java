package lt.ziniumanas.model;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PendingArticleUrlTableSequenceReset {

    private final JdbcTemplate jdbcTemplate;

    public PendingArticleUrlTableSequenceReset(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void resetPendingUrlSequence() {
        try {
            jdbcTemplate.execute("ALTER SEQUENCE public.pending_article_url_id_seq RESTART WITH 1");
            System.out.println("✅ Sekos restartavimas sėkmingas.");
        } catch (Exception e) {
            System.err.println("❌ Klaida restartuojant seką: " + e.getMessage());
        }
    }
}