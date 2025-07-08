import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import ArticleCard from './ArticleCard';

export default function SearchPage() {
    const location = useLocation();
    const query = new URLSearchParams(location.search).get('q');
    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!query) return;

        setLoading(true);
        fetch(`http://localhost:8080/api/search?q=${encodeURIComponent(query)}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP klaida: ${res.status}`);
                }
                return res.json();
            })
            .then(data => {
                console.log('Rasti straipsniai:', data);
                setArticles(data);
                setLoading(false);
            })
            .catch(err => {
                console.error('Klaida:', err);
                setError(err.message);
                setLoading(false);
            });
    }, [query]);

    if (loading) {
        return <div className="container mt-5">Kraunama...</div>;
    }

    if (error) {
        return <div className="container mt-5 text-danger">Klaida: {error}</div>;
    }

    return (
        <div className="container overflow-auto px-3" style={{
            marginTop: "100px",
            paddingBottom: "100px",
            maxHeight: "calc(100vh - 165px)"
        }}>
            {articles.length === 0 ? (
                <div className="alert alert-warning">Straipsnių pagal užklausą nerasta.</div>
            ) : (
                <div className="row">
                    <div className="col-md-8">
                        {articles.map(article => (
                            <div key={article.id}>
                                <ArticleCard article={article} />
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}