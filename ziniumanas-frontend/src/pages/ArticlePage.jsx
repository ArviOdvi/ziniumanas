import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

export default function ArticlePage() {
    const { id } = useParams();
    const [article, setArticle] = useState(null);
    const [klaida, setKlaida] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`http://localhost:8080/api/straipsnis/${id}`)
            .then((res) => {
                if (!res.ok) {
                    throw new Error(`Klaida: ${res.status}`);
                }
                return res.json();
            })
            .then((data) => {
                setArticle(data);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Straipsnio gavimo klaida:", err);
                setKlaida("Straipsnio nepavyko gauti.");
                setLoading(false);
            });
    }, [id]);

    if (loading) return <div className="container mt-5">Įkeliama...</div>;
    if (klaida) return <div className="container mt-5 alert alert-danger">{klaida}</div>;
    if (!article) return <div className="container mt-5 alert alert-warning">Straipsnis nerastas.</div>;

    return (
        <div className="container overflow-auto px-3" style={{
            marginTop: "100px",
            paddingBottom: "100px",
            maxHeight: "calc(100vh - 165px)"
        }}>
            <h1 className="mb-3">{article.articleName}</h1>
            <div className="text-muted mb-2">{formatDate(article.articleDate)}</div>

            {article?.sourceName && (
                <div className="mb-3">
                    <span className="badge bg-secondary">{article.sourceName}</span>
                </div>
            )}

            {article.contents && (
                <div dangerouslySetInnerHTML={{ __html: formatHtml(article.contents) }} />
            )}

            {/* Galerija – jei yra paveikslėlių */}
            {Array.isArray(article.imageUrls) && article.imageUrls.length > 0 && (
                <div className="mt-4">
                    <h5>Paveikslėliai</h5>
                    <div className="d-flex flex-wrap gap-3">
                        {article.imageUrls.map((url, index) => (
                            <img key={index} src={url} alt={`Paveikslėlis ${index}`} style={{ maxWidth: '300px', height: 'auto' }} />
                        ))}
                    </div>
                </div>
            )}

            {/* Video */}
            {Array.isArray(article.videoUrls) && article.videoUrls.length > 0 && (
                <div className="mt-4">
                    <h5>Vaizdo įrašai</h5>
                    <div className="d-flex flex-column gap-3">
                        {article.videoUrls.map((url, index) => (
                            <video key={index} controls style={{ maxWidth: '100%' }}>
                                <source src={url} type="video/mp4" />
                                Jūsų naršyklė nepalaiko video.
                            </video>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

// Pakeičia '---' į pastraipas
function formatHtml(text) {
    if (!text) return '';
    const html = text.replace(/---/g, '</p><p>');
    return `<p>${html}</p>`;
}

// Formatuoja datą, jei ji masyvo formatu [2025, 6, 17]
function formatDate(date) {
    if (!date) return '';
    if (Array.isArray(date)) {
        const [y, m, d] = date;
        return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    }
    return date;
}