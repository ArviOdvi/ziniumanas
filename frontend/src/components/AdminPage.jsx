import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import $ from 'jquery';
import 'datatables.net-bs5';
import 'datatables.net-bs5/css/dataTables.bootstrap5.min.css';
import './AdminPage.css';
import { useAuth } from '../contexts/AuthContext'; // Pakeista

export default function AdminPage() {
    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const tableRef = useRef();
    const navigate = useNavigate();
    const { user } = useAuth(); // Naudojame useAuth

    useEffect(() => {
        if (!user || !user.token) {
            setError('Nėra autentifikacijos tokeno. Prašome prisijungti.');
            setLoading(false);
            navigate('/login');
            return;
        }

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
                console.error('Klaida:', err.message);
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

    if (loading) return <div style={{ color: 'black', fontSize: '24px', textAlign: 'center', marginTop: '20px' }}>Kraunama...</div>;
    if (error) return <div style={{ color: 'red', fontSize: '24px', textAlign: 'center', marginTop: '20px' }}>Klaida: {error}</div>;

    return (
        <div style={{ margin: '20px auto', maxWidth: '1200px' }}>
            <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Straipsnių Duomenys</h2>
            <p style={{ textAlign: 'center', marginBottom: '20px' }}>Rodomi visi straipsnių įrašai.</p>

            <div style={{ overflowX: 'auto' }}>
                <table ref={tableRef} style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead style={{ backgroundColor: '#343a40', color: 'white' }}>
                    <tr>
                        <th style={{ padding: '10px' }}>ID</th>
                        <th style={{ padding: '10px' }}>Pavadinimas</th>
                        <th style={{ padding: '10px' }}>Turinys</th>
                        <th style={{ padding: '10px' }}>Kategorija</th>
                        <th style={{ padding: '10px' }}>Būsena</th>
                        <th style={{ padding: '10px' }}>Patikrinimo būsena</th>
                        <th style={{ padding: '10px' }}>Sukūrimo data</th>
                    </tr>
                    </thead>
                    <tbody>
                    {articles.map(article => (
                        <tr
                            key={article.id}
                            onClick={() => navigate(`/admin/articles/${article.id}/edit`)}
                            style={{ cursor: 'pointer' }}
                        >
                            <td style={{ padding: '10px', border: '1px solid #dee2e6' }}>{article.id}</td>
                            <td style={{ padding: '10px', border: '1px solid #dee2e6' }}>{article.articleName}</td>
                            <td style={{ padding: '10px', border: '1px solid #dee2e6', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis' }} title={article.contents}>{article.contents}</td>
                            <td style={{ padding: '10px', border: '1px solid #dee2e6' }}>{article.articleCategory}</td>
                            <td style={{ padding: '10px', border: '1px solid #dee2e6' }}>{article.articleStatus}</td>
                            <td style={{ padding: '10px', border: '1px solid #dee2e6' }}>{article.verificationStatus}</td>
                            <td style={{ padding: '10px', border: '1px solid #dee2e6' }}>{article.articleDate}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}