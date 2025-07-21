export const API_ENDPOINTS = {
    API: '/api',
    ARTICLES: '/api/articles',
    ARTICLE_BY_ID: id => `/api/straipsnis/${id}`,
    ARTICLES_BY_CATEGORY: category => `/api/kategorija/${category}`,
    SEARCH_ARTICLES: '/api/search',
    LOGIN: '/api/login',
    REGISTER: '/api/register',
    ADMIN_ARTICLES: id => `/api/admin/articles/${id}`,
};