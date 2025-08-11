import React from 'react';
import { Routes, Route } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import AdminPage from './pages/admin/AdminPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AuthProvider from './contexts/AuthContext';
import ArticleList from './components/ArticleList';
import CategoryPage from './pages/CategoryPage';
import ArticlePage from './pages/ArticlePage';
import AdminArticleEditPage from './pages/admin/AdminArticleEditPage';
import SearchPage from "./pages/SearchPage";

function App() {
    return (
        <AuthProvider>
            <Routes>
                {/* Layout su Header ir Footer */}
                <Route element={<MainLayout />}>
                    <Route path="/" element={<ArticleList />} />
                    <Route path="/login" element={<LoginPage fullPage/>} />
                    <Route path="/register" element={<RegisterPage fullPage/>} />
                    <Route path="/kategorija/:category" element={<CategoryPage />} />
                    <Route path="/straipsnis/:id" element={<ArticlePage />} />
                    <Route path="/search" element={<SearchPage />} />
                </Route>
                {/* Admin be Header ir Footer */}
                <Route path="/admin" element={<AdminPage/>} />
                <Route path="/admin/articles/:id/edit" element={<AdminArticleEditPage />} />
            </Routes>
        </AuthProvider>
    );
}

export default App;
