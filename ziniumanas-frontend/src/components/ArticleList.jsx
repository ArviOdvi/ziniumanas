import React, { useEffect, useState, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import ArticleCard from './ArticleCard';

export default function ArticleList() {
    const [articles, setArticles] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const location = useLocation();
    const scrollContainerRef = useRef(null);
    const cardRefs = useRef({}); // Objektas ref'ams pagal article.id

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
                setArticles(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    // Naujas: Scroll prie paskutinio viewed straipsnio po loading
    useEffect(() => {
        if (!loading && scrollContainerRef.current && articles.length > 0) {
            const lastId = sessionStorage.getItem('lastViewedArticleId');
            if (lastId && cardRefs.current[lastId]) {
                const targetCard = cardRefs.current[lastId];
                const scrollY = targetCard.offsetTop - 50; // Scroll prie kortelės top'o, minus offset pvz. header'iui
                scrollContainerRef.current.scrollTo({ top: scrollY, behavior: 'auto' });
                console.log('Scrolling to article ID:', lastId, 'at position:', scrollY); // Debug
                sessionStorage.removeItem('lastViewedArticleId'); // Optional: išvalyk po restore, kad nenaudotų senos
            }
        }
    }, [loading, articles]); // Priklauso nuo articles, kad ref'ai būtų užpildyti

    if (loading) return <div className="container mt-5">Kraunama...</div>;
    if (error) return <div className="container mt-5 text-danger">Klaida: {error}</div>;

    return (
        <div
            className="container overflow-auto px-3"
            ref={scrollContainerRef}
            style={{
                marginTop: "100px",
                paddingBottom: "100px",
                maxHeight: "calc(100vh - 165px)"
            }}
        >
            <div className="row">
                <div className="col-md-8">
                    {articles.length === 0 ? (
                        <p>Nėra straipsnių</p>
                    ) : (
                        articles.map(article => (
                            <div
                                key={article.id}
                                ref={(el) => (cardRefs.current[article.id] = el)} // Pridėk ref prie kiekvienos kortelės wrapper'io
                            >
                                <ArticleCard article={article} />
                            </div>
                        ))
                    )}
                </div>
                <aside className="col-md-4">{/* Sidebar */}</aside>
            </div>
        </div>
    );
}