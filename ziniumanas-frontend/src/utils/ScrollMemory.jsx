const SCROLL_KEY = 'articleListScroll';

export function saveScrollPosition(path, scrollY) {
    localStorage.setItem(SCROLL_KEY + path, scrollY); // Pakeista į localStorage
}

export function restoreScrollPosition(path) {
    const y = localStorage.getItem(SCROLL_KEY + path);
    return y ? parseInt(y, 10) : 0;
}