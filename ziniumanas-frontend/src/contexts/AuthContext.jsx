import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export const AuthContext = createContext();

export default function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = sessionStorage.getItem('token');
        const role = sessionStorage.getItem('role');
        const username = sessionStorage.getItem('username');
        if (token && role && username) {
            setUser({ token, role, username });
        }
    }, []);

    const login = (token, role, username) => {
        sessionStorage.setItem('token', token);
        sessionStorage.setItem('role', role);
        sessionStorage.setItem('username', username);
        setUser({ token, role, username });
        navigate(role === 'ADMIN' ? '/admin' : '/');
    };

    const logout = () => {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('role');
        sessionStorage.removeItem('username');
        setUser(null);
        navigate('/');
    };

    const isLoggedIn = !!user;

    return (
        <AuthContext.Provider value={{ user, login, logout, isLoggedIn }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}