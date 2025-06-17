package lt.ziniumanas.util;

public final class HttpEndpoint {

    private HttpEndpoint() {} // Kad niekas nesukurtų šios klasės instancijos

    // -----------------------------
    // BENDRI BAZINIAI KELIAI
    // -----------------------------
    public static final String BASE = "";

    public static final String HOME = "/";
    public static final String ADMIN_BASE = "/admin";

        // -----------------------------
    // ADMIN AI TRENIRAVIMO KELIAI
    // -----------------------------

    public static final String ADMIN_AI_TRAINING_ENDING = "/ai-training";
    public static final String ADMIN_AI_TRAINING_ENDING_DATA = "/data";
    public static final String ADMIN_AI_TRAINING_ENDING_DATA_INFO = "/data-info";
    public static final String ADMIN_AI_TRAINING_ENDING_METRICS = "/metrics";
    public static final String ADMIN_AI_TRAINING_ENDING_TRAIN = "/train";
    public static final String ADMIN_AI_TRAINING_BASE = ADMIN_BASE + ADMIN_AI_TRAINING_ENDING;
    public static final String ADMIN_AI_TRAINING = ADMIN_AI_TRAINING_BASE;
    public static final String ADMIN_AI_TRAINING_DATA = ADMIN_AI_TRAINING_BASE + ADMIN_AI_TRAINING_ENDING_DATA;
    public static final String ADMIN_AI_TRAINING_DATA_INFO = ADMIN_AI_TRAINING_BASE + ADMIN_AI_TRAINING_ENDING_DATA_INFO;
    public static final String ADMIN_AI_TRAINING_METRICS = ADMIN_AI_TRAINING_BASE + ADMIN_AI_TRAINING_ENDING_METRICS;
    public static final String ADMIN_AI_TRAINING_TRAIN = ADMIN_AI_TRAINING_BASE + ADMIN_AI_TRAINING_ENDING_TRAIN;

    // -----------------------------
    // ADMIN STRAIPSNIŲ VALDYMO KELIAI
    // -----------------------------
    public static final String ADMIN_ARTICLE_MANAGEMENT_PATH = "/articles";
    public static final String ADMIN_ARTICLE_MANAGEMENT = ADMIN_BASE + ADMIN_ARTICLE_MANAGEMENT_PATH;

    // -----------------------------
    // VIEW ŠABLONŲ PAVADINIMAI (be .html)
    // -----------------------------
    public static final String VIEW_AI_TRAINING = "admin/ai-training";
    public static final String VIEW_AI_TRAINING_DATA = "admin/ai-training-data";
    public static final String VIEW_AI_TRAINING_METRICS = "admin/ai-metrics";
    public static final String VIEW_ADMIN_ARTICLE_MANAGEMENT = "admin/article-management";
}