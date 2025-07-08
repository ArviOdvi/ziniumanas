import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import $ from 'jquery';
import 'datatables.net-bs5';
import 'datatables.net-bs5/css/dataTables.bootstrap5.min.css';
import './AdminPage.css'; // naujas failas stiliams

export default function AdminPage() {
    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const tableRef = useRef();
    const navigate = useNavigate();

    useEffect(() => {
        fetch('http://localhost:8080/api/articles')
            .then(res => {
                if (!res.ok) {
                    throw new Error('Klaida kraunant duomenis');
                }
                return res.json();
            })
            .then(data => {
                setArticles(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

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

    if (loading) return <div className="container mt-5">Kraunama...</div>;
    if (error) return <div className="container mt-5 text-danger">Klaida: {error}</div>;

    return (
        <div className="container mt-5">
            <h2 className="text-center mb-4">Straipsnių Duomenys</h2>
            <p className="text-center mb-4">Rodomi visi straipsnių įrašai.</p>

            <div className="table-container">
                <table ref={tableRef} className="table table-striped table-bordered table-hover" style={{ width: '100%', cursor: 'pointer' }}>
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
                            onClick={() => navigate(`/admin/edit/${article.id}`)}
                            className="clickable-row"
                        >
                            <td>{article.id}</td>
                            <td>{article.articleName}</td>
                            <td className="text-truncate" title={article.contents}>{article.contents}</td>
                            <td>{article.articleCategory}</td>
                            <td>{article.articleStatus}</td>
                            <td>{article.verificationStatus}</td>
                            <td>{article.articleDate}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}