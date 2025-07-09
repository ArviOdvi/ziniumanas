import { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // Pridėkite useNavigate
import { useAuth } from '../contexts/AuthContext';

export default function LoginPage({ fullPage }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const { login } = useAuth();
    const navigate = useNavigate(); // Pridėkite navigate

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            if (!response.ok) throw new Error(`Prisijungimo klaida: ${response.status} ${response.statusText}`);
            const data = await response.json();
            login(data.token, data.role);
            navigate('/admin'); // Nukreipkite į /admin
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div style={{ alignContent: "center", maxWidth: '500px', margin: '20px auto', padding: '200px' }}>
            <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Prisijungimas</h2>
            {error && <div style={{ color: 'red', fontSize: '18px', marginBottom: '20px', textAlign: 'center' }}>{error}</div>}
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ width: '400%', display: 'block', marginBottom: '5px' }}>Vartotojo vardas</label>
                    <input
                        type="text"
                        style={{ width: '400%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Slaptažodis</label>
                    <input
                        type="password"
                        style={{ width: '400%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <button
                    type="submit"
                    style={{ width: '400%', padding: '10px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                    Prisijungti
                </button>
            </form>
        </div>
    );
}