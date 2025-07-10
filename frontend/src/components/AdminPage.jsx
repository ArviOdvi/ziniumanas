import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import $ from 'jquery';
import 'datatables.net-bs5';
import 'datatables.net-bs5/css/dataTables.bootstrap5.min.css';
import 'bootstrap/dist/css/bootstrap.min.css'; // Pridėta, bet galima pašalinti, jei importuojama globaliai
import './AdminPage.css';
import { useAuth } from '../contexts/AuthContext';

export default function AdminPage() {
    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const tableRef = useRef();
    const navigate = useNavigate();
    const { user } = useAuth();

    useEffect(() => {
        if (!user || !user.token) {
            setError('Nėra autentifikacijos tokeno. Prašome prisijungti.');
            setLoading(false);
            navigate('/login');
            return;
        }

        console.log('Siunčiama užklausa į /api/admin/articles', 'Token:', user.token);
        fetch('http://localhost:8080/api/admin/articles', {
            headers: {
                'Authorization': `Bearer ${user.token}`,
                'Content-Type': 'application/json'
            }
        })
            .then(res => {
                if (!res.ok) {
                    throw new Error(`Klaida kraunant duomenis: ${res.status} ${res.statusText}`);
                }
                return res.json();
            })
            .then(data => {
                console.log('Gauti straipsniai:', data);
                setArticles(data);
                setLoading(false);
            })
            .catch(err => {
                console.error('Klaida kraunant straipsnius:', err.message);
                setError(err.message);
                setLoading(false);
            });
    }, [user, navigate]);

    useEffect(() => {
        if (!loading && articles.length > 0) {
            $(tableRef.current).DataTable({
                destroy: true,
                pageLength: 15,
                scrollY: '500px',
                scrollCollapse: true,
                language: {
                    lengthMenu: 'Rodyti _MENU_ įrašų per puslapį',
                    zeroRecords: 'Nėra straipsnių',
                    info: 'Rodomi _START_ iki _END_ iš _TOTAL_ įrašų',
                    infoEmpty: 'Nėra įrašų',
                    infoFiltered: '(filtruota iš _MAX_ įrašų)',
                    search: 'Ieškoti:',
                    paginate: {
                        first: 'Pirmas',
                        last: 'Paskutinis',
                        next: 'Kitas',
                        previous: 'Ankstesnis'
                    }
                }
            });
        }
    }, [loading, articles]);

    if (loading) return <div className="container mt-5 text-center">Kraunama...</div>;
    if (error) return (
        <div className="container mt-5 text-danger text-center">
            Klaida: {error}
            <button
                className="btn btn-secondary mt-3"
                onClick={() => window.location.reload()}
            >
                Bandyti dar kartą
            </button>
        </div>
    );

    return (
        <div className="container mt-5" style={{ maxWidth: '1200px' }}>
            <h2 className="mb-4 text-center">Straipsnių Duomenys</h2>
            <p className="text-center mb-4">Rodomi visi straipsnių įrašai.</p>

            <div className="table-responsive">
                <table ref={tableRef} className="table table-striped table-bordered table-hover">
                    <thead className="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Pavadinimas</th>
                        <th>Turinys</th>
                        <th>Kategorija</th>
                        <th>Būsena</th>
                        <th>Patikrinimo būsena</th>
                        <th>Sukūrimo data</th>
                    </tr>
                    </thead>
                    <tbody>
                    {articles.map(article => (
                        <tr
                            key={article.id}
                            onClick={() => navigate(`/admin/articles/${article.id}/edit`)}
                            style={{ cursor: 'pointer' }}
                        >
                            <td>{article.id}</td>
                            <td>{article.articleName}</td>
                            <td className="text-truncate" style={{ maxWidth: '200px' }} title={article.contents}>
                                {article.contents}
                            </td>
                            <td>{article.articleCategory}</td>
                            <td>{article.articleStatus}</td>
                            <td>{article.verificationStatus ? 'Patvirtinta' : 'Nepatvirtinta'}</td>
                            <td>{article.articleDate}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}