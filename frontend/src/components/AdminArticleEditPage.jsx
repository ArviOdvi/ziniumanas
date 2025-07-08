import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

export default function AdminArticleEditPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [article, setArticle] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('token'); // Gaukite tokeną iš localStorage
        if (!token) {
            setError('Nėra autentifikacijos tokeno. Prašome prisijungti.');
            setLoading(false);
            return;
        }

        fetch(`http://localhost:8080/api/admin/articles/${id}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
            .then(res => {
                if (!res.ok) throw new Error(`Klaida kraunant straipsnį: ${res.status}`);
                return res.json();
            })
            .then(data => {
                setArticle(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, [id]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setArticle(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        if (!token) {
            setError('Nėra autentifikacijos tokeno. Prašome prisijungti.');
            return;
        }

        fetch(`http://localhost:8080/api/admin/articles/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(article)
        })
            .then(res => {
                if (!res.ok) throw new Error(`Nepavyko atnaujinti straipsnio: ${res.status}`);
                return res.json();
            })
            .then(() => {
                setSuccess('Straipsnis sėkmingai atnaujintas');
                setTimeout(() => navigate('/admin'), 1500);
            })
            .catch(err => setError(err.message));
    };

    if (loading) return <div className="container mt-5">Kraunama...</div>;
    if (error) return <div className="container mt-5 text-danger">Klaida: {error}</div>;

    return (
        <div className="container mt-5" style={{ maxWidth: '700px' }}>
            <h2 className="mb-4 text-center">Redaguoti straipsnį</h2>

            {success && <div className="alert alert-success">{success}</div>}

            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label className="form-label">Pavadinimas</label>
                    <input
                        type="text"
                        name="articleName"
                        className="form-control"
                        value={article?.articleName || ''}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="mb-3">
                    <label className="form-label">Turinys</label>
                    <textarea
                        name="contents"
                        className="form-control"
                        rows="6"
                        value={article?.contents || ''}
                        onChange={handleChange}
                        required
                    ></textarea>
                </div>

                <div className="mb-3">
                    <label className="form-label">Kategorija</label>
                    <input
                        type="text"
                        name="articleCategory"
                        className="form-control"
                        value={article?.articleCategory || ''}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="mb-3">
                    <label className="form-label">Būsena</label>
                    <input
                        type="text"
                        name="articleStatus"
                        className="form-control"
                        value={article?.articleStatus || ''}
                        onChange={handleChange}
                    />
                </div>

                <div className="mb-3">
                    <label className="form-label">Patikrinimo būsena</label>
                    <input
                        type="text"
                        name="verificationStatus"
                        className="form-control"
                        value={article?.verificationStatus || ''}
                        onChange={handleChange}
                    />
                </div>

                <button type="submit" className="btn btn-primary w-100">Išsaugoti</button>
            </form>
        </div>
    );
}