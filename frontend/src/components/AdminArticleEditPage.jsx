import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext'; // Pakeista

export default function AdminArticleEditPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth(); // Naudojame useAuth

    const [article, setArticle] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    useEffect(() => {
        if (!user || !user.token) {
            setError('Nėra autentifikacijos tokeno. Prašome prisijungti.');
            setLoading(false);
            navigate('/login');
            return;
        }

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
                console.error('Klaida:', err.message);
                setError(err.message);
                setLoading(false);
            });
    }, [id, user, navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setArticle(prev => ({ ...prev, [name]: value }));
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
            .then(() => {
                setSuccess('Straipsnis sėkmingai atnaujintas');
                setTimeout(() => navigate('/admin'), 1500);
            })
            .catch(err => {
                console.error('Klaida:', err.message);
                setError(err.message);
            });
    };

    if (loading) return <div style={{ color: 'black', fontSize: '24px', textAlign: 'center', marginTop: '20px' }}>Kraunama...</div>;
    if (error) return <div style={{ color: 'red', fontSize: '24px', textAlign: 'center', marginTop: '20px' }}>Klaida: {error}</div>;

    return (
        <div style={{ maxWidth: '700px', margin: '20px auto', padding: '20px' }}>
            <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Redaguoti straipsnį</h2>

            {success && <div style={{ backgroundColor: '#d4edda', color: '#155724', padding: '10px', marginBottom: '20px', borderRadius: '5px' }}>{success}</div>}

            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Pavadinimas</label>
                    <input
                        type="text"
                        name="articleName"
                        style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                        value={article?.articleName || ''}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Turinys</label>
                    <textarea
                        name="contents"
                        style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc', minHeight: '150px' }}
                        value={article?.contents || ''}
                        onChange={handleChange}
                        required
                    ></textarea>
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Kategorija</label>
                    <input
                        type="text"
                        name="articleCategory"
                        style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                        value={article?.articleCategory || ''}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Būsena</label>
                    <input
                        type="text"
                        name="articleStatus"
                        style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                        value={article?.articleStatus || ''}
                        onChange={handleChange}
                    />
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Patikrinimo būsena</label>
                    <input
                        type="text"
                        name="verificationStatus"
                        style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                        value={article?.verificationStatus || ''}
                        onChange={handleChange}
                    />
                </div>

                <button
                    type="submit"
                    style={{ width: '100%', padding: '10px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                    Išsaugoti
                </button>
            </form>
        </div>
    );
}