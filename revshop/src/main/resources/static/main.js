/**
 * RevShop - Shared JavaScript
 */

/**
 * Global Fetch Interceptor for CSRF Token Protection
 */
const { fetch: originalFetch } = window;
window.fetch = async (...args) => {
    let [resource, config] = args;
    if (config && config.method) {
        const method = config.method.toUpperCase();
        if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            
            if (csrfToken && csrfHeader) {
                config.headers = {
                    ...config.headers,
                    [csrfHeader]: csrfToken
                };
            }
        }
    }
    return originalFetch(resource, config);
};

// Add a product to cart (uses query params as expected by the API)
async function addToCart(productId, quantity = 1) {
    try {
        const response = await fetch(`/api/cart/add?productId=${productId}&quantity=${quantity}`, {
            method: 'POST'
        });

        if (response.ok) {
            showToast("Product added to cart!", "success");
            updateCartCount();
        } else if (response.status === 403 || response.status === 401) {
            // User is not logged in or not a BUYER
            if (confirm("Would you like to log in or create an account to start adding items to your cart?")) {
                window.location.href = '/login';
            }
        } else {
            const error = await response.text();
            showToast(error, "error");
        }
    } catch (err) {
        showToast("Request failed: " + err, "error");
    }
}

// Update the cart count badge in the header
async function updateCartCount() {
    try {
        const badge = document.getElementById('cart-count');
        if (!badge) return; // Not logged in as buyer, no badge exists

        const res = await fetch('/api/cart');
        if (res.ok) {
            const cart = await res.json();
            const count = cart.items ? cart.items.reduce((sum, item) => sum + item.quantity, 0) : 0;
            badge.textContent = count;
        }
    } catch (e) {
        // Silently fail — user might not be logged in
    }
}

// Toggle favorite status
async function toggleFavorite(productId, element, isWishlistPage = false) {
    try {
        const response = await fetch(`/api/favorites/toggle/${productId}`, {
            method: 'POST'
        });

        if (response.ok) {
            const data = await response.json();
            if (data.isFavorited) {
                element.classList.add('favorited');
            } else {
                element.classList.remove('favorited');
                if (isWishlistPage) {
                    // Remove from view if on wishlist page
                    const card = document.getElementById(`fav-product-${productId}`);
                    if (card) {
                        card.style.opacity = '0';
                        setTimeout(() => {
                            card.remove();
                            if (document.querySelectorAll('#favoritesGrid .product-card').length === 0) {
                                document.getElementById('favoritesGrid').style.display = 'none';
                                document.getElementById('noFavorites').style.display = 'block';
                            }
                        }, 300);
                    }
                }
            }
        } else if (response.status === 401) {
            if (confirm("To save this to your wishlist, join RevShop or sign in to your account. Go to login page?")) {
                window.location.href = '/login';
            }
        } else {
            try {
                const error = await response.json();
                showToast(error.message || "Error updating favorite", "error");
            } catch (e) {
                showToast("Error updating favorite", "error");
            }
        }
    } catch (err) {
        console.error("Favorite toggle failed:", err);
        showToast("Favorite toggle failed", "error");
    }
}


// Initialize cart count on page load
document.addEventListener('DOMContentLoaded', updateCartCount);

/**
 * Modern Toast Notification System
 */
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = '🔔';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';

    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <div class="toast-content">${message}</div>
        <span class="toast-close">&times;</span>
    `;

    container.appendChild(toast);
    setTimeout(() => toast.classList.add('show'), 10);

    const closeToast = () => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    };

    toast.querySelector('.toast-close').onclick = closeToast;
    setTimeout(() => { if (toast.parentElement) closeToast(); }, 5000);
}
