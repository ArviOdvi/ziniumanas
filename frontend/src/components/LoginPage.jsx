import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import 'bootstrap/dist/css/bootstrap.min.css';

export default function LoginPage({ fullPage }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        try {
            const response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Prisijungimo klaida: ${response.status} ${errorText || response.statusText}`);
            }
            const data = await response.json();
            console.log('Prisijungimo atsakymas:', data);
            login(data.token, data.role);
            navigate(data.role === 'ADMIN' ? '/admin' : '/');
        } catch (err) {
            console.error('Klaida prisijungiant:', err.message);
            setError(err.message);
        }
    };

    return (
        <div className={`container mt-5 ${fullPage ? 'py-5' : ''}`} style={{ maxWidth: '500px' }}>
            <h2 className="mb-4 text-center">Prisijungimas</h2>
            {error && (
                <div className="alert alert-danger text-center">
                    {error}
                    {error.includes('User not found') && (
                        <div className="mt-2">
                            <a href="/register" className="btn btn-link">Registruotis</a>
                        </div>
                    )}
                </div>
            )}
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label htmlFor="usernameInput" className="form-label">Vartotojo vardas</label>
                    <input
                        type="text"
                        id="usernameInput"
                        className="form-control"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label htmlFor="passwordInput" className="form-label">Slaptažodis</label>
                    <input
                        type="password"
                        id="passwordInput"
                        className="form-control"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-primary w-100">Prisijungti</button>
            </form>
        </div>
    );
}