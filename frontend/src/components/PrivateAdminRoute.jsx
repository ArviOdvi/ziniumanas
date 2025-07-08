import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function PrivateAdminRoute({ children }) {
    const { user, isLoggedIn } = useAuth();

    // Patikrinam ar yra prisijungta ir ar rolė ADMIN
    if (!isLoggedIn || !user?.role || user.role !== 'ADMIN') {
        return <Navigate to="/" replace />;
    }

    return children;
}