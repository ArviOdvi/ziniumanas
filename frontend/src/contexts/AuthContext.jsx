import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext();

export default function AuthProvider({ children }) {
    const [user, setUser] = useState(null);

    const login = (token) => {
        localStorage.setItem('token', token);
        setUser({ token });
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
    };

    const isLoggedIn = !!user || !!localStorage.getItem('token');

    return (
        <AuthContext.Provider value={{ user, login, logout, isLoggedIn }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}