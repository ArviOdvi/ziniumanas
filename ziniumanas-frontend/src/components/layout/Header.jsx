import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import 'datatables.net-bs5/css/dataTables.bootstrap5.min.css';
import './Header.css';

export default function Header() {
    const location = useLocation();
    const navigate = useNavigate();
    const { isLoggedIn, user, logout } = useAuth();
    const [searchQuery, setSearchQuery] = useState('');
    const [language, setLanguage] = useState('LT');
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const categories1LT = [
        { path: '/', label: 'Naujienos' },
        { path: '/kategorija/pasaulyje', label: 'Pasaulyje' },
        { path: '/kategorija/ekonomika', label: 'Ekonomika' },
        { path: '/kategorija/kultura', label: 'Kultūra' },
        { path: '/kategorija/technologijos', label: 'Technologijos' },
        { path: '/kategorija/istorija', label: 'Istorija' },
        { path: '/kategorija/maistas', label: 'Maistas' },
        { path: '/kategorija/sveikata', label: 'Sveikata' }
    ];

    const categories2LT = [
        { path: '/kategorija/lietuvoje', label: 'Lietuvoje' },
        { path: '/kategorija/politika', label: 'Politika' },
        { path: '/kategorija/mokslas', label: 'Mokslas' },
        { path: '/kategorija/sportas', label: 'Sportas' },
        { path: '/kategorija/muzika', label: 'Muzika' },
        { path: '/kategorija/laisvalaikis', label: 'Laisvalaikis' },
        { path: '/kategorija/vaikams', label: 'Vaikams' }
    ];

    const categories3EN = [
        { path: '/', label: 'News' },
        { path: '/kategorija/pasaulyje', label: 'World' },
        { path: '/kategorija/ekonomika', label: 'Economy' },
        { path: '/kategorija/kultura', label: 'Culture' },
        { path: '/kategorija/technologijos', label: 'Technology' },
        { path: '/kategorija/istorija', label: 'History' },
        { path: '/kategorija/maistas', label: 'Food' },
        { path: '/kategorija/sveikata', label: 'Health' }
    ];

    const categories4EN = [
        { path: '/kategorija/lietuvoje', label: 'Lithuania' },
        { path: '/kategorija/politika', label: 'Politics' },
        { path: '/kategorija/mokslas', label: 'Science' },
        { path: '/kategorija/sportas', label: 'Sports' },
        { path: '/kategorija/muzika', label: 'Music' },
        { path: '/kategorija/laisvalaikis', label: 'Leisure' },
        { path: '/kategorija/vaikams', label: 'Kids' }
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
        }
    };

    const handleLanguageChange = (lang) => {
        setLanguage(lang);
        setIsDropdownOpen(false);
        console.log('Selected language:', lang);
    };

    useEffect(() => {
        if (!location.pathname.startsWith('/search')) {
            setSearchQuery('');
        }
    }, [location.pathname]);

    const categories1 = language === 'LT' ? categories1LT : categories3EN;
    const categories2 = language === 'LT' ? categories2LT : categories4EN;

    return (
        <header className="bg-dark text-white p-3">
            <div className="d-flex justify-content-between align-items-center">
                <Link to="/" className="text-white text-decoration-none h2">
                    <img src="/ziniumanas.png" alt="Ziniumanas logo" style={{ maxHeight: '50px' }} />
                </Link>

                <div
                    className="bg-dark text-white px-2 py-1"
                    style={{ maxWidth: '200px', height: 'auto', aspectRatio: '10 / 3' }}
                >
                    <p className="fst-italic" style={{ fontSize: '0.6rem', marginBottom: '0.125rem' }}>
                        {language === 'LT'
                            ? 'Akmens amžius baigėsi ne dėl to, kad baigėsi akmens ištekliai. Tiesiog kažkas pasiūlė geresnę idėją.'
                            : 'The Stone Age didn’t end because we ran out of stones. Someone just came up with a better idea.'}
                    </p>
                </div>

                <nav className="mt-2 mt-md-0">
                    <ul className="nav flex-nowrap">
                        {renderCategoryLinks(categories1)}
                    </ul>
                    <ul className="nav flex-nowrap" style={{ marginLeft: '80px', marginRight: '80px' }}>
                        {renderCategoryLinks(categories2)}
                    </ul>
                </nav>

                <div className="d-flex align-items-center gap-2">
                    <div className="custom-dropdown" style={{ width: '40px' }}>
                        <button
                            className="btn btn-outline-light dropdown-toggle"
                            style={{ padding: '2px 5px', fontSize: '12px' }}
                            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                        >
                            <span className="first-letter">{language[0]}</span>
                            <span className="second-letter">{language[1]}</span>
                        </button>
                        {isDropdownOpen && (
                            <div className="dropdown-menu show" style={{ minWidth: '40px', fontSize: '12px' }}>
                                <button
                                    className="dropdown-item"
                                    onClick={() => handleLanguageChange('LT')}
                                >
                                    <span className="first-letter">L</span>
                                    <span className="second-letter">T</span>
                                </button>
                                <button
                                    className="dropdown-item"
                                    onClick={() => handleLanguageChange('EN')}
                                >
                                    <span className="first-letter">E</span>
                                    <span className="second-letter">N</span>
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                <div className="d-flex align-items-center gap-2">
                    {isLoggedIn ? (
                        <>
                            <button className="btn btn-outline-light" onClick={logout}>
                                {language === 'LT' ? 'Atsijungti' : 'Logout'}
                            </button>
                            {user && user.role === 'ADMIN' ? (
                                <Link to="/admin" className="btn btn-outline-light">
                                    ADMIN
                                </Link>
                            ) : (
                                user && user.username
                            )}
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="btn btn-outline-light">
                                {language === 'LT' ? 'Prisijungti' : 'Login'}
                            </Link>
                            <Link to="/register" className="btn btn-outline-light">
                                {language === 'LT' ? 'Registracija' : 'Register'}
                            </Link>
                        </>
                    )}
                </div>

                <form className="d-flex mt-2 mt-md-0 ms-3" onSubmit={handleSearch}>
                    <label htmlFor="searchInput" className="visually-hidden">
                        {language === 'LT' ? 'Ieškoti' : 'Search'}
                    </label>
                    <input
                        className="form-control me-2"
                        type="search"
                        id="searchInput"
                        name="q"
                        placeholder={language === 'LT' ? 'Ieškoti...' : 'Search...'}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                    <button className="btn btn-outline-light" type="submit">🔍</button>
                </form>
            </div>
        </header>
    );
}