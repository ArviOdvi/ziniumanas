import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './Header.css';

export default function Header() {
    const location = useLocation();
    const navigate = useNavigate();
    const { isLoggedIn, logout } = useAuth();
    const [searchQuery, setSearchQuery] = useState('');

    const categories1 = [
        { path: '/', label: 'Naujienos' },
        { path: '/kategorija/pasaulyje', label: 'Pasaulyje' },
        { path: '/kategorija/ekonomika', label: 'Ekonomika' },
        { path: '/kategorija/kultura', label: 'Kultūra' },
        { path: '/kategorija/technologijos', label: 'Technologijos' },
        { path: '/kategorija/istorija', label: 'Istorija' },
        { path: '/kategorija/maistas', label: 'Maistas' },
        { path: '/kategorija/sveikata', label: 'Sveikata' }
    ];

    const categories2 = [
        { path: '/kategorija/lietuvoje', label: 'Lietuvoje' },
        { path: '/kategorija/politika', label: 'Politika' },
        { path: '/kategorija/mokslas', label: 'Mokslas' },
        { path: '/kategorija/sportas', label: 'Sportas' },
        { path: '/kategorija/muzika', label: 'Muzika' },
        { path: '/kategorija/laisvalaikis', label: 'Laisvalaikis' },
        { path: '/kategorija/vaikams', label: 'Vaikams' }
    ];

    const renderCategoryLinks = (categories) => (
        categories.map(cat => (
            <li className="nav-item" key={cat.path}>
                <Link
                    to={cat.path}
                    className={`nav-link category-link ${location.pathname === cat.path ? 'active-category' : ''}`}
                >
                    {cat.label}
                </Link>
            </li>
        ))
    );

    const handleSearch = (e) => {
        e.preventDefault();
        const trimmedQuery = searchQuery.trim();
        if (trimmedQuery) {
            navigate(`/search?q=${encodeURIComponent(trimmedQuery)}`, { replace: true });
            // Dabar NEVALOM laukelio – paliekam užpildytą.
        }
    };

    // Stebim, ar vartotojas išėjo iš paieškos puslapio
    useEffect(() => {
        if (!location.pathname.startsWith('/search')) {
            setSearchQuery(''); // Valom laukelį tik jei išeinam iš paieškos puslapio
        }
    }, [location.pathname]);

    return (
        <header className="bg-dark text-white p-1">
            <div className="d-flex justify-content-between align-items-center">
                <Link to="/" className="text-white text-decoration-none h2">
                    <img src="/ziniumanas.png" alt="Ziniumanas logotipas" style={{ maxHeight: "50px" }} />
                </Link>

                <div
                    className="bg-dark text-white px-2 py-1 w-100"
                    style={{ maxWidth: "200px", height: "auto", aspectRatio: "10 / 3" }}
                >
                    <p className="fst-italic" style={{ fontSize: "0.6rem", marginBottom: "0.125rem" }}>
                        Akmens amžius baigėsi ne dėl to, kad baigėsi akmens ištekliai. Tiesiog kažkas pasiūlė geresnę idėją.
                    </p>
                </div>

                <nav className="mt-2 mt-md-0">
                    <ul className="nav flex-nowrap">
                        {renderCategoryLinks(categories1)}
                    </ul>
                    <ul className="nav flex-nowrap" style={{ marginLeft: "80px", marginRight: "80px" }}>
                        {renderCategoryLinks(categories2)}
                    </ul>
                </nav>

                {isLoggedIn ? (
                    <button className="btn btn-outline-light" onClick={logout}>Atsijungti</button>
                ) : (
                    <Link to="/login" className="btn btn-outline-light">Prisijungti</Link>
                )}
                <Link to="/register" className="btn btn-outline-light ms-2">Registracija</Link>

                <form className="d-flex mt-2 mt-md-0" onSubmit={handleSearch}>
                    <label htmlFor="searchInput" className="visually-hidden">Ieškoti</label>
                    <input
                        className="form-control me-2"
                        type="search"
                        id="searchInput"
                        name="q"
                        placeholder="Ieškoti..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                    <button className="btn btn-outline-light" type="submit">🔍</button>
                </form>
            </div>
        </header>
    );
}