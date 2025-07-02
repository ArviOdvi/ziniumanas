import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

function CategoryArticles() {
    const { category } = useParams();
    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);

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

    if (loading) return <p>Kraunama...</p>;
    if (articles.length === 0) return <p>Nėra straipsnių kategorijoje „{category}“.</p>;

    return (
        <div className="container mt-4">
            <h2>Straipsniai kategorijoje: {category}</h2>
            <ul className="list-group">
                {articles.map(article => (
                    <li key={article.id} className="list-group-item">
                        <a href={`/straipsnis/${article.id}`} className="text-decoration-none">
                            <strong>{article.articleName}</strong> – {article.articleDate}
                        </a>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default CategoryArticles;