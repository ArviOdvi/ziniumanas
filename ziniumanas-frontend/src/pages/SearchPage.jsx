import React, { useEffect, useState, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import ArticleCard from '../components/ArticleCard';

export default function SearchPage() {
    const location = useLocation();
    const query = new URLSearchParams(location.search).get('q');
    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const scrollContainerRef = useRef(null); // Ref scroll'able div'ui
    const cardRefs = useRef({}); // Ref'ai kortelėms pagal ID

    useEffect(() => {
        if (!query) {
            setLoading(false);
            return;
        }

        setLoading(true);
        fetch(`http://localhost:8080/api/search?q=${encodeURIComponent(query)}`)
            .then(res => {
                if (!res.ok) {
                    return res.text().then(text => {
                        throw new Error(text || `Klaida kraunant straipsnius: ${res.status} ${res.statusText}`);
                    });
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

    // Scroll restore prie paskutinio straipsnio
    useEffect(() => {
        if (!loading && scrollContainerRef.current && articles.length > 0) {
            const lastId = sessionStorage.getItem('lastViewedArticleId');
            if (lastId && cardRefs.current[lastId]) {
                const targetCard = cardRefs.current[lastId];
                const scrollY = targetCard.offsetTop - 50; // Offset header'iui
                scrollContainerRef.current.scrollTo({ top: scrollY, behavior: 'auto' });
                console.log('Scrolling to article ID:', lastId, 'at position:', scrollY); // Debug
                sessionStorage.removeItem('lastViewedArticleId'); // Išvalyk po restore
            }
        }
    }, [loading, articles]);

    if (loading) {
        return <div className="container mt-5">Kraunama...</div>;
    }

    if (error) {
        return <div className="container mt-5 text-danger">Klaida: {error}</div>;
    }

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
            {articles.length === 0 ? (
                <div className="alert alert-warning">Straipsnių pagal užklausą nerasta.</div>
            ) : (
                <div className="row">
                    <div className="col-md-8">
                        {articles.map(article => (
                            <div
                                key={article.id}
                                ref={(el) => (cardRefs.current[article.id] = el)} // Ref kortelei
                            >
                                <ArticleCard article={article} />
                            </div>
                        ))}
                    </div>
                    <aside className="col-md-4">{/* Sidebar, jei reikia */}</aside>
                </div>
            )}
        </div>
    );
}