import React, { useEffect, useState } from 'react';
import ArticleCard from './ArticleCard';

export default function ArticleList() {
    const [articles, setArticles] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        fetch('http://localhost:8080/api/articles')
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP klaida: ${res.status}`);
                }
                return res.json();
            })
            .then(data => {
                console.log('Gauti straipsniai:', data);
                setArticles(data);
                setLoading(false);
            })
            .catch(err => {
                console.error('Klaida:', err);
                setError(err.message);
                setLoading(false);
            });
    }, []);

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
            <div className="row">
                <div className="col-md-8">
                    {articles.length === 0 ? (
                        <p>Nėra straipsnių</p>
                    ) : (
                        articles.map(article => (
                            <ArticleCard key={article.id} article={article} />
                        ))
                    )}
                </div>
                <aside className="col-md-4">
                    {/* Šoninė juosta / būsimi komponentai */}
                </aside>
            </div>
        </div>
    );
}