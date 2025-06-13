package lt.ziniumanas.util;

public final class HttpEndpoint {

    private HttpEndpoint() {} // Kad niekas nesukurtų šios klasės instancijos

    // URL path constants

    public static final String HOME = "/";
    public static final String ADMIN_AI_TRAINING = "/admin/ai-training";
    public static final String ADMIN_AI_TRAINING_DATA = ADMIN_AI_TRAINING + "/data";
    public static final String ADMIN_AI_TRAINING_DATA_INFO = ADMIN_AI_TRAINING + "/data-info";
    public static final String ADMIN_AI_TRAINING_METRICS = ADMIN_AI_TRAINING + "/metrics";
    public static final String ADMIN_AI_TRAINING_TRAIN = ADMIN_AI_TRAINING + "/train";

    // View template names (for Thymeleaf or similar)
    public static final String VIEW_AI_TRAINING = "admin/ai-training";
    public static final String VIEW_AI_TRAINING_DATA = "admin/ai-training-data";
    public static final String VIEW_AI_TRAINING_METRICS = "admin/ai-metrics";


}
