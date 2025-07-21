import React from 'react';
import { Link } from 'react-router-dom';
import DOMPurify from 'dompurify';

export default function ArticleCard({ article }) {
    console.log('Article in ArticleCard:', article); // Derinimui
    if (!article) {
        return <div>Klaida: straipsnis nerastas</div>;
    }

    return (
        <article className="mb-4">
            <div className="text-muted small mb-1">{article. articleDate || 'Nėra datos'}</div>
            <div className="mb-4">
                <span className="badge bg-secondary">{article.sourceName || 'Nėra šaltinio'}</span>
            </div>
            <h2>
                <Link to={`/straipsnis/${article.id}`} className="text-dark text-decoration-none">
                    {article.articleName || 'Be pavadinimo'}
                </Link>
            </h2>
            <div dangerouslySetInnerHTML={{ __html: formatHtml(article.contents) }} />
            <Link to={`/straipsnis/${article.id}`} className="btn btn-sm btn-primary mt-2">
                Skaityti daugiau
            </Link>
        </article>
    );
}

function formatHtml(text) {
    const short = text?.length > 300 ? text.slice(0, 300) + '...' : text || '';
    const html = `<p>${short.replace(/---/g, '</p><p>')}</p>`;
    return DOMPurify.sanitize(html);
}