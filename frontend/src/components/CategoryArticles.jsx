import React, { useEffect, useState, useRef } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import ArticleCard from './ArticleCard'; // Importuok ArticleCard

function CategoryArticles() {
    const { category } = useParams();
    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);
    const scrollContainerRef = useRef(null); // Ref scroll'able div'ui
    const cardRefs = useRef({}); // Ref'ai kortelėms pagal ID

    useEffect(() => {
        axios.get(`/api/kategorija/${category}`)
            .then(response => {
                setArticles(response.data);
                setLoading(false);
            })
            .catch(error => {
                console.error("Klaida gaunant straipsnius:", error);
                setLoading(false);
            });
    }, [category]);

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

    if (loading) return <p>Kraunama...</p>;
    if (articles.length === 0) return <p>Nėra straipsnių kategorijoje „{category}“.</p>;

    return (
        <div
            className="container overflow-auto px-3"
            ref={scrollContainerRef}
            style={{
                marginTop: "100px",
                paddingBottom: "100px",
                maxHeight: "calc(100vh - 165px)" // Konsistentiškas su CategoryPage
            }}
        >
            <h2>Straipsniai kategorijoje: {category}</h2>
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
        </div>
    );
}

export default CategoryArticles;