package lt.ziniumanas.util;

public final class ApiEndPoint {

    private ApiEndPoint() {} // Utility klasė

    public static final String API = "/api";

    // Straipsnių susiję endpointai
    public static final String ARTICLES = API + "/articles";
    public static final String ARTICLE_BY_ID = API + "/straipsnis/{id}";
    public static final String CATEGORY = API + "/kategorija/{category}";
    public static final String SEARCH = API + "/search";
    public static final String NEWS_SOURCE = API + "/news-sources";

    // Autentifikacija
    public static final String LOGIN = API + "/login";
    public static final String REGISTER = API + "/register";

    // Admin API
    public static final String ADMIN = API + "/admin";
    public static final String ADMIN_ARTICLES = ADMIN + "/articles";
    public static final String ADMIN_ARTICLE_BY_ID = ADMIN_ARTICLES + "/{id}";
}