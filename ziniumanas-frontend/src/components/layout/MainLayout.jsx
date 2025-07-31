import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';

export default function MainLayout() {
    return (
        <div className="d-flex flex-column min-vh-100 overflow-hidden">
            <header className="bg-light border-bottom position-fixed top-0 w-100 z-3">
                <Header />
            </header>

            <main className="flex-grow-1">
                <Outlet />
            </main>

            <footer className="bg-light border-top position-fixed bottom-0 w-100 z-3">
                <Footer />
            </footer>
        </div>
    );
}