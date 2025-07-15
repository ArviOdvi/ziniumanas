import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import 'bootstrap/dist/css/bootstrap.min.css';

export default function RegisterPage({ fullPage }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('USER');
    const [error, setError] = useState(null);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password, role })
            });
            if (!response.ok) throw new Error(`Registracija nepavyko: ${response.status} ${response.statusText}`);
            const data = await response.json();
            console.log('Registracijos atsakymas:', data);
            login(data.token, data.role);
            navigate(data.role === 'ADMIN' ? '/admin' : '/');
        } catch (err) {
            console.error('Klaida registruojantis:', err.message);
            setError(err.message);
        }
    };

    return (
        <div className={`container mt-5 ${fullPage ? 'py-5' : ''}`} style={{ maxWidth: '500px' }}>
            <h2 className="mb-4 text-center">Registracija</h2>
            {error && <div className="alert alert-danger text-center">{error}</div>}
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
                <button type="submit" className="btn btn-primary w-100">Registruotis</button>
            </form>
        </div>
    );
}