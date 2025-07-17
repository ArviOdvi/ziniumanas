import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import 'bootstrap/dist/css/bootstrap.min.css';

export default function AdminArticleEditPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();

    const [article, setArticle] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const categories = [
        { value: 'Naujienos', label: 'Naujienos' },
        { value: 'Pasaulyje', label: 'Pasaulyje' },
        { value: 'Ekonomika', label: 'Ekonomika' },
        { value: 'Kultūra', label: 'Kultūra' },
        { value: 'Technologijos', label: 'Technologijos' },
        { value: 'Istorija', label: 'Istorija' },
        { value: 'Maistas', label: 'Maistas' },
        { value: 'Sveikata', label: 'Sveikata' },
        { value: 'Lietuvoje', label: 'Lietuvoje' },
        { value: 'Politika', label: 'Politika' },
        { value: 'Mokslas', label: 'Mokslas' },
        { value: 'Sportas', label: 'Sportas' },
        { value: 'Muzika', label: 'Muzika' },
        { value: 'Laisvalaikis', label: 'Laisvalaikis' },
        { value: 'Vaikams', label: 'Vaikams' }
    ];

    const articleStatuses = [
        { value: 'DRAFT', label: 'Juodraštis' },
        { value: 'PENDING_APPROVAL', label: 'Laukia patvirtinimo' },
        { value: 'APPROVED', label: 'Patvirtintas' },
        { value: 'PUBLISHED', label: 'Paskelbtas' },
        { value: 'ARCHIVED', label: 'Archyvuotas' },
        { value: 'REJECTED', label: 'Atmestas' }
    ];

    const verificationStatuses = [
        { value: 'TRUE', label: 'Patvirtinta' },
        { value: 'FALSE', label: 'Nepatvirtinta' }
    ];

    useEffect(() => {
        if (!user || !user.token) {
            setError('Nėra autentifikacijos tokeno. Prašome prisijungti.');
            setLoading(false);
            navigate('/login');
            return;
        }

        console.log('Siunčiama užklausa į /api/admin/articles/' + id, 'Token:', user.token);
        fetch(`http://localhost:8080/api/admin/articles/${id}`, {
            headers: {
                'Authorization': `Bearer ${user.token}`,
                'Content-Type': 'application/json'
            }
        })
            .then(res => {
                if (!res.ok) throw new Error(`Klaida kraunant straipsnį: ${res.status} ${res.statusText}`);
                return res.json();
            })
            .then(data => {
                console.log('Gauti straipsnio duomenys:', data);
                setArticle(data);
                setLoading(false);
            })
            .catch(err => {
                console.error('Klaida kraunant straipsnį:', err.message);
                setError(err.message);
                setLoading(false);
            });
    }, [id, user, navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setArticle(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!user || !user.token) {
            setError('Nėra autentifikacijos tokeno. Prašome prisijungti.');
            navigate('/login');
            return;
        }

        fetch(`http://localhost:8080/api/admin/articles/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${user.token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(article)
        })
            .then(res => {
                if (!res.ok) throw new Error(`Nepavyko atnaujinti straipsnio: ${res.status} ${res.statusText}`);
                return res.json();
            })
            .then(data => {
                console.log('Atnaujintas straipsnis:', data);
                setSuccess('Straipsnis sėkmingai atnaujintas');
                setTimeout(() => navigate('/admin'), 1500);
            })
            .catch(err => {
                console.error('Klaida atnaujinant straipsnį:', err.message);
                setError(err.message);
            });
    };

    if (loading) return <div className="container mt-5 text-center">Kraunama...</div>;
    if (error) return (
        <div className="container mt-5 text-danger text-center">
            Klaida: {error}
            <button className="btn btn-secondary mt-3" onClick={() => navigate('/admin')}>
                Grįžti
            </button>
        </div>
    );

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
                    <select
                        name="articleCategory"
                        className="form-select"
                        value={article?.articleCategory || ''}
                        onChange={handleChange}
                        required
                    >
                        <option value="">Pasirinkite kategoriją</option>
                        {categories.map(cat => (
                            <option key={cat.value} value={cat.value}>{cat.label}</option>
                        ))}
                    </select>
                </div>

                <div className="mb-3">
                    <label className="form-label">Būsena</label>
                    <select
                        name="articleStatus"
                        className="form-select"
                        value={article?.articleStatus || ''}
                        onChange={handleChange}
                        required
                    >
                        <option value="">Pasirinkite būseną</option>
                        {articleStatuses.map(status => (
                            <option key={status.value} value={status.value}>{status.label}</option>
                        ))}
                    </select>
                </div>

                <div className="mb-3">
                    <label className="form-label">Patikrinimo būsena</label>
                    <select
                        name="verificationStatus"
                        className="form-select"
                        value={article?.verificationStatus || ''}
                        onChange={handleChange}
                        required
                    >
                        <option value="">Pasirinkite patikrinimo būseną</option>
                        {verificationStatuses.map(status => (
                            <option key={status.value} value={status.value}>{status.label}</option>
                        ))}
                    </select>
                </div>

                <div className="d-flex gap-2">
                    <button type="submit" className="btn btn-primary w-100">Išsaugoti</button>
                    <button
                        type="button"
                        className="btn btn-secondary w-100"
                        onClick={() => navigate('/admin')}
                    >
                        Atšaukti
                    </button>
                </div>
            </form>
        </div>
    );
}