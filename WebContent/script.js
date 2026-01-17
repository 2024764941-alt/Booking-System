// DotsStudio - Main Script
// Handles state management, data persistence requiring localStorage, and page-specific logic

// ==========================================
// DATA MANAGEMENT
// ==========================================
class DataManager {
    constructor() {
        this.STORAGE_KEYS = {
            USERS: 'dott_users',
            DESIGNERS: 'dott_designers',
            BOOKINGS: 'dott_bookings',
            CURRENT_USER: 'dott_current_user',
            TEMP_BOOKING: 'dott_temp_booking'
        };

        this.init();
    }

    // ... init ...

    init() {
        // Initialize Users
        if (!localStorage.getItem(this.STORAGE_KEYS.USERS)) {
            const defaultUsers = [
                { firstName: 'John', lastName: 'Doe', email: 'john@example.com', phone: '1234567890', address: '123 Main St', password: 'password' }
            ];
            localStorage.setItem(this.STORAGE_KEYS.USERS, JSON.stringify(defaultUsers));
        }

        // Initialize Designers
        if (!localStorage.getItem(this.STORAGE_KEYS.DESIGNERS)) {
            const defaultDesigners = [
                { id: 1, name: 'Alice Johnson', specialty: 'Modern Minimalist', availability: ['09:00', '10:00', '14:00'] },
                { id: 2, name: 'Bob Smith', specialty: 'Traditional Elegance', availability: ['11:00', '15:00', '16:00'] },
                { id: 3, name: 'Carol Lee', specialty: 'Contemporary Fusion', availability: ['08:00', '12:00', '17:00'] }
            ];
            localStorage.setItem(this.STORAGE_KEYS.DESIGNERS, JSON.stringify(defaultDesigners));
        }

        // Initialize Bookings
        if (!localStorage.getItem(this.STORAGE_KEYS.BOOKINGS)) {
            const defaultBookings = [
                { id: 1, client: 'John Doe', email: 'john@example.com', phone: '1234567890', designer: 'Alice Johnson', date: '2026-01-02', time: '09:00', notes: 'Kitchen design', status: 'Confirmed' },
                { id: 2, client: 'Jane Smith', email: 'jane@example.com', phone: '0987654321', designer: 'Bob Smith', date: '2026-01-03', time: '11:00', notes: 'Living room', status: 'Pending' }
            ];
            localStorage.setItem(this.STORAGE_KEYS.BOOKINGS, JSON.stringify(defaultBookings));
        }
    }

    // --- Helpers ---
    _get(key) {
        const data = localStorage.getItem(key);
        return data ? JSON.parse(data) : null;
    }

    _set(key, data) {
        localStorage.setItem(key, JSON.stringify(data));
    }

    // --- User Methods ---
    getUsers() { return this._get(this.STORAGE_KEYS.USERS) || []; }

    registerUser(user) {
        const users = this.getUsers();
        if (users.find(u => u.email === user.email)) return { success: false, message: 'Email already registered' };
        users.push(user);
        this._set(this.STORAGE_KEYS.USERS, users);
        this.login(user); // Auto login
        return { success: true };
    }

    updateUser(updatedUser) {
        let users = this.getUsers();
        const index = users.findIndex(u => u.email === updatedUser.email);
        if (index !== -1) {
            users[index] = updatedUser;
            this._set(this.STORAGE_KEYS.USERS, users);
            // Update current session if it's the logged-in user
            const currentUser = this.getCurrentUser();
            if (currentUser && currentUser.email === updatedUser.email) {
                this.login(updatedUser);
            }
            return { success: true };
        }
        return { success: false, message: 'User not found' };
    }

    login(user) {
        localStorage.setItem(this.STORAGE_KEYS.CURRENT_USER, JSON.stringify(user));
    }

    logout() {
        localStorage.removeItem(this.STORAGE_KEYS.CURRENT_USER);
        localStorage.removeItem(this.STORAGE_KEYS.TEMP_BOOKING);
        // Redirect to Server-Side Logout to kill session
        window.location.href = 'LogoutServlet';
    }

    getCurrentUser() {

        // Helper function to get status badge styles


        return this._get(this.STORAGE_KEYS.CURRENT_USER);
    }

    // --- Designer Methods ---
    getDesigners() { return this._get(this.STORAGE_KEYS.DESIGNERS) || []; }

    addDesigner(designer) {
        const designers = this.getDesigners();
        designer.id = designers.length > 0 ? Math.max(...designers.map(d => d.id)) + 1 : 1;
        designers.push(designer);
        this._set(this.STORAGE_KEYS.DESIGNERS, designers);
    }

    updateDesigner(updatedDesigner) {
        let designers = this.getDesigners();
        const index = designers.findIndex(d => d.id === updatedDesigner.id);
        if (index !== -1) {
            designers[index] = updatedDesigner;
            this._set(this.STORAGE_KEYS.DESIGNERS, designers);
        }
    }

    deleteDesigner(id) {
        let designers = this.getDesigners().filter(d => d.id !== id);
        this._set(this.STORAGE_KEYS.DESIGNERS, designers);
    }

    // --- Booking Methods ---
    getBookings() { return this._get(this.STORAGE_KEYS.BOOKINGS) || []; }

    addBooking(booking) {
        const bookings = this.getBookings();
        booking.id = bookings.length > 0 ? Math.max(...bookings.map(b => b.id)) + 1 : 1;
        bookings.push(booking);
        this._set(this.STORAGE_KEYS.BOOKINGS, bookings);
        return booking;
    }

    updateBookingStatus(id, status) {
        const bookings = this.getBookings();
        const booking = bookings.find(b => b.id === id);
        if (booking) {
            booking.status = status;
            this._set(this.STORAGE_KEYS.BOOKINGS, bookings);
        }
    }

    deleteBooking(id) {
        const bookings = this.getBookings().filter(b => b.id !== id);
        this._set(this.STORAGE_KEYS.BOOKINGS, bookings);
    }

    // --- Booking Flow State ---
    getTempBooking() { return this._get(this.STORAGE_KEYS.TEMP_BOOKING) || {}; }

    updateTempBooking(data) {
        const current = this.getTempBooking();
        const updated = { ...current, ...data };
        this._set(this.STORAGE_KEYS.TEMP_BOOKING, updated);
        return updated;
    }

    clearTempBooking() {
        localStorage.removeItem(this.STORAGE_KEYS.TEMP_BOOKING);
    }
}

const db = new DataManager();

// Helper: Calculate Common Slots
async function calculateCommonSlots(designers, date) {
    const standardHours = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'];
    const designersToCheck = Array.isArray(designers) ? designers : [designers];

    // Ensure we have designers to check
    if (designersToCheck.length === 0) return [];

    try {
        const [designersConfig, ...bookedTimesArrays] = await Promise.all([
            // FILTER BY DATE: Only return designers valid for this date (Source of Truth)
            fetch(`api/designers?date=${encodeURIComponent(date)}`).then(r => r.json()),
            ...designersToCheck.map(dName =>
                fetch(`api/availability?designer=${encodeURIComponent(dName)}&date=${encodeURIComponent(date)}`).then(r => r.json())
            )
        ]);

        let intersection = null;

        designersToCheck.forEach((dName, index) => {
            const dConfig = designersConfig.find(d => d.name === dName);
            const dBooked = bookedTimesArrays[index];

            // STRICT CHECK: If designer is NOT in the filtered list, they are UNAVAILABLE using "Opt-in" logic
            if (!dConfig) {
                console.log(`DEBUG: Designer ${dName} is not available on ${date} (Filtered by Server)`);
                // Intersection with empty set is empty
                intersection = [];
                return;
            }

            let dSlots = [];
            // Parse start/end from config (Server sends workHoursStart/workHoursEnd)
            // dConfig.availability is a display string (e.g. "09:00 - 17:00"), not discrete slots.
            if (dConfig && dConfig.workHoursStart && dConfig.workHoursEnd) {
                const start = parseInt(dConfig.workHoursStart.split(':')[0]);
                const end = parseInt(dConfig.workHoursEnd.split(':')[0]);
                for (let h = start; h < end; h++) {
                    dSlots.push(h.toString().padStart(2, '0') + ':00');
                }
            }

            // Legacy/Fallback check (incase property names change or missing)
            if (dSlots.length === 0 && dConfig && dConfig.shiftStart && dConfig.shiftEnd) {
                const start = parseInt(dConfig.shiftStart.split(':')[0]);
                const end = parseInt(dConfig.shiftEnd.split(':')[0]);
                for (let h = start; h < end; h++) {
                    dSlots.push(h.toString().padStart(2, '0') + ':00');
                }
            }

            if (dSlots.length === 0) dSlots = [...standardHours]; // Fallback defaults

            console.log(`DEBUG: Designer ${dName} Slots:`, dSlots);
            console.log(`DEBUG: Designer ${dName} Booked:`, dBooked);

            // Filter out booked
            const dFree = dSlots.filter(t => !dBooked.includes(t));

            if (intersection === null) {
                intersection = dFree;
            } else {
                intersection = intersection.filter(t => dFree.includes(t));
            }
        });

        return intersection || [];
    } catch (err) {
        console.error("Error calculating availability", err);
        throw err;
    }
}

// Helper function to get status badge styles
function getStatusStyle(status) {
    status = (status || '').toLowerCase();
    if (status === 'confirmed') return 'background-color: #d4edda; color: #155724; padding: 4px 12px; border-radius: 12px; font-size: 0.85em; font-weight: 600;';
    if (status === 'pending') return 'background-color: #fff3cd; color: #856404; padding: 4px 12px; border-radius: 12px; font-size: 0.85em; font-weight: 600;';
    if (status === 'completed') return 'background-color: #cce5ff; color: #004085; padding: 4px 12px; border-radius: 12px; font-size: 0.85em; font-weight: 600;';
    if (status === 'cancelled') return 'background-color: #f8d7da; color: #721c24; padding: 4px 12px; border-radius: 12px; font-size: 0.85em; font-weight: 600;';
    return 'background-color: #e2e3e5; color: #383d41; padding: 4px 12px; border-radius: 12px; font-size: 0.85em; font-weight: 600;';
}


// ==========================================
// GLOBAL OVERRIDES
// ==========================================
// Force all alerts to use custom modal
const originalAlert = window.alert;
const originalConfirm = window.confirm;

window.alert = function (message) {
    if (window.showAlert) {
        window.showAlert(message);
    } else {
        originalAlert(message);
    }
};

// ==========================================
// CUSTOM ALERT & CONFIRM IMPLEMENTATION
// ==========================================



// Create Toast Container on load if not exists
function ensureToastContainer() {
    if (!document.querySelector('.toast-container')) {
        const container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
}

// Custom Alert
window.showAlert = (message, callback) => {
    // Remove existing modal if any
    const existingOverlay = document.querySelector('.custom-modal-overlay');
    if (existingOverlay) existingOverlay.remove();

    const overlay = document.createElement('div');
    overlay.className = 'custom-modal-overlay';

    overlay.innerHTML = `
        <div class="custom-modal">
            <h3>Notification</h3>
            <p>${message}</p>
            <div class="custom-modal-actions">
                <button class="btn" id="custom-alert-ok">OK</button>
            </div>
        </div>
    `;

    document.body.appendChild(overlay);

    const okBtn = document.getElementById('custom-alert-ok');
    okBtn.focus();

    const closeAlert = () => {
        overlay.style.animation = 'fadeOut 0.3s ease-out'; // You might want to add fadeOut keyframes or just remove
        setTimeout(() => {
            overlay.remove();
            if (callback) callback();
        }, 100); // Small delay for animation feel
    };

    okBtn.onclick = closeAlert;

    // Close on Enter key
    okBtn.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') closeAlert();
    });
};

// Override native alert
window.alert = window.showAlert;

// Custom Confirm
window.showCustomConfirm = (message, onConfirm) => {
    // Remove existing modal if any
    const existingOverlay = document.querySelector('.custom-modal-overlay');
    if (existingOverlay) existingOverlay.remove();

    const overlay = document.createElement('div');
    overlay.className = 'custom-modal-overlay';

    overlay.innerHTML = `
        <div class="custom-modal">
            <h3>Confirmation</h3>
            <p>${message}</p>
            <div class="custom-modal-actions">
                <button class="btn secondary" id="custom-confirm-cancel">Cancel</button>
                <button class="btn" id="custom-confirm-ok">Confirm</button>
            </div>
        </div>
    `;

    document.body.appendChild(overlay);

    const okBtn = document.getElementById('custom-confirm-ok');
    const cancelBtn = document.getElementById('custom-confirm-cancel');

    cancelBtn.focus(); // Focus cancel for safety

    cancelBtn.onclick = () => {
        overlay.remove();
    };

    okBtn.onclick = () => {
        overlay.remove();
        if (onConfirm) onConfirm();
    };
};

// Toast Notification
window.showToast = (message, type = 'info') => {
    ensureToastContainer();
    const container = document.querySelector('.toast-container');

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let icon = 'info-circle';
    if (type === 'success') icon = 'check-circle';
    if (type === 'error') icon = 'exclamation-circle';

    toast.innerHTML = `
        <i class="fas fa-${icon}"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    // Trigger reflow to enable transition
    void toast.offsetWidth;
    toast.classList.add('show');

    // Auto dismiss
    setTimeout(() => {
        toast.classList.remove('show');
        // FIX: Long timeout here is a typo or placeholder? 
        // Correct to match transition: 400ms
        setTimeout(() => toast.remove(), 400);
    }, 3000);
};

window.confirm = function (message) {
    // Legacy support where possible, but async flow breaks standard confirm() return.
    // Ideally code should migrate to showConfirm(msg, callback).
    // For now, we fallback to native for direct return usages, or custom if widely refactored.
    return originalConfirm(message);
};


// ==========================================
// PAGE LOGIC
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
    setupCurrentPage();
});

// Common Navigation Setup
function setupNavigation() {
    const user = db.getCurrentUser();
    // Check if user has ADMIN or DESIGNER role
    const hasDashboardAccess = (user && (user.role === 'ADMIN' || user.role === 'DESIGNER')) || sessionStorage.getItem('admin_logged_in') === 'true';

    // Elements
    const navLogin = document.getElementById('nav-login');
    const navDashboard = document.getElementById('nav-dashboard');

    // Dropdown Elements
    const navUserDropdown = document.getElementById('nav-user-dropdown');
    const userDropdownToggle = document.getElementById('user-dropdown-toggle');
    const dropdownMenu = document.querySelector('.dropdown-menu');
    const navLogoutDropdown = document.getElementById('nav-logout-dropdown');

    // Legacy/Admin Logout Button (if present outside dropdown)
    const navLogoutCustomer = document.getElementById('nav-logout-customer');

    // --- Reset Initial State ---
    if (navLogin) navLogin.style.display = 'inline-block';
    if (navUserDropdown) navUserDropdown.style.display = 'none';
    if (navDashboard) navDashboard.style.display = 'none';
    if (navLogoutCustomer) navLogoutCustomer.style.display = 'none';

    // --- Logic ---
    if (hasDashboardAccess) {
        if (navLogin) navLogin.style.display = 'none';
        if (navDashboard) navDashboard.style.display = 'inline-block';

        // Admin usually has a separate logout or uses the customer one repurposed
        if (navLogoutCustomer) {
            navLogoutCustomer.style.display = 'inline-block';
            // Clean old listeners
            const newLogout = navLogoutCustomer.cloneNode(true);
            navLogoutCustomer.parentNode.replaceChild(newLogout, navLogoutCustomer);

            newLogout.addEventListener('click', (e) => {
                e.preventDefault();
                sessionStorage.removeItem('admin_logged_in');
                db.logout(); // Call shared logout to kill server session
            });
        }

    } else if (user) {
        // Logged In Customer
        if (navLogin) navLogin.style.display = 'none';
        if (navDashboard) navDashboard.style.display = 'none';

        if (navUserDropdown && userDropdownToggle) {
            navUserDropdown.style.display = 'inline-block';
            userDropdownToggle.innerHTML = `${user.firstName} <i class="icon-arrow-down" style="font-size: 0.6em; margin-left: 5px;">&#9660;</i>`;

            // --- Update Dropdown Links with Hashes ---
            if (dropdownMenu) {
                const dropdownLinks = dropdownMenu.querySelectorAll('a');
                dropdownLinks.forEach(link => {
                    if (link.textContent.includes('My Bookings')) link.href = 'profile.jsp#bookings';
                    if (link.textContent.includes('Profile Settings')) link.href = 'profile.jsp#settings';
                });
            }
            // ----------------------------------------

            // Toggle Dropdown
            // Remove old listeners to be safe
            const newToggle = userDropdownToggle.cloneNode(true);
            userDropdownToggle.parentNode.replaceChild(newToggle, userDropdownToggle);

            newToggle.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                if (dropdownMenu) dropdownMenu.classList.toggle('show');
            });

            // Close on click outside
            document.addEventListener('click', (e) => {
                if (navUserDropdown && !navUserDropdown.contains(e.target)) {
                    if (dropdownMenu) dropdownMenu.classList.remove('show');
                }
            });

            // Logout Logic (Dropdown Item)
            if (navLogoutDropdown) {
                const newLogoutDrop = navLogoutDropdown.cloneNode(true);
                navLogoutDropdown.parentNode.replaceChild(newLogoutDrop, navLogoutDropdown);

                newLogoutDrop.addEventListener('click', (e) => {
                    e.preventDefault();
                    db.logout();
                });
            }
        } else {
            // Fallback if dropdown elements are missing usually (e.g. old page version)
            // Try to use legacy logout if available
            if (navLogoutCustomer) {
                navLogoutCustomer.style.display = 'inline-block';
                const newLogout = navLogoutCustomer.cloneNode(true);
                navLogoutCustomer.parentNode.replaceChild(newLogout, navLogoutCustomer);
                newLogout.addEventListener('click', (e) => {
                    e.preventDefault();
                    db.logout();
                });
            }
        }

    } else {
        // Not Logged In
        // Default state is already set (Login visible)
    }
}

function setupCurrentPage() {
    const path = window.location.pathname;

    if (document.getElementById('landing')) {
        setupLanding();
    } else if (document.getElementById('booking-method-page')) {
        setupBookingMethod();
    } else if (document.getElementById('booking-category-page')) {
        setupBookingCategory();
    } else if (document.getElementById('booking-date-page')) {
        setupBookingDate();
    } else if (document.getElementById('booking-designer-page')) {
        setupBookingDesigner();
    } else if (document.getElementById('booking-time-page')) {
        setupBookingTime();
    } else if (document.getElementById('booking-summary-page')) {
        setupBookingSummary();
    } else if (document.getElementById('booking-confirmation-page')) {
        setupBookingConfirmation();
        // } else if (document.getElementById('admin-login-form')) {
        //    setupAdminLogin();
    } else if (document.getElementById('register')) {
        setupRegister();
    } else if (document.getElementById('admin-dashboard-page')) {
        setupAdminDashboard();
    } else if (document.getElementById('profile-page')) {
        setupProfile();
    } else if (document.getElementById('designer-dashboard-page')) {
        setupDesignerDashboard();
    }
}

// ------------------------------------------
// 3.5. Booking: Method Selection
// ------------------------------------------
function setupBookingMethod() {
    const user = db.getCurrentUser();
    if (!user) { window.location.href = 'login.jsp'; return; }

    // Clear previous incomplete bookings
    db.clearTempBooking();
}

// ------------------------------------------
// 3.6. Booking: Category
// ------------------------------------------
// ------------------------------------------
// 3.6. Booking: Category
// ------------------------------------------
function setupBookingCategory() {
    const user = db.getCurrentUser();
    if (!user) { window.location.href = 'login.jsp'; return; }

    const container = document.getElementById('category-grid');
    const nextBtn = document.getElementById('next-to-designer-cat');
    let selectedCategories = []; // Array for Multi-Select

    fetch('api/specialties')
        .then(res => res.json())
        .then(categories => {
            container.innerHTML = '';
            if (categories.length === 0) {
                container.innerHTML = '<p>No styles available.</p>';
                return;
            }

            categories.forEach(cat => {
                const card = document.createElement('div');
                card.className = 'category-card';
                const imgName = cat.toLowerCase() + '.jpg';

                card.innerHTML = `
                    <div style="height: 150px; overflow: hidden; margin-bottom: 1rem; border-radius: 4px;">
                        <img src="category_images/${imgName}?v=${new Date().getTime()}" 
                             alt="${cat}" 
                             class="category-image"
                             style="width: 100%; height: 100%; object-fit: cover;"
                             onerror="this.style.display='none'">
                    </div>
                    <h3>${cat}</h3>
                `;

                card.addEventListener('click', () => {
                    // Multi-Select Toggle
                    if (selectedCategories.includes(cat)) {
                        selectedCategories = selectedCategories.filter(c => c !== cat);
                        card.classList.remove('selected');
                    } else {
                        selectedCategories.push(cat);
                        card.classList.add('selected');
                    }
                });

                container.appendChild(card);
            });
        })
        .catch(err => {
            console.error('Error fetching categories:', err);
            container.innerHTML = '<p>Error loading styles.</p>';
        });

    nextBtn.addEventListener('click', () => {
        if (selectedCategories.length === 0) {
            if (window.showToast) window.showToast('Please select at least one style', 'error');
            else showAlert('Please select at least one style');
            return;
        }
        // Store as CSV string for API compatibility, or modify DB to handle array. 
        // Servlet expects "Modern,Classic".
        db.updateTempBooking({ category: selectedCategories.join(',') });

        // Flow A: Category -> Designer
        window.location.href = 'booking-designer.jsp';
    });
}


// ------------------------------------------
// 1. Landing Page
// ------------------------------------------
function setupLanding() {
    const user = db.getCurrentUser();
    const btn = document.getElementById('book-consultation-btn');
    if (btn) {
        btn.addEventListener('click', () => {
            if (user) {
                window.location.href = 'booking-method.jsp';
            } else {
                window.location.href = 'login.jsp';
            }
        });
    }
}

// ------------------------------------------
// 2. Login Page
// ------------------------------------------
// ------------------------------------------
// 3. Register Page - Handled Server Side (RegisterServlet)
// ------------------------------------------
// function setupRegister() { ... }

function setupRegister() {
    console.log(">>> DEBUG: setupRegister called");
    const form = document.getElementById('jsp-register-form');
    if (!form) {
        console.error(">>> DEBUG: Register form not found");
        return;
    }

    form.addEventListener('submit', (e) => {
        console.log(">>> DEBUG: Register submit intercepted");
        const password = document.getElementById('register-password').value.trim();
        const confirm = document.getElementById('confirm-password').value.trim();

        if (password !== confirm) {
            e.preventDefault();
            const msg = 'The passwords entered do not match. Please try again.';
            console.warn(">>> DEBUG: Password mismatch. Showing alert: " + msg);

            // Try explicit toast if custom one fails logic
            if (window.showToast) {
                window.showToast(msg, 'error');
            } else if (window.showAlert) {
                window.showAlert(msg);
            } else {
                alert(msg);
            }
        }
    });
}

// ------------------------------------------
// 3. Register Page
// ------------------------------------------


// ------------------------------------------
// 4. Booking: Date
// ------------------------------------------
function setupBookingDate() {
    const user = db.getCurrentUser();
    if (!user) { window.location.href = 'login.jsp'; return; }

    const nextBtn = document.getElementById('next-to-designer');
    const input = document.getElementById('appointment-date');

    // Pre-fill if exists
    const temp = db.getTempBooking();
    if (temp.date) input.value = temp.date;

    nextBtn.addEventListener('click', async () => {
        if (!input.value) {
            if (window.showToast) window.showToast('Please select a date', 'error');
            else showAlert('Please select a date');
            return;
        }

        // Validate Availability if Designers are selected (Flow A)
        const currentTemp = db.getTempBooking(); // Re-fetch to get latest state
        if (currentTemp.designers || currentTemp.designer) {
            const designers = currentTemp.designers || [currentTemp.designer];
            nextBtn.disabled = true;
            nextBtn.textContent = 'Checking availability...';

            try {
                const commonSlots = await calculateCommonSlots(designers, input.value);
                if (commonSlots.length === 0) {
                    showAlert('The selected designers are fully booked on this date. Please select another date.');
                    nextBtn.disabled = false;
                    nextBtn.textContent = 'Next';
                    return;
                }
            } catch (error) {
                showAlert('Error checking availability. Please try again.');
                nextBtn.disabled = false;
                nextBtn.textContent = 'Next';
                return;
            }
            nextBtn.disabled = false;
            nextBtn.textContent = 'Next';
        }

        db.updateTempBooking({ date: input.value });

        // Routing:
        // Flow A (Category->Designer->Date) -> Go to Time
        // Flow B (Date) -> Go to Time
        window.location.href = 'booking-time.jsp';
    });
}


// ------------------------------------------
// 5. Booking: Designer
// ------------------------------------------
// ------------------------------------------
// Navigation Helper
// ------------------------------------------
window.handleBookingBack = function () {
    const temp = db.getTempBooking();
    const currentPage = window.location.pathname.split('/').pop();
    console.log("DEBUG: Handling Back Navigation from", currentPage);

    if (currentPage.includes('booking-category.jsp')) {
        window.location.href = 'booking-method.jsp';
    } else if (currentPage.includes('booking-date.jsp')) {
        if (temp.category) { window.location.href = 'booking-designer.jsp'; }
        else { window.location.href = 'booking-method.jsp'; }
    } else if (currentPage.includes('booking-time.jsp')) {
        window.location.href = 'booking-date.jsp';
    } else if (currentPage.includes('booking-designer.jsp')) {
        if (temp.category) { window.location.href = 'booking-category.jsp'; }
        else if (temp.date) { window.location.href = 'booking-time.jsp'; }
        else { window.location.href = 'booking-category.jsp'; }
    } else if (currentPage.includes('booking-summary.jsp')) {
        if (temp.category) { window.location.href = 'booking-time.jsp'; }
        else { window.location.href = 'booking-designer.jsp'; }
    } else {
        history.back();
    }
};

// ------------------------------------------
// 5. Booking: Designer
// ------------------------------------------
function setupBookingDesigner() {
    const user = db.getCurrentUser();
    if (!user) { window.location.href = 'login.jsp'; return; }

    const container = document.getElementById('designer-cards');
    container.innerHTML = '<p>Loading designers...</p>';

    // Determine query: Date-First (Flow B) or Category-First (Flow A)
    const temp = db.getTempBooking();
    let url = 'api/designers';
    let isFlowB = false;

    // Build URL Parameters
    let params = new URLSearchParams();

    // Add Timestamp to bust cache
    params.append('_t', new Date().getTime());

    if (temp.date && temp.time) {
        // Flow B
        params.append('date', temp.date);
        params.append('time', temp.time);
        isFlowB = true;
    } else if (temp.category) {
        // Flow A
        params.append('category', temp.category);
    }

    // Construct final URL
    url += '?' + params.toString();

    console.log("DEBUG: Fetching Designers URL:", url); // Debug Log

    // Initialize selected designers from temp if exists (and handle legacy single string)
    let selectedDesigners = [];
    if (temp.designers) {
        selectedDesigners = Array.isArray(temp.designers) ? temp.designers : [temp.designers];
    } else if (temp.designer) { // Legacy fallback
        selectedDesigners = [temp.designer];
    }


    fetch(url)
        .then(res => res.json())
        .then(designers => {
            container.innerHTML = '';

            console.log("DEBUG: Raw Designers from API:", designers); // Debug Log

            // Filter inactive/full (Backend time filter handles availability for Flow B)
            const activeDesigners = designers.filter(d => {
                // Relaxed Status Check (Case-Insensitive)
                const status = d.status || d.Status || 'Active';
                const isActive = status.toLowerCase() === 'active';

                const current = d.currentProjects || 0;
                // Treat 0 or null max as Default 10 (or unlimited) rather than Full
                const max = d.maxProjects || d.maxSimultaneousProjects || 10;

                // Only consider full if max is explicitly set and positive
                const isFull = max > 0 && current >= max;

                if (!isActive) console.log(`DEBUG: Designer ${d.name} filtered out (Status: ${status})`);
                if (isFull) console.log(`DEBUG: Designer ${d.name} filtered out (Full: ${current}/${max})`);

                return isActive && !isFull;
            });

            console.log("DEBUG: Active Designers after filter:", activeDesigners); // Debug Log

            if (activeDesigners.length === 0) {
                container.innerHTML = '<p>No available designers found matching your criteria.</p>';
                return;
            }

            // Create "Next" button for Flow A (since manual selection is needed)
            // For Flow B, we also want Multi-Select now per requirements.

            // Add instructions
            const instruction = document.createElement('p');
            instruction.style.gridColumn = "1 / -1";
            instruction.style.marginBottom = "1rem";
            instruction.textContent = "Select one or more designers.";
            container.appendChild(instruction);


            activeDesigners.forEach(designer => {
                const card = document.createElement('div');
                card.className = 'card';
                card.innerHTML = `<h3>${designer.name}</h3><p>${designer.specialty}</p>`;

                // Highlight selection
                if (selectedDesigners.includes(designer.name)) {
                    card.classList.add('selected');
                }

                card.addEventListener('click', () => {
                    // Multi-Select Toggle
                    if (selectedDesigners.includes(designer.name)) {
                        selectedDesigners = selectedDesigners.filter(n => n !== designer.name);
                        card.classList.remove('selected');
                    } else {
                        selectedDesigners.push(designer.name);
                        card.classList.add('selected');
                    }
                });
                container.appendChild(card);
            });

            // Add a floating or bottom "Next" button because we can select multiple
            // Changing the Back/Next buttons in JSP is generic, we need to bind the "Next" logic here.
            // But wait, the JSP has "Back". It doesn't have "Next" because it was click-card-to-advance.
            // We need to inject a Next button or repurpose one if it exists. 
            // The JSP for booking-designer does NOT have a Next button. We must add one dynamically.

            let nextBtn = document.getElementById('dynamic-next-btn');
            if (!nextBtn) {
                nextBtn = document.createElement('button');
                nextBtn.id = 'dynamic-next-btn';
                nextBtn.className = 'btn';
                nextBtn.textContent = 'Next';
                nextBtn.style.marginTop = '20px';
                nextBtn.style.gridColumn = '1 / -1'; // Span full width
                nextBtn.style.justifySelf = 'end';
                container.parentElement.appendChild(nextBtn); // Append to container parent
            }

            nextBtn.onclick = () => {
                if (selectedDesigners.length === 0) {
                    if (window.showToast) window.showToast('Please select at least one designer', 'error');
                    else showAlert('Please select at least one designer');
                    return;
                }

                db.updateTempBooking({ designers: selectedDesigners, designer: selectedDesigners[0] }); // store first as primary for legacy compat if needed

                if (isFlowB) {
                    // Flow B: Date->Time->Designer -> Summary
                    window.location.href = 'booking-summary.jsp';
                } else {
                    // Flow A: Category->Designer -> Date
                    window.location.href = 'booking-date.jsp';
                }
            };

        })
        .catch(err => {
            console.error('Error fetching designers:', err);
            container.innerHTML = '<p>Error loading designers. Please try again later.</p>';
        });
}

// ------------------------------------------
// 6. Booking: Time
// ------------------------------------------
// ------------------------------------------
// 6. Booking: Time
// ------------------------------------------
// ------------------------------------------
// 6. Booking: Time
// ------------------------------------------
function setupBookingTime() {
    const user = db.getCurrentUser();
    if (!user) { window.location.href = 'login.jsp'; return; }

    const temp = db.getTempBooking();
    const container = document.getElementById('time-slots');
    container.innerHTML = '<p>Loading available times...</p>';

    // Check if we have designers (Flow A)
    // IMPORTANT: Ensure we validly check for non-empty array
    const hasDesigners = temp.designers && Array.isArray(temp.designers) && temp.designers.length > 0;

    // Define standard hours if no specific constraints
    const standardHours = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'];

    if (!hasDesigners) {
        // Flow B: Date -> Time (No designers selected yet)
        console.log("DEBUG: Flow B identified (No designers). Fetching booked times for date:", temp.date);

        if (!temp.date) {
            container.innerHTML = '<p>Please select a date first.</p>';
            return;
        }

        // Fetch booked times for the selected date
        fetch(`api/booked-times?date=${encodeURIComponent(temp.date)}&_t=${new Date().getTime()}`)
            .then(res => res.json())
            .then(bookedTimes => {
                console.log("DEBUG: Booked times:", bookedTimes);

                // Filter out booked times from standard hours
                const availableTimes = standardHours.filter(time => !bookedTimes.includes(time));

                console.log("DEBUG: Available times:", availableTimes);
                renderTimeSlots(availableTimes);

                // Setup Next Button for Flow B
                const nextBtn = document.getElementById('next-to-summary');
                // Remove old listeners by cloning
                const newNextBtn = nextBtn.cloneNode(true);
                nextBtn.parentNode.replaceChild(newNextBtn, nextBtn);

                newNextBtn.addEventListener('click', () => {
                    const currentTemp = db.getTempBooking();
                    if (!currentTemp.time) { showAlert('Please select a time slot'); return; }

                    // Flow B: Go to Designer
                    window.location.href = 'booking-designer.jsp';
                });
            })
            .catch(err => {
                console.error("Error fetching booked times:", err);
                container.innerHTML = '<p>Error loading available times. Please try again.</p>';
            });

        return;
    }

    // Flow A: Intersection Logic
    const designersToCheck = Array.isArray(temp.designers) ? temp.designers : [temp.designers];

    calculateCommonSlots(designersToCheck, temp.date)
        .then(intersection => {
            renderTimeSlots(intersection);
        })
        .catch(err => {
            console.error("Error calculating availability", err);
            container.innerHTML = '<p>Error loading availability.</p>';
        });

    function renderTimeSlots(slots) {
        container.innerHTML = '';
        if (slots.length === 0) {
            container.innerHTML = '<p>No common time slots available for selected designers on this date.</p>';
            return;
        }

        slots.forEach(time => {
            const slot = document.createElement('div');
            slot.className = 'time-slot';
            slot.textContent = time;
            if (temp.time === time) slot.classList.add('selected');

            slot.addEventListener('click', () => {
                document.querySelectorAll('.time-slot').forEach(s => s.classList.remove('selected'));
                slot.classList.add('selected');
                db.updateTempBooking({ time: time });
            });
            container.appendChild(slot);
        });

        // Setup Next Button for Flow A
        const nextBtn = document.getElementById('next-to-summary');
        const newNextBtn = nextBtn.cloneNode(true); // Clone to clear old listeners
        nextBtn.parentNode.replaceChild(newNextBtn, nextBtn);

        newNextBtn.addEventListener('click', () => {
            const currentTemp = db.getTempBooking();
            if (!currentTemp.time) { showAlert('Please select a time slot'); return; }

            // Flow A: Done -> Summary
            window.location.href = 'booking-summary.jsp';
        });
    }
}

// ------------------------------------------
// 7. Booking: Summary
// ------------------------------------------
// ------------------------------------------
// 7. Booking: Summary
// ------------------------------------------
function setupBookingSummary() {
    const user = db.getCurrentUser();
    if (!user) { window.location.href = 'login.jsp'; return; }

    const temp = db.getTempBooking();

    // Support Multi-Designer
    let designers = [];
    if (temp.designers) designers = temp.designers;
    else if (temp.designer) designers = [temp.designer];

    document.getElementById('summary-name').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('summary-email').textContent = user.email;
    document.getElementById('summary-designer').textContent = designers.join(', ');
    document.getElementById('summary-date').textContent = temp.date;
    document.getElementById('summary-time').textContent = temp.time;

    const phoneInput = document.getElementById('summary-phone');
    phoneInput.value = user.phone;

    const confirmBtn = document.getElementById('confirm-booking');
    confirmBtn.addEventListener('click', async () => {
        confirmBtn.disabled = true;
        confirmBtn.textContent = "Processing...";

        const notes = document.getElementById('summary-notes').value;
        const phone = phoneInput.value;
        const category = temp.category; // CSV or single

        // Create Bookings sequentially or parallel
        let successCount = 0;
        let errors = [];
        let lastId = 0;

        for (const dName of designers) {
            const bookingData = {
                client: `${user.firstName} ${user.lastName}`,
                email: user.email,
                phone: phone,
                designer: dName,
                date: temp.date,
                time: temp.time,
                category: category,
                notes: notes
            };

            try {
                const res = await fetch('api/bookings', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(bookingData)
                });
                const data = await res.json();

                if (data.success) {
                    successCount++;
                    lastId = data.id;
                } else {
                    errors.push(`${dName}: ${data.message}`);
                }
            } catch (err) {
                errors.push(`${dName}: Connection fail`);
            }
        }

        if (successCount === designers.length) {
            db.clearTempBooking();
            // Redirect to confirmation of the LAST one (or we could show a generic success).
            // Ideal: confirmation page showing list, but for now standard page.
            window.location.href = `booking-confirmation.jsp?id=${lastId}`;
        } else if (successCount > 0) {
            // Partial success
            showAlert(`Partially successful. Booked ${successCount}/${designers.length}. Errors: ${errors.join('; ')}`, () => {
                db.clearTempBooking();
                window.location.href = `booking-confirmation.jsp?id=${lastId}`;
            });
        } else {
            // All failed
            showAlert('Booking failed. ' + errors.join('; '));
            confirmBtn.disabled = false;
            confirmBtn.textContent = "Confirm Booking";
        }
    });
}

// ------------------------------------------
// 8. Booking: Confirmation
// ------------------------------------------
function setupBookingConfirmation() {
    const params = new URLSearchParams(window.location.search);
    const id = parseInt(params.get('id'));
    if (!id) {
        showAlert('No booking ID provided', () => { window.location.href = 'index.jsp'; });
        return;
    }

    // Fetch from API to get the latest booking details
    fetch('api/bookings')
        .then(res => {
            if (res.status === 401) {
                showAlert("Session expired. Please log in again.", () => { window.location.href = 'login.jsp'; });
                throw new Error("Unauthorized");
            }
            return res.json();
        })
        .then(bookings => {
            if (!Array.isArray(bookings)) {
                console.error("Expected array but got:", bookings);
                if (bookings.error) {
                    showAlert("Error: " + bookings.error);
                } else {
                    showAlert("Unexpected server response. Please try again.");
                }
                return;
            }
            const booking = bookings.find(b => b.id === id);

            if (booking) {
                document.getElementById('confirm-id').textContent = booking.id;
                document.getElementById('confirm-name').textContent = booking.client;
                document.getElementById('confirm-email').textContent = booking.email;
                document.getElementById('confirm-phone').textContent = booking.phone;
                document.getElementById('confirm-designer').textContent = booking.designer;
                document.getElementById('confirm-date').textContent = booking.date;
                document.getElementById('confirm-time').textContent = booking.time;
                document.getElementById('confirm-notes').textContent = booking.notes || '-';
            } else {
                showAlert('Booking not found in your history. Please check "My Bookings" in your profile.');
            }
        })
        .catch(err => {
            console.error('Error fetching booking details:', err);
            showAlert('Unable to load booking details.');
        });

    document.getElementById('print-booking').addEventListener('click', () => window.print());
}

// ------------------------------------------
// 8.5. Profile Page
// ------------------------------------------
function setupProfile() {
    const user = db.getCurrentUser();
    if (!user) { window.location.href = 'login.jsp'; return; }

    // --- Tab Logic ---
    const tabs = document.querySelectorAll('.tab-btn');
    const sections = document.querySelectorAll('.tab-content');

    function switchTab(targetId) {
        tabs.forEach(t => t.classList.remove('active'));
        sections.forEach(s => {
            s.classList.remove('active');
            s.style.display = 'none';
        });

        const activeTab = document.querySelector(`.tab-btn[data-target="${targetId}"]`);
        const activeSection = document.getElementById(targetId === 'bookings' ? 'my-bookings-section' : 'profile-settings-section');

        if (activeTab && activeSection) {
            activeTab.classList.add('active');
            activeSection.classList.add('active');
            activeSection.style.display = 'block';
        }
    }

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const target = tab.getAttribute('data-target');
            switchTab(target);
            // Update URL hash without jumping
            history.pushState(null, null, `#${target}`);
        });
    });

    // Check Hash on Load
    const hash = window.location.hash.replace('#', '');
    if (hash === 'settings') {
        switchTab('settings');
    } else {
        switchTab('bookings');
    }

    // --- Populate Settings Form ---
    document.getElementById('profile-firstname').value = user.firstName;
    document.getElementById('profile-lastname').value = user.lastName;
    document.getElementById('profile-email').value = user.email;
    document.getElementById('profile-phone').value = user.phone;
    document.getElementById('profile-address').value = user.address || '';

    // Handle Form Submit
    document.getElementById('profile-form').addEventListener('submit', (e) => {
        e.preventDefault();

        const firstName = document.getElementById('profile-firstname').value;
        const lastName = document.getElementById('profile-lastname').value;
        const phone = document.getElementById('profile-phone').value;
        const address = document.getElementById('profile-address').value;
        const password = document.getElementById('profile-password').value;

        const params = new URLSearchParams();
        params.append('firstName', firstName);
        params.append('lastName', lastName);
        params.append('phone', phone);
        params.append('address', address);
        if (password) {
            params.append('password', password);
        }

        fetch('UpdateProfileServlet', {
            method: 'POST',
            body: params
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    showAlert('Profile updated successfully!');
                    // Optionally update local user details if we want to keep `user` variable fresh without reload
                    user.firstName = firstName;
                    user.lastName = lastName;
                    user.phone = phone;
                    user.address = address;
                    // Password not stored in JS object usually
                } else {
                    showAlert('Error updating profile: ' + data.message);
                }
            })
            .catch(err => {
                console.error(err);
                showAlert('Error submitting update. Please try again.');
            });
    });

    // --- Populate Bookings List ---
    const bookingsList = document.getElementById('customer-bookings-list');
    bookingsList.innerHTML = '<p>Loading bookings...</p>';

    fetch('api/bookings')
        .then(res => res.json())
        .then(allBookings => {
            const bookings = allBookings.filter(b => b.email === user.email);
            bookingsList.innerHTML = '';

            if (bookings.length === 0) {
                bookingsList.innerHTML = '<p class="empty-state">No bookings found. <a href="booking-date.jsp">Book a consultation</a>.</p>';
            } else {
                bookings.forEach(booking => {
                    const card = document.createElement('div');
                    card.className = 'booking-card-item';
                    const statusClass = booking.status.toLowerCase();
                    card.innerHTML = `
                        <div class="booking-info">
                            <h4>${booking.designer}</h4>
                            <p>Date: ${booking.date} at ${booking.time}</p>
                            <p class="status-badge ${statusClass}">${booking.status}</p>
                            ${statusClass !== 'cancelled' && statusClass !== 'completed' ? `<button class="btn small cancel btn-red" style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%) !important; color: white !important; border: none !important; padding: 0.5rem 1rem !important; border-radius: 99px !important; font-weight: 600 !important;" onclick="window.cancelBookingFinal(${booking.id})">CANCEL BOOKING</button>` : ''}
                        </div>
                    `;
                    bookingsList.appendChild(card);
                });
            }
        })
        .catch(err => {
            console.error(err);
            bookingsList.innerHTML = '<p style="color:red">Error loading bookings.</p>';
        });
}

// ------------------------------------------
// 9. Admin Login
// ------------------------------------------
// function setupAdminLogin() { ... removed ... }

// ------------------------------------------
// 10. Admin Dashboard
// ------------------------------------------

// Helpers for New Modal
window.switchTab = function (tabName) {
    // Select tabs ONLY within the promote modal
    const modal = document.getElementById('promote-modal');
    if (!modal) return;

    const tabs = modal.querySelectorAll('.tab-btn');
    const panes = modal.querySelectorAll('.tab-pane');

    // Deactivate all
    tabs.forEach(t => t.classList.remove('active'));
    panes.forEach(p => p.classList.remove('active'));

    // Activate target by matching onclick attribute or data-tab
    tabs.forEach((tab, index) => {
        const onclick = tab.getAttribute('onclick');
        if (onclick && onclick.includes(`'${tabName}'`)) {
            tab.classList.add('active');
        }
    });

    const targetPane = modal.querySelector('#tab-' + tabName);
    if (targetPane) targetPane.classList.add('active');
};

window.toggleDesignerFields = function () {
    const role = document.getElementById('promote-role').value;
    const basicFields = document.getElementById('designer-basic-fields');
    const tabBtns = document.querySelectorAll('.tab-btn');

    if (role === 'ADMIN') {
        if (basicFields) basicFields.style.display = 'none';
        if (tabBtns[1]) tabBtns[1].style.display = 'none';
        if (tabBtns[2]) tabBtns[2].style.display = 'none';
    } else {
        if (basicFields) basicFields.style.display = 'block';
        if (tabBtns[1]) tabBtns[1].style.display = 'inline-block';
        if (tabBtns[2]) tabBtns[2].style.display = 'inline-block';
    }
};

window.toggleScheduleInputs = function () {
    // Placeholder for future logic
};

function setupAdminDashboard() {
    const user = db.getCurrentUser();
    // Check using Unified Role OR Legacy (fallback)
    const hasAccess = (user && (user.role === 'ADMIN' || user.role === 'DESIGNER')) || sessionStorage.getItem('admin_logged_in') === 'true';

    if (!hasAccess) {
        showAlert("Access Denied: Authorized personnel only.", () => { window.location.href = 'login.jsp'; });
        return;
    }

    // Sidebar Navigation
    document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            document.querySelectorAll('.sidebar-nav .nav-item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            document.querySelectorAll('.dashboard-section').forEach(s => s.classList.remove('active'));

            if (item.id === 'nav-overview') {
                document.getElementById('overview').classList.add('active');
                populateAdminOverview();
            } else if (item.id === 'nav-bookings') {
                document.getElementById('manage-bookings').classList.add('active');
                populateAdminBookings();
            } else if (item.id === 'nav-designers') {
                document.getElementById('manage-designers').classList.add('active');
                populateAdminDesigners();
            } else if (item.id === 'nav-designer-mgmt') {
                document.getElementById('designer-management').classList.add('active');
                populateDesignerManagement();
            }
        });
    });

    document.getElementById('nav-logout').addEventListener('click', (e) => {
        e.preventDefault();
        sessionStorage.removeItem('admin_logged_in');
        db.logout(); // Call shared logout to kill server session
    });

    // Initial Load
    populateAdminOverview();

    // Setup Add/Promote Designer Button
    const addDesignerBtn = document.getElementById('add-designer-btn');
    const modal = document.getElementById('promote-modal');
    const closeModal = document.querySelector('.close-modal');
    const promoteForm = document.getElementById('promote-form');

    // Open Modal
    if (addDesignerBtn) {
        // addDesignerBtn.textContent = "Promote / Add Staff"; // Keep original text
        addDesignerBtn.addEventListener('click', () => {
            if (window.openPromoteModal) {
                window.openPromoteModal();

                // Ensure Role Dropdown is VISIBLE for generic add staff
                const roleSelect = document.getElementById('promote-role');
                if (roleSelect && roleSelect.parentElement) {
                    roleSelect.parentElement.style.display = 'block';
                }
            } else {
                modal.style.display = "block";
            }
        });
    }

    // Close Modal
    if (closeModal) {
        closeModal.addEventListener('click', () => {
            modal.style.display = "none";
        });
    }
    window.onclick = function (event) {
        if (event.target == modal) {
            modal.style.display = "none";
        }
    }

    // Autocomplete Logic
    const searchInput = document.getElementById('promote-search');
    const resultsList = document.getElementById('search-results');
    const emailInput = document.getElementById('promote-email');
    const newUserFields = document.getElementById('new-user-fields');

    // Initially hide new user fields (show only if email is new)
    if (newUserFields) newUserFields.style.display = 'none';

    let debounceTimer;
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            const term = e.target.value;
            if (term.length < 2) {
                resultsList.style.display = 'none';
                return;
            }

            debounceTimer = setTimeout(() => {
                fetch(`api/search-users?term=${encodeURIComponent(term)}`)
                    .then(res => res.json())
                    .then(data => {
                        resultsList.innerHTML = '';
                        if (data.error) return;
                        if (Array.isArray(data) && data.length > 0) {
                            resultsList.style.display = 'block';
                            data.forEach(userObj => {
                                // Backend now returns explicit objects: {email, phone, firstName, lastName}
                                // Or fallback to old string (though we changed backend)
                                const email = userObj.email || userObj;
                                const phone = userObj.phone || '';
                                const fName = userObj.firstName || '';
                                const lName = userObj.lastName || '';

                                const li = document.createElement('li');
                                li.textContent = email + (phone ? ` (${phone})` : '');
                                li.addEventListener('click', () => {
                                    emailInput.value = email;
                                    searchInput.value = email;

                                    // Auto-fill Phone and Name
                                    const phoneField = document.getElementById('promote-phone');
                                    if (phoneField) phoneField.value = phone;

                                    const fNameInput = document.getElementById('promote-firstname');
                                    const lNameInput = document.getElementById('promote-lastname');
                                    if (fNameInput) fNameInput.value = fName;
                                    if (lNameInput) lNameInput.value = lName;

                                    resultsList.style.display = 'none';
                                    if (newUserFields) newUserFields.style.display = 'none'; // Existing user
                                });
                                resultsList.appendChild(li);
                            });
                        } else {
                            resultsList.style.display = 'none';
                        }
                    })
                    .catch(err => console.error(err));
            }, 300);
        });
    }

    // Show fields if email is manually typed (assumption: if they type it, it might be new)
    if (emailInput) {
        emailInput.addEventListener('input', () => {
            if (newUserFields) newUserFields.style.display = 'block';
        });
    }

    // Form Submit
    if (promoteForm) {
        promoteForm.addEventListener('submit', (e) => {
            e.preventDefault();

            // Gather Basic Fields
            const email = emailInput.value;
            const role = document.getElementById('promote-role').value;
            const specialty = document.getElementById('promote-specialty').value;
            const bio = document.getElementById('promote-bio').value;

            // Gather New Designer Fields
            // Fixed: Use correct IDs from admin-dashboard.jsp
            const empTypeElement = document.getElementById('designer-type'); // Currently missing in HTML
            const employmentType = empTypeElement ? empTypeElement.value : 'FULL'; // Default to FULL if missing

            const statusElement = document.getElementById('promote-status'); // Correct ID is promote-status
            const status = statusElement ? statusElement.value : 'Active';

            // Work Days removed as per user request
            const workDays = "";

            const workHoursStart = document.getElementById('work-start').value || "09:00";
            const workHoursEnd = document.getElementById('work-end').value || "17:00";
            const availability = document.getElementById('promote-availability') ? document.getElementById('promote-availability').value : "";

            // Gather Capacity Fields
            const maxHoursPerWeek = document.getElementById('max-hours') ? document.getElementById('max-hours').value : "40";
            const minHoursGuaranteed = document.getElementById('min-hours').value;
            const maxSimultaneousProjects = document.getElementById('max-projects').value;
            const maxBookingsPerWeek = document.getElementById('max-bookings').value;

            // New User Fields
            const firstName = document.getElementById('promote-firstname').value;
            const lastName = document.getElementById('promote-lastname').value;
            const password = document.getElementById('promote-password').value;

            // Basic Validation
            if (role === 'DESIGNER' && (!specialty)) {
                // Specialty is mandatory for designers in most cases
                if (specialty.trim() === '') {
                    showAlert("Specialty is required for Designers.");
                    return;
                }
            }

            let body = `email=${encodeURIComponent(email)}&role=${encodeURIComponent(role)}`;

            if (role === 'DESIGNER') {
                body += `&specialty=${encodeURIComponent(specialty)}&bio=${encodeURIComponent(bio)}` +
                    `&employmentType=${encodeURIComponent(employmentType)}&status=${encodeURIComponent(status)}` +
                    `&workDays=${encodeURIComponent(workDays)}&workHoursStart=${encodeURIComponent(workHoursStart)}` +
                    `&workHoursEnd=${encodeURIComponent(workHoursEnd)}&availability=${encodeURIComponent(availability)}` +
                    `&maxHoursPerWeek=${maxHoursPerWeek}&minHoursGuaranteed=${minHoursGuaranteed}` +
                    `&maxSimultaneousProjects=${maxSimultaneousProjects}&maxBookingsPerWeek=${maxBookingsPerWeek}`;
            }

            if (newUserFields && newUserFields.style.display !== 'none') {
                body += `&firstName=${encodeURIComponent(firstName)}&lastName=${encodeURIComponent(lastName)}&password=${encodeURIComponent(password)}`;
            }

            fetch('admin/approve-designer', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success || data.status === 'success') {
                        alert("Saved! Page will reload.");
                        location.reload();
                    } else {
                        showAlert('Error: ' + data.message);
                    }
                })
                .catch(err => {
                    console.error(err);
                    showAlert('An error occurred.');
                });
        });
    }

    // Designer Management Form Handler (Fix: Prevent Reload)
    const designerMgmtForm = document.getElementById('designer-mgmt-form');
    if (designerMgmtForm) {
        designerMgmtForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const btn = designerMgmtForm.querySelector('button[type="submit"]');
            const originalText = btn.textContent;
            btn.textContent = 'Saving...';
            btn.disabled = true;

            // Collect Data
            const id = document.getElementById('designer-email').dataset.id; // Stored on edit click
            const specialty = document.getElementById('designer-specialty').value;
            const bio = document.getElementById('designer-bio').value;

            // Schedule Data from Global Manager (if exists)
            // For now, simpler update:
            const params = new URLSearchParams();
            params.append('action', 'update_details');
            params.append('id', id);
            params.append('specialty', specialty);
            params.append('bio', bio);

            fetch('api/designer-mgmt', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        if (window.showToast) window.showToast('Designer updated successfully!', 'success');
                        else alert('Saved!');

                        document.getElementById('designer-mgmt-modal').style.display = 'none';
                        populateDesignerManagement(); // Refresh Table
                    } else {
                        if (window.showToast) window.showToast('Error: ' + data.error, 'error');
                        else alert('Error: ' + data.error);
                    }
                })
                .catch(err => {
                    console.error(err);
                    if (window.showToast) window.showToast('Connection Error', 'error');
                })
                .finally(() => {
                    btn.textContent = originalText;
                    btn.disabled = false;
                });
        });
    }
}



// ==========================================
// Schedule Management (Calendar)
// ==========================================
class ScheduleManager {
    constructor(containerId, headerId) {
        this.currentDate = new Date();
        this.currentDesignerId = null;
        this.availabilityMap = new Map(); // 'YYYY-MM-DD' -> boolean
        this.containerId = containerId || 'schedule-calendar';
        this.headerId = headerId || 'calendar-month-year';
    }

    init(designerId) {
        this.currentDesignerId = designerId;
        this.loadSchedule();
        this.renderCalendar(); // Initial render
    }

    renderCalendar() {
        const grid = document.getElementById(this.containerId);
        if (!grid) return;

        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth(); // 0-indexed

        // Header
        const monthNames = ["January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"];
        const headerTitle = document.getElementById(this.headerId);
        if (headerTitle) headerTitle.textContent = `${monthNames[month]} ${year}`;

        grid.innerHTML = '';

        // Day Headers
        const days = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
        days.forEach(d => {
            const el = document.createElement('div');
            el.className = 'calendar-day-header';
            el.style.textAlign = 'center';
            el.style.fontWeight = 'bold';
            el.style.fontSize = '0.8em';
            el.style.color = '#7f8c8d';
            el.style.paddingBottom = '5px';
            el.textContent = d;
            grid.appendChild(el);
        });

        // Calendar Days
        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();

        // Empty slots
        for (let i = 0; i < firstDay; i++) {
            const el = document.createElement('div');
            el.className = 'calendar-day empty';
            grid.appendChild(el);
        }

        // Days
        for (let d = 1; d <= daysInMonth; d++) {
            const el = document.createElement('div');
            el.className = 'calendar-day';
            el.textContent = d;

            const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
            el.dataset.date = dateStr;

            if (this.availabilityMap.get(dateStr)) {
                el.classList.add('available');
            }

            el.addEventListener('click', () => this.toggleDate(dateStr, el));
            grid.appendChild(el);
        }
    }

    loadSchedule() {
        if (!this.currentDesignerId) return;
        this.availabilityMap.clear();

        fetch(`api/schedule?designerId=${this.currentDesignerId}`)
            .then(res => res.json())
            .then(data => {
                if (Array.isArray(data)) {
                    data.forEach(item => {
                        // API returns { "date": "YYYY-MM-DD", "isAvailable": 1/0, ... }
                        if (item.isAvailable == 1 || item.isAvailable === true || item.isAvailable === 'true') {
                            this.availabilityMap.set(item.date, true);
                        }
                    });
                }
                this.renderCalendar();
            })
            .catch(err => console.error("Error loading schedule:", err));
    }

    toggleDate(dateStr, el) {
        if (!this.currentDesignerId) return;

        const wasAvailable = el.classList.contains('available');
        const newAvailable = !wasAvailable;

        // Optimistic
        if (newAvailable) el.classList.add('available');
        else el.classList.remove('available');
        this.availabilityMap.set(dateStr, newAvailable);

        const payload = JSON.stringify({
            designerId: this.currentDesignerId,
            date: dateStr,
            isAvailable: newAvailable
        });

        fetch('api/schedule', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: payload
        }).then(res => res.json())
            .then(data => {
                if (!data.success) {
                    // Revert
                    if (newAvailable) el.classList.remove('available');
                    else el.classList.add('available');
                    window.showAlert("Failed to update schedule: " + (data.message || 'Unknown error'));
                }
            })
            .catch(err => {
                console.error(err);
                if (newAvailable) el.classList.remove('available');
                else el.classList.add('available');
            });
    }
}

// Global Instances
window.scheduleManager = new ScheduleManager('schedule-calendar', 'calendar-month-year');
window.promoteScheduleManager = new ScheduleManager('schedule-calendar-promote', 'calendar-month-year-promote');

// Global Helpers
window.changeMonth = (delta) => {
    window.scheduleManager.currentDate.setMonth(window.scheduleManager.currentDate.getMonth() + delta);
    window.scheduleManager.renderCalendar();
    window.scheduleManager.loadSchedule();
};

window.changeMonthPromote = (delta) => {
    window.promoteScheduleManager.currentDate.setMonth(window.promoteScheduleManager.currentDate.getMonth() + delta);
    window.promoteScheduleManager.renderCalendar();
    window.promoteScheduleManager.loadSchedule();
};

// Global Helper
window.changeMonth = (delta) => {
    window.scheduleManager.currentDate.setMonth(window.scheduleManager.currentDate.getMonth() + delta);
    // Reload schedule for new month view? 
    // Usually availability fetch might be per month or all. 
    // My servlet fetches by Month if Month param is passed, or All?
    // UserDAO.getDesignerSchedule takes date.
    // Wait, ScheduleServlet GET:
    // String monthStr = request.getParameter("month"); // YYYY-MM
    // If present, filters by month. If not, returns all?
    // Let's check logic:
    // If month present -> fetch specific. Else -> fetch all future?
    // I didn't verify ScheduleServlet logic for "all".
    // Assuming for now I fetch all or I should update loadSchedule to pass month.
    // Let's pass month in loadSchedule to be safe if Servlet supports it.

    // For now, re-render + reload.
    window.scheduleManager.renderCalendar();
    window.scheduleManager.loadSchedule();
};


function populateAdminOverview() {
    fetch('api/bookings')
        .then(res => res.json())
        .then(bookings => {
            document.getElementById('total-bookings').textContent = bookings.length;

            // Use Local Date for "Today" comparison
            const now = new Date();
            const today = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0');

            const upcomingCount = bookings.filter(b => b.date > today).length;
            document.getElementById('upcoming-appointments').textContent = upcomingCount;

            const todayCount = bookings.filter(b => b.date === today).length;
            document.getElementById('today-consultations').textContent = todayCount;
        })
        .catch(err => console.error('Error loading overview:', err));
}

// Global filter state
window.currentBookingFilter = 'all';

window.filterBookings = (scope) => {
    window.currentBookingFilter = scope;

    // Update active card visual
    document.querySelectorAll('.stat-card').forEach(card => card.classList.remove('active'));

    // Actually our IDs are: card-total, card-upcoming, card-today
    // Scope passed: 'all', 'upcoming', 'today'

    const cardId = scope === 'all' ? 'card-total' : `card-${scope}`;
    const card = document.getElementById(cardId);
    if (card) card.classList.add('active');

    // 1. Switch View to Bookings List
    const navBookings = document.getElementById('nav-bookings');
    if (navBookings) {
        // Manually trigger the click logic if event listeners are set up for tabs
        // Or manually toggle classes if we know the structure

        // Option A: Simulate Click (Simplest if listeners exist)
        // navBookings.click(); 

        // Option B: Explicitly Toggle Classes (More robust if click toggles visibility)
        document.querySelectorAll('.dashboard-section').forEach(s => s.classList.remove('active'));
        document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

        document.getElementById('manage-bookings').classList.add('active');
        navBookings.classList.add('active');
    }

    // 2. Fetch and Filter
    populateAdminBookings(scope);
};

function populateAdminBookings(filter = 'all') {
    const tbody = document.getElementById('bookings-tbody');
    tbody.innerHTML = '<tr><td colspan="8">Loading...</td></tr>';

    fetch('api/bookings')
        .then(res => res.json())
        .then(bookings => {
            console.log('Bookings fetched:', bookings.length);
            tbody.innerHTML = '';

            // Filter Logic
            // Manual YYYY-MM-DD to avoid locale issues
            const now = new Date();
            const today = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0');

            console.log('Filter:', filter, 'Today:', today);

            let filteredBookings = bookings;

            if (filter === 'upcoming') {
                filteredBookings = bookings.filter(b => b.date > today);
            } else if (filter === 'today') {
                filteredBookings = bookings.filter(b => b.date === today);
            }

            console.log('Filtered Count:', filteredBookings.length);

            if (filteredBookings.length === 0) {
                tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding: 20px;">No ${filter === 'all' ? '' : filter} bookings found.</td></tr>`;
                return;
            }

            filteredBookings.forEach(booking => {
                const row = document.createElement('tr');
                const status = booking.status || 'Confirmed';

                // Modern status badge with navy blue for Confirmed
                let statusBadge = '';
                switch (status) {
                    case 'Confirmed':
                        statusBadge = `<span style="display: inline-flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: 16px; font-size: 0.85em; font-weight: 700; background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%); color: #0d47a1; border: 1px solid #1e40af;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ${status}</span>`;
                        break;
                    case 'Completed':
                        statusBadge = `<span style="display: inline-flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: 16px; font-size: 0.85em; font-weight: 700; background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%); color: #2e7d32; border: 1px solid #a5d6a7;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ${status}</span>`;
                        break;
                    case 'Pending':
                        statusBadge = `<span style="display: inline-flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: 16px; font-size: 0.85em; font-weight: 700; background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%); color: #e65100; border: 1px solid #ffcc80;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ${status}</span>`;
                        break;
                    case 'Cancelled':
                        statusBadge = `<span style="display: inline-flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: 16px; font-size: 0.85em; font-weight: 700; background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%); color: #c62828; border: 1px solid #ef9a9a;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ${status}</span>`;
                        break;
                    default:
                        statusBadge = `<span style="display: inline-flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: 16px; font-size: 0.85em; font-weight: 700; background: #f5f5f5; color: #666;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ${status}</span>`;
                }

                row.innerHTML = `
            <td style="font-weight: 700; color: #2c3e50; text-align: center;">${booking.id}</td>
            <td style="text-align: center;">
                <div style="font-weight: 600; color: #2c3e50;">${booking.client}</div>
            </td>
            <td style="color: #555; font-size: 0.9em; text-align: center;">${booking.phone}</td>
            <td style="text-align: center;">
                <div style="font-weight: 600; color: #0d47a1;">${booking.designer}</div>
            </td>
            <td style="text-align: center;">
                <div style="font-weight: 600;">${new Date(booking.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</div>
                <div style="font-size: 0.85em; color: #7f8c8d;">Created: ${new Date(booking.created).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</div>
            </td>
            <td style="font-weight: 600; color: #0d47a1; text-align: center;">${booking.time}</td>
            <td class="notes-cell" title="${booking.notes || ''}" style="max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; text-align: center;">${booking.notes || '-'}</td>
            <td style="text-align: center;">${statusBadge}</td>
            <td style="text-align: center;">
                <div style="display: flex; gap: 8px; justify-content: center;">
                    <button class="btn small edit" onclick="window.toggleBookingStatus(${booking.id}, '${status}')" title="Toggle Status" style="background: linear-gradient(135deg, #0d47a1 0%, #0a3d91 100%); color: white; border: none; padding: 6px 12px; border-radius: 6px; cursor: pointer; transition: all 0.2s;"><i class="fas fa-check"></i></button>
                    ${status !== 'Cancelled' ? `<button class="btn small cancel" onclick="window.cancelBookingFinal(${booking.id})" title="Cancel Booking" style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); color: white; border: none; padding: 6px 14px; border-radius: 6px; cursor: pointer; transition: all 0.2s; font-weight: 600;">Cancel</button>` : ''}
                </div>
            </td>
        `;
                tbody.appendChild(row);
            });
        })
        .catch(err => {
            console.error(err);
            tbody.innerHTML = '<tr><td colspan="8" style="color:red">Error loading bookings.</td></tr>';
        });
}
// Expose specific functions to window for onClick attributes (simpler for this refactor)
window.toggleBookingStatus = (id, currentStatus) => {
    const newStatus = currentStatus === 'Confirmed' ? 'Completed' : 'Confirmed';
    fetch(`api/bookings/update?id=${id}&status=${newStatus}`, { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                if (window.showToast) window.showToast('Booking status updated!', 'success');
                if (window.populateAdminBookings) populateAdminBookings();
                if (window.populateAdminOverview) populateAdminOverview();
                // Designer Refresh
                if (window.populateDesignerBookings && document.getElementById('my-bookings-tbody')) {
                    populateDesignerBookings();
                }
            } else {
                showAlert('Error updating status: ' + data.message);
            }
        });
};
window.cancelBookingFinal = (id) => {
    console.log("cancelBookingFinal executed for id:", id);
    // Explicitly check for showCustomConfirm availability
    if (typeof window.showCustomConfirm === 'function') {
        window.showCustomConfirm('Are you sure you want to cancel this booking?', () => {
            fetch(`api/bookings/update?id=${id}&status=Cancelled`, { method: 'POST' })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        if (window.showToast) window.showToast('Booking cancelled successfully', 'success');

                        // Refresh Logic
                        if (document.getElementById('admin-dashboard-page')) {
                            populateAdminBookings();
                            populateAdminOverview();
                        } else if (window.populateDesignerBookings && document.getElementById('my-bookings-tbody')) {
                            populateDesignerBookings();
                        } else {
                            if (window.setupProfile) setupProfile();
                        }
                    } else {
                        if (window.showToast) window.showToast('Error: ' + data.message, 'error');
                        else alert('Error: ' + data.message);
                    }
                })
                .catch(err => {
                    if (window.showToast) window.showToast('An error occurred', 'error');
                    else alert('An error occurred');
                });
        });
    } else {
        console.error("Window.showCustomConfirm is not defined!");
        // Force a different alert to prove we are running new code
        alert("System Error: Custom confirmation dialog failed to load. Please refresh the page.");
    }
};

function populateAdminDesigners() {
    const tbody = document.getElementById('designers-tbody');
    tbody.innerHTML = '<tr><td colspan="5">Loading...</td></tr>';

    // Fetch from Server
    fetch('api/designers?type=all&_=' + new Date().getTime())
        .then(res => res.json())
        .then(designers => {
            // Save to global scope for Edit function
            window.adminStaffList = designers;

            tbody.innerHTML = '';

            // Check for error object from server
            if (designers.error || !Array.isArray(designers)) {
                tbody.innerHTML = `<tr><td colspan="5" style="color:red">Error: ${designers.error || 'Invalid response'}</td></tr>`;
                return;
            }

            if (designers.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5">No staff found. Promote a user!</td></tr>';
                return;
            }

            designers.forEach(designer => {
                const row = document.createElement('tr');
                const isDesigner = (designer.role || '').toUpperCase() === 'DESIGNER';
                const role = designer.role || 'Unknown';

                // Role Badge
                let roleBadge = '';
                if (role.toUpperCase() === 'ADMIN') {
                    roleBadge = `<span style="display: inline-flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: 16px; font-size: 0.85em; font-weight: 700; background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%); color: #0d47a1; border: 1px solid #1e40af;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ADMIN</span>`;
                } else if (role.toUpperCase() === 'DESIGNER') {
                    roleBadge = `<span style="display: inline-flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: 16px; font-size: 0.85em; font-weight: 700; background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%); color: #6a1b9a; border: 1px solid #ce93d8;"><i class="fas fa-circle" style="font-size:0.5em;"></i> DESIGNER</span>`;
                } else {
                    roleBadge = `<span style="display: inline-flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: 16px; font-size: 0.85em; font-weight: 700; background: #f5f5f5; color: #666;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ${role}</span>`;
                }

                // Name Handling
                const displayName = designer.name || `${designer.firstName || ''} ${designer.lastName || ''}`.trim() || 'Unknown';

                let statusBadge = `<span style="color: #ccc;">-</span>`;
                let specialtyHtml = `<span style="color: #ccc;">-</span>`;
                let availabilityHtml = `<span style="color: #ccc;">-</span>`;

                if (isDesigner) {
                    // Status
                    const status = designer.status || 'Active';
                    const statusColor = (status === 'Active') ? '#2e7d32' : (status === 'Inactive' ? '#c62828' : '#f39c12');
                    statusBadge = `<span style="color: ${statusColor}; font-weight: 600;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ${status}</span>`;

                    // Specialty
                    specialtyHtml = `<span class="specialty-pill ${(designer.specialty || 'default').toLowerCase()}">${designer.specialty || '-'}</span>`;

                    // Availability
                    const avail = (designer.availability && designer.availability.length > 0)
                        ? designer.availability.join(', ')
                        : 'Not Set';
                    availabilityHtml = `<div style="font-size: 0.9em; color: #555;"><i class="fas fa-clock" style="color:#888; margin-right:4px;"></i> ${avail}</div>`;

                } else {
                    statusBadge = `<span style="color: #2e7d32; font-weight: 600;"><i class="fas fa-circle" style="font-size:0.5em;"></i> Active</span>`;
                }

                row.innerHTML = `
                    <td style="text-align: center;">
                        <div style="font-weight: 600; color: #333;">${displayName}</div>
                        <div style="font-size: 0.85em; color: #888;">${designer.email}</div>
                    </td>
                    <td style="text-align: center;">${roleBadge}</td>
                    <!-- Separated Specialty Column -->
                    <td style="text-align: center;">${specialtyHtml}</td>
                    <!-- Separated Joined Column -->
                    <td style="color: #666; font-size: 0.9em; text-align: center;">
                        <i class="fas fa-calendar-alt" style="color:#ccc; margin-right:4px;"></i>
                        ${designer.joinDate || '-'}
                    </td>
                    <!-- Separated Contact/Status Column -->
                    <td style="text-align: center;">
                        <div style="font-size: 0.9em; color: #555; margin-bottom: 4px;">
                            <i class="fas fa-phone" style="color:#aaa; margin-right:4px;"></i> 
                            ${designer.phone || 'N/A'}
                        </div>
                        ${statusBadge}
                    </td>
                    <td class="action-buttons" style="text-align: center !important; vertical-align: middle;">
                        <div style="width: 100%; text-align: center;">
                            ${role.toUpperCase() !== 'ADMIN' ? `
                            <button class="btn small edit" onclick="window.editStaff(${designer.id})" title="Edit" style="background: linear-gradient(135deg, #0d47a1 0%, #0a3d91 100%); color: white; border: none; padding: 6px 12px; border-radius: 6px; cursor: pointer; transition: all 0.2s; font-weight:bold; font-size:0.8em; display:inline-flex; align-items:center; gap:4px; margin-right: 8px;">
                                <i class="fas fa-pencil-alt"></i> Edit
                            </button>` : ''}
                            ${role.toUpperCase() !== 'ADMIN' ? `
                            <button class="btn small delete" data-staff-id="${designer.id}" title="Delete" style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); color: white; border: none; padding: 6px 12px; border-radius: 6px; cursor: pointer; transition: all 0.2s; font-weight:bold; font-size:0.8em; display:inline-flex; align-items:center; gap:4px;">
                                <i class="fas fa-trash-alt"></i> Delete
                            </button>` : ''}
                        </div>
                    </td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(err => {
            console.error('Error fetching staff:', err);
            tbody.innerHTML = '<tr><td colspan="5" style="color:red">Error loading staff.</td></tr>';
        });
}

// Event delegation for delete buttons in staff table
document.addEventListener('click', function (e) {
    if (e.target.closest('.btn.delete[data-staff-id]')) {
        const btn = e.target.closest('.btn.delete[data-staff-id]');
        const staffId = btn.getAttribute('data-staff-id');
        if (staffId && staffId !== 'undefined' && staffId !== 'null') {
            window.deleteStaff(parseInt(staffId));
        } else {
            console.error('No valid staff ID found on delete button:', staffId);
            if (window.showAlert) {
                showAlert('Error: Unable to delete staff member. No valid ID found.');
            }
        }
    }
});

window.toggleDesignerFields = () => {
    const roleSelect = document.getElementById('promote-role');
    const role = roleSelect ? roleSelect.value : 'DESIGNER';
    const designerFields = document.getElementById('designer-basic-fields');
    const staffId = document.getElementById('promote-staff-id').value;
    const isEdit = !!staffId;

    const modal = document.getElementById('promote-modal');
    if (!modal) return;

    const tabBar = modal.querySelector('.modal-tabs');
    const sectionHeader = document.getElementById('section-header-admin');

    if (role === 'DESIGNER') {
        if (designerFields) designerFields.style.display = 'block';

        // CONDITIONAL: Only show tabs if we are in EDIT mode
        if (tabBar) tabBar.style.display = isEdit ? 'flex' : 'none';

        // If tabs are hidden (Add Mode), show the Basic Info header
        if (sectionHeader) sectionHeader.style.display = isEdit ? 'none' : 'block';

        // Ensure schedule manager is visible if on schedule tab
        // Refresher
        if (window.ScheduleManager && window.ScheduleManager.renderCalendar) {
            // Only render if we have a valid ID to prevent 400 Bad Request (e.g. Add Staff Mode)
            if (window.ScheduleManager.currentDesignerId) {
                window.ScheduleManager.renderCalendar();
            }
        }
    } else {
        if (designerFields) designerFields.style.display = 'none';
        if (tabBar) tabBar.style.display = 'none';
        if (sectionHeader) sectionHeader.style.display = 'block';
    }
};

window.deleteStaff = (id) => {
    // console.log("Delete clicked for", id); // Debug

    // Explicitly check if showConfirm is available
    if (typeof showConfirm === 'function') {
        showConfirm('Are you sure you want to delete this staff member? This action cannot be undone.', () => {
            performDelete(id);
        });
    } else {
        // Fallback if modal script failed to load for some reason
        if (confirm('Are you sure you want to delete this staff member?')) {
            performDelete(id);
        }
    }
};

function performDelete(id) {
    fetch('admin/remove-staff', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `id=${id}`
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                if (window.showAlert) showAlert(data.message);
                else alert(data.message);

                populateAdminDesigners(); // Refresh list
            } else {
                if (window.showAlert) showAlert('Error: ' + data.message);
                else alert('Error: ' + data.message);
            }
        })
        .catch(err => {
            console.error(err);
            if (window.showAlert) showAlert('An error occurred during deletion.');
            else alert('An error occurred.');
        });
}

// [Deleted duplicate editStaff function]

// Reset function for "Promote User" button
window.openPromoteModal = () => {
    const modal = document.getElementById('promote-modal');
    const form = document.getElementById('promote-form');

    // Explicitly clear the hidden ID to force "Add" mode
    if (document.getElementById('promote-staff-id')) {
        document.getElementById('promote-staff-id').value = '';
    }

    // Explicitly hide tabs (Add Mode default)
    const tabs = document.querySelectorAll('.modal-tabs');
    tabs.forEach(t => t.style.display = 'none');

    form.reset();
    document.getElementById('promote-email').readOnly = false;
    document.getElementById('new-user-fields').style.display = 'none'; // Initially hidden

    // Reset tabs
    window.switchTab('basic');
    window.toggleDesignerFields();

    // Ensure Role Dropdown is visible by default (unless hidden by specific caller)
    const roleSelect = document.getElementById('promote-role');
    if (roleSelect && roleSelect.parentElement) {
        roleSelect.parentElement.style.display = 'block';
    }

    modal.style.display = 'block';
};

// ==========================================
// Designer Management Section (NEW)
// ==========================================
// Edit Designer (Specialized for Designer Mgmt Tab)
window.editDesigner = (id) => {
    // Find in the list loaded by populateDesignerManagement
    const designer = window.designerMgmtList ? window.designerMgmtList.find(d => d.id === id) : null;
    if (!designer) {
        console.error("Designer not found in local list");
        return;
    }

    const modal = document.getElementById('designer-mgmt-modal');
    if (!modal) return;

    // Populate Fields
    document.getElementById('designer-email').value = designer.email || '';
    document.getElementById('designer-name').value = designer.name || `${designer.firstName} ${designer.lastName}`;
    document.getElementById('designer-specialty').value = designer.specialty || '';
    document.getElementById('designer-bio').value = designer.bio || '';

    // Init Calendar
    // Init Calendar
    if (window.ScheduleManager) {
        window.ScheduleManager.init(id);
    }

    modal.style.display = 'block';
};

let currentDesignerFilter = 'all';

function populateDesignerManagement(filterType = 'all') {
    currentDesignerFilter = filterType;
    const tbody = document.getElementById('designer-mgmt-tbody');
    tbody.innerHTML = '<tr><td colspan="5">Loading...</td></tr>';

    fetch('api/designers?type=all&_=' + new Date().getTime())
        .then(res => res.json())
        .then(designers => {
            window.designerMgmtList = designers;
            tbody.innerHTML = '';

            // Filter only designers (not admins)
            let filteredDesigners = designers.filter(d => (d.role || '').toUpperCase() === 'DESIGNER');

            // Apply type filter
            if (filterType === 'part') {
                filteredDesigners = filteredDesigners.filter(d => d.employmentType === 'PART');
            } else if (filterType === 'full') {
                filteredDesigners = filteredDesigners.filter(d => d.employmentType === 'FULL');
            }

            if (filteredDesigners.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 20px;">No designers found</td></tr>';
                return;
            }

            filteredDesigners.forEach(designer => {
                const row = document.createElement('tr');

                // Status with color-coded styling
                const status = designer.status || 'Active';
                let statusColor = '#27ae60';
                if (status === 'Full' || status === 'On Leave') statusColor = '#f39c12'; // Fixed: On Leave = Orange
                if (status === 'Inactive') statusColor = '#e74c3c';
                const statusHtml = `<span style="color: ${statusColor}; font-weight: 600; font-size: 0.95em;"><i class="fas fa-circle" style="font-size:0.5em;"></i> ${status}</span>`;

                // Capacity (assuming we track current vs max projects)
                const currentProj = designer.currentProjects || 0;
                // FIX: Use correct property name from JSON (maxProjects)
                const maxProj = designer.maxProjects ?? 5;
                const capacityPerc = maxProj > 0 ? (currentProj / maxProj) * 100 : 0;
                let capacityColor = '#27ae60';
                if (capacityPerc > 80) capacityColor = '#e74c3c';
                else if (capacityPerc > 60) capacityColor = '#f39c12';

                const capacityHtml = `<div style="display: flex; align-items: center; justify-content: center; gap: 10px;">
                    <span style="font-weight: 600; color: ${capacityColor};">${currentProj}/${maxProj}</span>
                    <div style="display: flex; justify-content: flex-start; width: 60px; height: 6px; background: #e0e0e0; border-radius: 10px; overflow: hidden; margin: 0 !important; text-align: left !important;">
                        <div style="width: ${capacityPerc}%; height: 100%; background: ${capacityColor}; border-radius: 10px; transition: width 0.3s ease; margin: 0 !important; display: block;"></div>
                    </div>
                </div>`;

                // Name: Use designer.name if available, else combine first/last
                const displayName = designer.name || `${designer.firstName || ''} ${designer.lastName || ''}`.trim() || 'Unknown';

                // Specialty
                const specialtyHtml = `<span class="specialty-pill ${(designer.specialty || 'default').toLowerCase()}">${designer.specialty || '-'}</span>`;

                // Availability
                // Availability - Show BOTH Time and Specific Dates
                let availHtml = '';

                // 2. Upcoming Dates
                let hasDates = false;
                if (designer.upcomingDates && designer.upcomingDates.length > 0) {
                    hasDates = true;
                    const dateStr = designer.upcomingDates.map(d => {
                        const [y, m, day] = d.split('-').map(Number);
                        const date = new Date(y, m - 1, day);
                        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                    }).join(', ');
                    availHtml += `<div style="font-size: 0.9em; color: #0277bd; margin-top:4px;"><i class="fas fa-calendar-alt" style="color:#0288d1; margin-right:4px;"></i> ${dateStr}</div>`;
                }

                // 1. Standard Availability (Time / Manual Text)
                const availJoined = (designer.availability && designer.availability.length > 0) ? designer.availability.join(', ') : 'Not Set';

                // Logic: Only show standard time line IF:
                // It is NOT the default "09:00 - 17:00" OR there are NO specific dates.
                // If it IS "09:00 - 17:00" AND we have dates, we HIDE it.
                // If the user typed manual text (e.g. "Call me"), availJoined will be "Call me" (logic in backend handles hiding 9-17 if manual text exists).
                // So here we only need to check if the RESULTING string contains "09:00 - 17:00".

                let showStandard = true;
                if (hasDates && availJoined.includes('09:00 - 17:00') && designer.availability.length === 1) {
                    showStandard = false;
                }

                if (showStandard) {
                    // Prepend time/text ABOVE the dates? 
                    // Previous code appended dates AFTER this.
                    // We need to insert this BEFORE the dates div we just made.
                    // Actually, simpler: construct html string in order.

                    let timeHtml = `<div style="font-size: 0.9em; color: #555;"><i class="fas fa-clock" style="color:#888; margin-right:4px;"></i> ${availJoined}</div>`;
                    availHtml = timeHtml + availHtml;
                }
                const availabilityHtml = availHtml;

                row.innerHTML = `
                    <td>
                        <div style="display: flex; flex-direction: column; gap: 4px;">
                            <strong style="color: #2c3e50; font-size: 1em;">${displayName}</strong>
                            <small style="color: #95a5a6;">${designer.email}</small>
                        </div>
                    </td>
                    <!-- Bio Column -->
                    <td style="color: #666; font-size: 0.9em; max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${designer.bio || ''}">
                        ${designer.bio || '-'}
                    </td>
                    <!-- Specialty Column -->
                    <td>${specialtyHtml}</td>
                    <!-- Load Column (Added) -->
                    <td>
                        <div style="font-size: 0.9em; font-weight: 600; color: #555;">${designer.currentProjects || designer.projects || 0} active</div>
                    </td>
                    <td>
                        <div style="font-size: 0.9em; font-weight: 600; color: #2e7d32;">${designer.completedProjects || 0} completed</div>
                    </td>
                    <td>
                        <div style="margin-bottom: 4px;">${statusHtml}</div>
                        ${availabilityHtml}
                    </td>
                    <td>${capacityHtml}</td>
                `;
                tbody.appendChild(row);
            });

        })
        .catch(err => {
            console.error(err);
            tbody.innerHTML = '<tr><td colspan="5" style="color:red">Error loading designers</td></tr>';
        });
}

window.openDesignerMgmtModal = (designerId) => {
    const designer = window.designerMgmtList.find(d => d.id === designerId);
    if (!designer) return;

    const modal = document.getElementById('designer-mgmt-modal');

    // Populate Basic Info
    document.getElementById('designer-email').value = designer.email;
    document.getElementById('designer-name').value = designer.name;
    document.getElementById('designer-specialty').value = designer.specialty || '';
    document.getElementById('designer-bio').value = designer.bio || '';

    // Populate Schedule (Calendar)
    document.getElementById('designer-status-mgmt').value = designer.status || 'Active';

    // Init Calendar
    window.scheduleManager.init(designerId);

    // Reset to Basic tab
    switchDesignerTab('basic');

    modal.style.display = 'block';
};

// Tab Switching for Designer Modal
window.switchDesignerTab = function (tabName) {
    const tabs = document.querySelectorAll('#designer-mgmt-modal .tab-btn');
    const panes = document.querySelectorAll('#designer-mgmt-modal .tab-pane');

    tabs.forEach(t => t.classList.remove('active'));
    panes.forEach(p => p.classList.remove('active'));

    const targetTab = Array.from(tabs).find(t => t.onclick.toString().includes(`'${tabName}'`));
    if (targetTab) targetTab.classList.add('active');

    const targetPane = document.getElementById('designer-tab-' + tabName);
    if (targetPane) targetPane.classList.add('active');
};

// Toggle Part-Time / Full-Time fields - REMOVED

// Add event listeners for employment type radio buttons - REMOVED
document.addEventListener('DOMContentLoaded', () => {
    // employment type listeners removed


    // Filter buttons
    const filterDropdown = document.getElementById('designer-filter-dropdown');
    const filterPartBtn = document.getElementById('filter-part-btn');
    const filterFullBtn = document.getElementById('filter-full-btn');

    if (filterDropdown) {
        filterDropdown.addEventListener('change', (e) => {
            populateDesignerManagement(e.target.value);
        });
    }

    if (filterPartBtn) {
        filterPartBtn.addEventListener('click', () => {
            filterPartBtn.classList.add('active');
            filterFullBtn.classList.remove('active');
            populateDesignerManagement('part');
        });
    }

    if (filterFullBtn) {
        filterFullBtn.addEventListener('click', () => {
            filterFullBtn.classList.add('active');
            filterPartBtn.classList.remove('active');
            populateDesignerManagement('full');
        });
    }

    // Close modal
    const closeDesignerModal = document.querySelector('.close-designer-modal');
    if (closeDesignerModal) {
        closeDesignerModal.addEventListener('click', () => {
            document.getElementById('designer-mgmt-modal').style.display = 'none';
        });
    }
});

window.deleteDesigner = (id) => {
    showConfirm('Delete designer?', () => {
        db.deleteDesigner(id);
        populateAdminDesigners();
    });
};

// ==========================================
// Custom Alert/Confirm System
// ==========================================
window.showAlert = (message, callback) => {
    const modal = document.getElementById('custom-global-modal');
    const title = document.getElementById('custom-modal-title');
    const msg = document.getElementById('custom-modal-message');
    const footer = document.getElementById('custom-modal-footer');

    if (!modal) { originalAlert(message); if (callback) callback(); return; }

    title.textContent = 'Alert';
    msg.textContent = message;

    footer.innerHTML = `<button class="custom-btn custom-btn-primary" id="custom-alert-ok">OK</button>`;

    const btn = footer.querySelector('button');
    btn.onclick = () => {
        window.closeCustomModal();
        if (callback) callback();
    };

    modal.classList.add('show');
};

window.showConfirm = (message, onConfirm, onCancel) => {
    const modal = document.getElementById('custom-global-modal');
    const title = document.getElementById('custom-modal-title');
    const msg = document.getElementById('custom-modal-message');
    const footer = document.getElementById('custom-modal-footer');

    if (!modal) { if (originalConfirm(message)) onConfirm(); else if (onCancel) onCancel(); return; }

    title.textContent = 'Confirm';
    msg.textContent = message;

    footer.innerHTML = `
                <button class="custom-btn custom-btn-secondary" id="custom-modal-cancel">Cancel</button>
                    <button class="custom-btn custom-btn-primary" id="custom-modal-confirm">Confirm</button>
            `;

    document.getElementById('custom-modal-cancel').onclick = () => {
        window.closeCustomModal();
        if (onCancel) onCancel();
    };

    document.getElementById('custom-modal-confirm').onclick = () => {
        window.closeCustomModal();
        if (onConfirm) onConfirm();
    };

    modal.classList.add('show');
};

window.closeCustomModal = () => {
    const modal = document.getElementById('custom-global-modal');
    if (modal) modal.classList.remove('show');
};

// ==========================================
// Initialization
// ==========================================// --- INITIALIZATION ---
document.addEventListener('DOMContentLoaded', () => {
    console.log("Script loaded, initializing...");
    // Designer Dashboard
    if (document.getElementById('designer-dashboard')) {
        setupDesignerDashboard();
    }

    // Admin Dashboard
    if (document.getElementById('admin-dashboard-page')) {
        console.log('Admin Dashboard detected');
        setupAdminDashboard();
    }

    // Customer Profile
    // We check for elements unique to profile.jsp
    if (document.getElementById('profile-form') || document.querySelector('.profile-container')) {
        console.log('Profile Page detected');
        if (typeof setupProfile === 'function') setupProfile();
    }

    // Booking Flow (if functions exist)
    if (document.getElementById('booking-date-container')) {
        // setupBooking(); // If such function exists
    }
});

// Edit Staff Implementation
window.editStaff = (id) => {
    console.log("editStaff called for ID:", id);
    const staff = window.adminStaffList.find(s => s.id === id);
    console.log("Staff found:", staff);

    if (!staff) {
        alert('Staff member not found.');
        return;
    }

    // Populate Modal
    document.getElementById('promote-staff-id').value = staff.id;
    document.getElementById('promote-email').value = staff.email;
    document.getElementById('promote-phone').value = staff.phone || '';
    document.getElementById('promote-role').value = staff.role.toUpperCase();

    // Name Split (Naive)
    const nameParts = (staff.name || '').split(' ');
    document.getElementById('promote-firstname').value = nameParts[0] || '';
    document.getElementById('promote-lastname').value = nameParts.slice(1).join(' ') || '';

    // Designer Fields
    if (staff.role.toUpperCase() === 'DESIGNER') {
        const specSelect = document.getElementById('promote-specialty');
        if (specSelect) specSelect.value = staff.specialty || '';
        document.getElementById('promote-bio').value = staff.bio || '';

        // Capacity Fields
        if (document.getElementById('max-hours')) document.getElementById('max-hours').value = staff.maxHours || 40;
        if (document.getElementById('min-hours')) document.getElementById('min-hours').value = staff.minHours || 0;
        if (document.getElementById('max-projects')) document.getElementById('max-projects').value = staff.maxProjects || 5;
        if (document.getElementById('max-bookings')) document.getElementById('max-bookings').value = staff.maxBookings || 20;

        console.log("Staff is Designer. DesignerID:", staff.designerId);
        // Init Calendar if exists
        setTimeout(() => {
            console.log("Attempting to init ScheduleManager...");
            if (window.ScheduleManager) {
                window.ScheduleManager.init(staff.designerId);
            } else {
                console.error("ScheduleManager is UNDEFINED!");
            }
        }, 100);
    }

    // UI Updates
    const modal = document.getElementById('promote-modal');
    modal.style.display = 'block';
    if (window.toggleDesignerFields) window.toggleDesignerFields();

    // Update Button Text
    const submitBtn = document.querySelector('#promote-form button[type="submit"]');
    if (submitBtn) submitBtn.textContent = 'Save Changes';
};

// Custom Dropdown Logic for Specialties
window.toggleAppSpecialties = (e) => {
    e.stopPropagation();
    const options = document.getElementById('specialty-options');
    if (options) options.classList.toggle('active');
};

window.updateAppSpecialtyDisplay = () => {
    const container = document.getElementById('specialty-options');
    const display = document.querySelector('#specialty-multiselect .select-box .text');
    const hiddenInput = document.getElementById('promote-specialty');

    if (!container || !display || !hiddenInput) return;

    const checked = Array.from(container.querySelectorAll('input[type="checkbox"]:checked'))
        .map(cb => cb.value);

    if (checked.length === 0) {
        display.textContent = 'Select Specialty...';
        display.style.color = '#777';
    } else {
        display.textContent = checked.join(', ');
        display.style.color = '#333';
    }

    hiddenInput.value = checked.join(', ');
};

document.addEventListener('click', (e) => {
    const multiselect = document.getElementById('specialty-multiselect');
    const options = document.getElementById('specialty-options');
    if (multiselect && !multiselect.contains(e.target) && options) {
        options.classList.remove('active');
    }
});

// Handle Promote/Edit Form Submission
document.addEventListener('DOMContentLoaded', () => {
    const promoteForm = document.getElementById('promote-form');
    // Ensure Add Staff button works correctly
    const addStaffBtn = document.getElementById('add-designer-btn');
    if (addStaffBtn) {
        addStaffBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (window.openPromoteModal) window.openPromoteModal();
        });
    }

    // Explicitly expose submit function to bypass form default submission issues
    window.submitPromoteForm = function () {
        const promoteForm = document.getElementById('promote-form');
        if (!promoteForm) return;

        const id = document.getElementById('promote-staff-id').value;
        const isEdit = !!id;

        const formData = new URLSearchParams();
        formData.append('id', id);
        formData.append('email', document.getElementById('promote-email').value);
        formData.append('role', document.getElementById('promote-role').value);
        formData.append('firstName', document.getElementById('promote-firstname').value);
        formData.append('lastName', document.getElementById('promote-lastname').value);
        formData.append('phone', document.getElementById('promote-phone').value);
        formData.append('status', document.getElementById('promote-status').value);

        if (document.getElementById('promote-role').value === 'DESIGNER') {
            formData.append('specialty', document.getElementById('promote-specialty').value);
            formData.append('bio', document.getElementById('promote-bio').value);
            formData.append('maxHoursPerWeek', document.getElementById('max-hours').value);
            formData.append('minHoursGuaranteed', document.getElementById('min-hours').value);
            formData.append('maxSimultaneousProjects', document.getElementById('max-projects').value);
            formData.append('maxBookingsPerWeek', document.getElementById('max-bookings').value);
            formData.append('workHoursStart', document.getElementById('work-start').value);
            formData.append('workHoursEnd', document.getElementById('work-end').value);
            formData.append('availability', document.getElementById('promote-availability').value);
        }

        const endpoint = 'admin/approve-designer';

        fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData
        })
            .then(res => res.json())
            .then(data => {
                if (data.success || data.status === 'success') {
                    document.getElementById('promote-modal').style.display = 'none';
                    if (window.populateAdminDesigners) window.populateAdminDesigners();
                    if (window.showToast) window.showToast('Staff saved successfully!', 'success');
                    else alert('Staff saved successfully!');
                } else {
                    if (window.showToast) window.showToast(data.message, 'error');
                    else alert('Error: ' + data.message);
                }
            })
            .catch(err => {
                console.error(err);
                if (window.showToast) window.showToast('Network error occurred', 'error');
            });
    };

    if (promoteForm) {
        promoteForm.addEventListener('submit', (e) => {
            e.preventDefault();

            const id = document.getElementById('promote-staff-id').value;
            const isEdit = !!id;

            const formData = new URLSearchParams();
            formData.append('id', id);
            formData.append('email', document.getElementById('promote-email').value);
            formData.append('role', document.getElementById('promote-role').value);
            formData.append('firstName', document.getElementById('promote-firstname').value);
            formData.append('lastName', document.getElementById('promote-lastname').value);
            formData.append('phone', document.getElementById('promote-phone').value);
            formData.append('status', document.getElementById('promote-status').value);

            if (document.getElementById('promote-role').value === 'DESIGNER') {
                // Multiple Specialties Logic
                formData.append('specialty', document.getElementById('promote-specialty').value);

                formData.append('bio', document.getElementById('promote-bio').value);

                // Capacity - Use Correct Parameter Names for Servlet
                formData.append('maxHoursPerWeek', document.getElementById('max-hours').value);
                formData.append('minHoursGuaranteed', document.getElementById('min-hours').value);
                formData.append('maxSimultaneousProjects', document.getElementById('max-projects').value);
                formData.append('maxBookingsPerWeek', document.getElementById('max-bookings').value);

                // Schedule
                formData.append('workHoursStart', document.getElementById('work-start').value);
                formData.append('workHoursEnd', document.getElementById('work-end').value);
                formData.append('availability', document.getElementById('promote-availability').value);

                // Working Days logic removed (replaced by direct Calendar updates)
                // formData.append('workDays', ...); 

                // Note: Schedule updates (Calendar) are handled per-click, so we don't submit them here.
            }

            const endpoint = 'admin/approve-designer';

            fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success || data.status === 'success') {
                        document.getElementById('promote-modal').style.display = 'none';
                        if (window.populateAdminDesigners) window.populateAdminDesigners();
                        if (window.showToast) window.showToast('Staff saved successfully!', 'success');
                        else alert('Staff saved successfully!');
                    } else {
                        alert('Error: ' + data.message);
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert('An error occurred.');
                });
        });
    }
});

// [Restored editStaff]
window.editStaff = (id) => {
    console.log("editStaff called for ID:", id);
    if (!window.adminStaffList) {
        console.error("Admin Staff List not loaded!");
        return;
    }

    // Convert id to number just in case
    const staffId = parseInt(id);

    // Look in BOTH lists to find the most relevant/fresh data
    // Prioritize designerMgmtList if we are likely on that tab, or just check both.
    let staff = null;
    if (window.designerMgmtList) {
        staff = window.designerMgmtList.find(s => s.id === staffId);
    }
    if (!staff && window.adminStaffList) {
        staff = window.adminStaffList.find(s => s.id === staffId);
    }

    console.log("Staff found:", staff);

    if (!staff) {
        alert('Staff member not found. Please refresh the page.');
        return;
    }

    const modal = document.getElementById('promote-modal');
    if (!modal) return;

    // Reset Form
    document.getElementById('promote-form').reset();
    document.getElementById('promote-staff-id').value = staff.id;

    // Basic Fields
    document.getElementById('promote-email').value = staff.email;
    document.getElementById('promote-email').readOnly = true;

    // NEW: Populate Phone
    document.getElementById('promote-phone').value = staff.phone || '';

    // NEW: Populate Name Fields safely
    const fNameInput = document.getElementById('promote-firstname');
    const lNameInput = document.getElementById('promote-lastname');
    if (fNameInput) fNameInput.value = staff.firstName || '';
    if (lNameInput) lNameInput.value = staff.lastName || '';


    // Role
    const role = (staff.role || 'DESIGNER').toUpperCase();
    document.getElementById('promote-role').value = role;

    // Status
    document.getElementById('promote-status').value = staff.status || 'Active';

    // Hide Password/New User fields
    const newUserFields = document.getElementById('new-user-fields');
    if (newUserFields) newUserFields.style.display = 'none';

    // Show/Hide tabs based on role
    const isAdmin = role === 'ADMIN';
    const tabButtons = modal.querySelectorAll('.modal-tabs .tab-btn');
    const tabBar = modal.querySelector('.modal-tabs');
    const sectionHeader = modal.querySelector('#section-header-admin');

    if (isAdmin) {
        if (tabBar) tabBar.style.display = 'none';
        if (sectionHeader) sectionHeader.style.display = 'block';
    } else {
        if (tabBar) tabBar.style.display = 'flex';
        if (sectionHeader) sectionHeader.style.display = 'none';

        // Show scheduler/capacity tab buttons
        if (tabButtons) tabButtons.forEach(btn => btn.style.display = 'inline-block');
    }

    // Designer Fields
    if (role === 'DESIGNER') {
        // Multi-select population
        // Update Custom Dropdown Checkboxes
        const container = document.getElementById('specialty-options');
        if (container) {
            const inputs = container.querySelectorAll('input[type="checkbox"]');
            inputs.forEach(input => input.checked = false);

            if (staff.specialty) {
                const specs = staff.specialty.split(',').map(s => s.trim());
                inputs.forEach(input => {
                    if (specs.includes(input.value)) input.checked = true;
                });
            }
            // Trigger visual update to apply classes
            // Trigger visual update to apply classes
            if (window.updateAppSpecialtyDisplay) window.updateAppSpecialtyDisplay();
        }
        document.getElementById('promote-bio').value = staff.bio || '';

        // Capacity
        if (document.getElementById('max-hours')) document.getElementById('max-hours').value = staff.maxHours ?? 40;
        if (document.getElementById('min-hours')) document.getElementById('min-hours').value = staff.minHours ?? 0;
        if (document.getElementById('max-projects')) document.getElementById('max-projects').value = staff.maxProjects ?? 99; // Changed default to 99 for debug
        if (document.getElementById('max-bookings')) document.getElementById('max-bookings').value = staff.maxBookings ?? 20;

        // Schedule (Shift Hours)
        if (document.getElementById('work-start')) document.getElementById('work-start').value = staff.workHoursStart || '09:00';
        if (document.getElementById('work-end')) document.getElementById('work-end').value = staff.workHoursEnd || '17:00';

        // Init Calendar using DESIGNER ID
        // Note: Working Days checkboxes are replaced by Calendar view
        console.log("Initializing Calendar for DesignerID:", staff.designerId);

        if (window.ScheduleManager) {
            if (staff.designerId) {
                // FORCE use of the Singleton ScheduleManager (defined at bottom of file)
                // because changeMonthPromote uses it.
                window.ScheduleManager.init(staff.designerId);
            } else {
                console.error("Creating ScheduleManager init failed: staff.designerId is missing/zero");
            }
        }
    }

    // Trigger UI updates
    if (window.toggleDesignerFields) window.toggleDesignerFields();
    if (window.switchTab) window.switchTab('basic');

    modal.style.display = 'block';
};

// Schedule Manager Logic
console.log("Defining ScheduleManager...");
window.ScheduleManager = {
    currentDesignerId: null,
    currentDate: new Date(),

    init: function (designerId) {
        this.currentDesignerId = designerId;
        this.renderCalendar();
    },

    renderCalendar: function () {
        console.log("renderCalendar: currentDesignerId:", this.currentDesignerId, "Type:", typeof this.currentDesignerId);

        if (!this.currentDesignerId || this.currentDesignerId === 'undefined') {
            console.warn("ScheduleManager: No valid Designer ID set. Skipping fetch.");
            // Clear calendar or show message
            const container = document.getElementById('schedule-calendar-promote');
            if (container) container.innerHTML = '<div style="padding:20px;text-align:center;color:#888;">No Designer Selected</div>';
            return;
        }

        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        console.log("Rendering Calendar:", year, month);

        // Update header
        const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        if (document.getElementById('calendar-month-year-promote'))
            document.getElementById('calendar-month-year-promote').textContent = `${monthNames[month]} ${year}`;

        // Fetch data
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}`;
        console.log("Fetching schedule for:", dateStr);

        fetch(`api/schedule?designerId=${this.currentDesignerId}&month=${dateStr}`)
            .then(res => res.json())
            .then(data => {
                console.log("Schedule Data Received:", data);
                this.drawGrid(year, month, data);
            })
            .catch(err => console.error("Error fetching schedule:", err));
    },

    drawGrid: function (year, month, scheduleData) {
        const container = document.getElementById('schedule-calendar-promote');
        container.innerHTML = '';

        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();

        // Map schedule data
        const availabilityMap = {};
        if (Array.isArray(scheduleData)) {
            scheduleData.forEach(item => {
                // item.date is likely YYYY-MM-DD
                // If DB returns timestamp, substring it
                let d = item.date;
                if (d && d.length > 10) d = d.substring(0, 10);

                availabilityMap[d] = item.available;
            });
        } else {
            console.error("Schedule Data is not an array:", scheduleData);
        }

        console.log("Availability Map:", availabilityMap);

        // Empty slots
        for (let i = 0; i < firstDay; i++) {
            const div = document.createElement('div');
            div.className = 'calendar-day empty';
            container.appendChild(div);
        }

        // Days
        for (let day = 1; day <= daysInMonth; day++) {
            const div = document.createElement('div');
            div.className = 'calendar-day';
            div.textContent = day;

            const dateKey = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const isAvailable = availabilityMap[dateKey];

            if (isAvailable) {
                div.style.backgroundColor = '#c6a87c'; // Available color
                div.style.color = 'white';
            } else {
                div.style.backgroundColor = '#e0e0e0'; // Unavailable
                div.style.color = '#666';
            }

            div.onclick = () => this.toggleDate(day, !isAvailable);
            container.appendChild(div);
        }
    },

    toggleDate: function (day, makeAvailable) {
        console.log("toggleDate called for day:", day);
        if (!this.currentDesignerId) {
            console.error("toggleDate aborted: currentDesignerId is missing!");
            if (window.showToast) window.showToast("System Error: No Designer ID loaded.", 'error');
            else alert("System Error: No Designer ID loaded.");
            return;
        }

        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

        console.log("Toggling:", dateStr, "to", makeAvailable);

        const params = new URLSearchParams();
        params.append('designerId', this.currentDesignerId);
        params.append('date', dateStr);
        params.append('available', makeAvailable);
        // Default times for quick toggle
        params.append('startTime', '09:00');
        params.append('endTime', '17:00');

        fetch('api/schedule', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
            .then(res => res.json())
            .then(data => {
                console.log("Update Response:", data);
                if (data.success || data.true) {
                    this.renderCalendar(); // Redraw
                    if (window.showToast) window.showToast("Schedule updated", 'success');
                } else {
                    if (window.showToast) window.showToast("Error updating schedule", 'error');
                    else alert('Error updating schedule');
                }
            })
            .catch(err => {
                console.error("Fetch Error:", err);
                if (window.showToast) window.showToast("System Error during toggle", 'error');
                else alert("System Error during toggle");
            });
    }
};

window.changeMonthPromote = (delta) => {
    window.ScheduleManager.currentDate.setMonth(window.ScheduleManager.currentDate.getMonth() + delta);

    window.ScheduleManager.renderCalendar();
};

// ------------------------------------------
// 9. Designer Dashboard
// ------------------------------------------
function setupDesignerDashboard() {
    console.log("Setting up Designer Dashboard...");
    const user = JSON.parse(localStorage.getItem('dott_current_user') || '{}');
    // Ensure we are allowed here
    // Although server side redirects, this covers direct access or session quirks
    if (!user || (user.role !== 'DESIGNER' && user.role !== 'ADMIN')) {
        // Allow Admin to view for testing? Technically LoginServlet redirects based on role.
        // But if I am Admin and force URL, I might want to see it? 
        // Let's just be strict or allow fallthrough. User check is enough.
    }

    // --- Sidebar Navigation ---
    const navLinks = document.querySelectorAll('.sidebar-nav .nav-item');
    const sections = document.querySelectorAll('.dashboard-section');

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            // Remove active class from all
            navLinks.forEach(n => n.classList.remove('active'));
            sections.forEach(s => s.classList.remove('active'));

            // Add active to clicked
            link.classList.add('active');

            // Show section
            const targetId = link.id.replace('nav-', 'section-');
            const targetSection = document.getElementById(targetId);
            if (targetSection) targetSection.classList.add('active');

            // Specific Init Logic
            if (link.id === 'nav-bookings') {
                populateDesignerBookings();
            } else if (link.id === 'nav-my-schedule') {
                if (window.designerCalendar && window.currentDesignerId) {
                    window.designerCalendar.render();
                }
            }
        });
    });

    // --- Logout ---
    const logoutBtn = document.getElementById('nav-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            db.logout();
        });
    }

    // --- Load Data ---
    fetch('api/designers?type=all&_=' + new Date().getTime())
        .then(res => res.json())
        .then(designers => {
            // Robust Email Logic: Prefer Server Injection, Fallback to LocalStorage
            let myEmail = window.serverCurrentUserEmail;
            if (!myEmail) {
                console.warn("Server email not found, falling back to localStorage");
                if (user && user.email) myEmail = user.email;
            }

            console.log("Looking for designer with email:", myEmail);
            const myself = designers.find(d => d.email === myEmail);

            if (myself) {
                console.log("Found Designer Self:", myself);
                // Ensure we use the correct IDs for Calendar vs Updates
                window.currentDesignerId = myself.designerId; // For Calendar (DESIGNERS table ID)
                window.currentStaffId = myself.id; // For Staff Updates (STAFF table ID)

                populateDesignerProfile(myself);
                setupDesignerCalendar();
                populateDesignerBookings();
            } else {
                console.error("Designer profile not found for email:", myEmail);

                // Fallback attempt
                if (user && user.firstName) {
                    const byName = designers.find(d => d.name && d.name.includes(user.firstName));
                    if (byName) {
                        console.log("Found by name match fallback:", byName);
                        window.currentDesignerId = byName.designerId;
                        window.currentStaffId = byName.id;
                        populateDesignerProfile(byName);
                        setupDesignerCalendar();
                        populateDesignerBookings();
                        return;
                    }
                }

                // Show audible error or UI feedback
                const container = document.getElementById('designer-dashboard');
                if (container) {
                    const errorDiv = document.createElement('div');
                    errorDiv.style.cssText = "background: #ffebee; color: #c62828; padding: 15px; margin: 20px; border-radius: 4px; text-align: center;";
                    errorDiv.innerHTML = `<strong>Error:</strong> Could not load your designer profile. System Email: ${myEmail || 'None'}`;
                    container.insertBefore(errorDiv, container.firstChild);
                }
            }
        })
        .catch(err => console.error("Error loading designers:", err));


    // --- Handle Profile Save ---
    const profileForm = document.getElementById('designer-profile-form');
    if (profileForm && !profileForm.dataset.listenerAttached) {
        profileForm.dataset.listenerAttached = 'true'; // Mark as attached
        profileForm.addEventListener('submit', (e) => {
            e.preventDefault();
            if (!window.currentStaffId) { alert("Error: User ID not found."); return; }

            const formData = new URLSearchParams();
            formData.append('id', window.currentStaffId);
            formData.append('role', 'DESIGNER');
            // We need to keep Email same as it's typically read-only or ID
            formData.append('email', document.getElementById('my-email').value);

            formData.append('firstName', document.getElementById('my-firstname').value);
            formData.append('lastName', document.getElementById('my-lastname').value);
            formData.append('phone', document.getElementById('my-phone').value);

            // Designer Specifics
            formData.append('specialty', document.getElementById('my-specialty').value);
            formData.append('bio', document.getElementById('my-bio').value);

            // Capacity - Preserve existing values from loaded data
            const cur = window.currentUserData || {};
            formData.append('maxHours', cur.maxHours || 40);
            formData.append('minHours', cur.minHours || 0);
            formData.append('maxProjects', cur.maxProjects || 5);
            formData.append('maxBookings', cur.maxBookings || 20);

            // Status ?
            if (window.currentUserData) {
                formData.append('status', window.currentUserData.status || 'Active');
            }

            fetch('admin/update-staff', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success || data.status === 'success') {
                        if (window.showToast) window.showToast('Profile updated successfully!', 'success');
                        else alert('Profile updated successfully!');
                    } else {
                        if (window.showToast) window.showToast('Error: ' + data.message, 'error');
                        else alert('Error: ' + data.message);
                    }
                })
                .catch(err => {
                    console.error(err);
                    if (window.showToast) window.showToast('An error occurred.', 'error');
                    else alert('An error occurred.');
                });
        });
    }
}

function populateDesignerProfile(data) {
    window.currentUserData = data;

    // Naive name split attempt if firstName/lastName not distinct in data object 
    // (DesignersListServlet returns 'name' full string usually, checking...)
    console.log("DEBUG: populateDesignerProfile data: ", data);
    console.log("DEBUG: API Version: ", data.version);
    console.group("Name Debug");
    console.log("fName raw:", data.firstName);
    console.log("lName raw:", data.lastName);
    console.groupEnd();

    let fName = data.firstName;
    let lName = data.lastName;

    if (!fName || !lName) {
        // Fallback to splitting 'name'
        const parts = (data.name || '').split(' ');
        fName = parts[0] || '';
        lName = parts.slice(1).join(' ') || '';
    }

    const setVal = (id, val) => {
        const el = document.getElementById(id);
        if (el) el.value = val || '';
    };

    setVal('my-firstname', fName);
    setVal('my-lastname', lName);
    setVal('my-email', data.email);
    setVal('my-phone', data.phone);
    // setVal('my-specialty', data.specialty);
    if (window.setSpecialtyValues) {
        window.setSpecialtyValues(data.specialty);
    } else {
        setVal('my-specialty', data.specialty);
    }
    setVal('my-bio', data.bio);

    // Capacity - Elements removed from UI, but data kept in window.currentUserData
    // setVal('my-max-hours', data.maxHours);
    // setVal('my-min-hours', data.minHours);
    // setVal('my-max-projects', data.maxProjects);
    // setVal('my-max-bookings', data.maxBookings);
}

function populateDesignerBookings() {
    const tbody = document.getElementById('my-bookings-tbody');
    if (!tbody) return;

    console.log("Populating Designer Bookings...");
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 20px;">Loading bookings...</td></tr>';

    fetch('api/bookings?_=' + new Date().getTime())
        .then(res => res.json())
        .then(bookings => {
            console.log("Bookings fetched:", bookings);
            if (!Array.isArray(bookings)) {
                console.error("Bookings is not an array");
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:red; padding:20px;">Error loading bookings</td></tr>';
                return;
            }

            // Filter for current user (Designer)
            // We use name matching against booking.designer
            // Ideally use ID, but Booking Model uses Designer Name string currently
            const myName = (window.currentUserData && window.currentUserData.name) ? window.currentUserData.name : '';
            console.log("Filtering bookings for Designer:", myName);

            // Filtering
            const myBookings = bookings.filter(b => b.designer === myName);
            console.log("Found bookings:", myBookings.length);

            if (myBookings.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px;">No bookings found assigned to you.</td></tr>';
                return;
            }

            tbody.innerHTML = '';
            myBookings.forEach(b => {
                const row = `
                    <tr style="border-bottom: 1px solid #eee;">
                        <td style="padding: 15px; text-align: center;">#${b.id}</td>
                        <td style="padding: 15px; text-align: center;"><strong>${b.client}</strong></td>
                        <td style="padding: 15px; text-align: center;">${b.phone}<br><small>${b.email}</small></td>
                        <td style="padding: 15px; text-align: center;">${b.date}<br><small style="color:#888">${b.time}</small></td>
                        <td style="padding: 15px; text-align: center; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${b.notes || ''}">${b.notes || '-'}</td>
                        <td style="padding: 15px; text-align: center;">
                            <span style="padding: 4px 10px; border-radius: 12px; font-size: 0.85em; font-weight: 700; ${getStatusStyle(b.status)}">
                                ${b.status}
                            </span>
                        </td>
                        <td style="padding: 15px; text-align: center;">
<div style="display: flex; gap: 8px; justify-content: center;">
                             <button class="btn small edit" onclick="window.toggleBookingStatus(${b.id}, '${b.status}')" title="Toggle Status" style="background: linear-gradient(135deg, #0d47a1 0%, #0a3d91 100%); color: white; border: none; padding: 6px 12px; border-radius: 6px; cursor: pointer;">
                                ✓
                             </button>
                             ${b.status !== 'Cancelled' ? `<button class="btn small cancel" onclick="window.cancelBookingFinal(${b.id})" title="Cancel Booking" style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); color: white; border: none; padding: 6px 14px; border-radius: 6px; cursor: pointer; transition: all 0.2s; font-weight: 600;">Cancel</button>` : ''}
                        </div>
                        </td>
                    </tr>
                 `;
                tbody.innerHTML += row;
            });
        })
        .catch(err => {
            console.error("Error fetching bookings:", err);
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:red; padding:20px;">Error loading bookings</td></tr>';
        });
}

function updateBookingStatusPrompt(id, currentStatus) {
    const newStatus = prompt("Enter new status (Confirmed, Completed, Cancelled):", currentStatus);
    if (newStatus && newStatus !== currentStatus) {
        if (window.toggleBookingStatus) {
            window.toggleBookingStatus(id, newStatus);
        } else {
            // Fallback if toggleBookingStatus is not defined (should be in script.js)
            console.error("toggleBookingStatus function missing");
            if (window.showToast) window.showToast("Error: Functionality missing", 'error');
            else alert("Error: Functionality missing");
        }
    }
}


// --- Dedicated Designer Calendar ---
function setupDesignerCalendar() {
    window.designerCalendar = {
        currentDate: new Date(),
        render: function () {
            if (!window.currentDesignerId) return;

            const year = this.currentDate.getFullYear();
            const month = this.currentDate.getMonth();
            const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

            const titleEl = document.getElementById('calendar-month-year');
            if (titleEl) titleEl.textContent = `${monthNames[month]} ${year}`;

            const dateStr = `${year}-${String(month + 1).padStart(2, '0')}`;

            fetch(`api/schedule?designerId=${window.currentDesignerId}&month=${dateStr}&_=${new Date().getTime()}`)
                .then(res => res.json())
                .then(data => {
                    this.drawGrid(year, month, data);
                })
                .catch(err => console.error(err));
        },

        drawGrid: function (year, month, scheduleData) {
            const container = document.getElementById('calendar-days');
            if (!container) return;
            container.innerHTML = '';

            const firstDay = new Date(year, month, 1).getDay();
            const daysInMonth = new Date(year, month + 1, 0).getDate();

            // Empty slots
            for (let i = 0; i < firstDay; i++) {
                const empty = document.createElement('div');
                empty.className = 'calendar-day empty';
                container.appendChild(empty);
            }

            // Days
            for (let d = 1; d <= daysInMonth; d++) {
                const dayEl = document.createElement('div');
                dayEl.className = 'calendar-day';
                dayEl.textContent = d;

                const dateKey = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;

                let isAvailable = false;
                let startTime = '09:00';
                let endTime = '17:00';

                // Find data for this day
                const dayData = Array.isArray(scheduleData) ? scheduleData.find(s => s.date === dateKey) : null;

                if (dayData) {
                    isAvailable = dayData.available; // boolean
                    if (dayData.start) startTime = dayData.start;
                    if (dayData.end) endTime = dayData.end;
                }

                if (isAvailable) {
                    dayEl.classList.add('available');
                    dayEl.style.backgroundColor = '#c6a87c';
                    dayEl.style.color = 'white';
                    // Optional: Show dot or small time hint?
                } else {
                    dayEl.style.backgroundColor = '#f8f9fa';
                    dayEl.style.color = '#333';
                }

                // Click to Open Modal
                dayEl.addEventListener('click', () =>
                    openScheduleModal(dateKey, isAvailable, startTime, endTime)
                );

                container.appendChild(dayEl);
            }
        }
    };

    // Init Render
    window.designerCalendar.render();

    // Wire up buttons
    document.getElementById('prev-month-btn').onclick = () => {
        window.designerCalendar.currentDate.setMonth(window.designerCalendar.currentDate.getMonth() - 1);
        window.designerCalendar.render();
    };
    document.getElementById('next-month-btn').onclick = () => {
        window.designerCalendar.currentDate.setMonth(window.designerCalendar.currentDate.getMonth() + 1);
        window.designerCalendar.render();
    };

    // Initial Render
    window.designerCalendar.render();
}

// --- Designer Profile Edit Logic ---
function toggleProfileEdit() {
    const form = document.getElementById('designer-profile-form');
    if (!form) return;

    // Enable inputs
    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
        if (input.id !== 'my-email') { // Email stays readonly
            input.disabled = false;
            input.style.backgroundColor = '#fff';
            input.style.border = '1px solid #c6a87c';
        }
    });

    // Toggle Buttons
    document.getElementById('btn-edit-profile').style.display = 'none';
    document.getElementById('profile-actions').style.display = 'flex';
}

function cancelProfileEdit() {
    // Reload original data to revert changes
    if (window.currentUserData) {
        populateDesignerProfile(window.currentUserData);
    }

    const form = document.getElementById('designer-profile-form');
    if (!form) return;

    // Disable inputs
    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
        input.disabled = true;
        input.style.backgroundColor = '#f9f9f9';
        input.style.border = '1px solid #e0e0e0';
    });

    // Toggle Buttons
    document.getElementById('btn-edit-profile').style.display = 'block';
    document.getElementById('profile-actions').style.display = 'none';
}

// --- Schedule Day Modal Logic ---
window.selectedScheduleDate = null;
window.selectedScheduleStatus = false;

function openScheduleModal(dateStr, isAvailable, startTime, endTime) {
    window.selectedScheduleDate = dateStr;
    window.selectedScheduleStatus = isAvailable;

    const modal = document.getElementById('schedule-day-modal');
    const title = document.getElementById('modal-date-title');
    const timeSettings = document.getElementById('time-settings');
    const btnAvail = document.getElementById('btn-status-available');
    const btnUnavail = document.getElementById('btn-status-unavailable');

    // Reset Inputs
    document.getElementById('day-start-time').value = startTime || '09:00';
    document.getElementById('day-end-time').value = endTime || '17:00';

    // Set UI
    modal.style.display = 'flex';

    // Parse Date for Title
    const dateObj = new Date(dateStr);
    title.textContent = dateObj.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });

    setDayStatus(isAvailable);
}

function closeScheduleModal() {
    document.getElementById('schedule-day-modal').style.display = 'none';
}

function updateDayAvailability(available) {
    // alert("DEBUG: Clicked " + available); // Verify click
    const btnAvail = document.getElementById('btn-status-available');
    const btnUnavail = document.getElementById('btn-status-unavailable');
    const timeSettings = document.getElementById('time-settings');

    if (available) {
        btnAvail.classList.add('selected-avail');
        btnUnavail.classList.remove('selected-avail');

        btnAvail.style.background = '#c6a87c';
        btnAvail.style.color = '#fff';
        btnAvail.style.borderColor = '#c6a87c';

        btnUnavail.style.background = '#fff';
        btnUnavail.style.color = '#333';
        btnUnavail.style.borderColor = '#ddd';

        timeSettings.style.display = 'block';
    } else {
        btnAvail.classList.remove('selected-avail');
        btnUnavail.classList.add('selected-avail');

        btnAvail.style.background = '#fff';
        btnAvail.style.color = '#333';
        btnAvail.style.borderColor = '#ddd';

        btnUnavail.style.background = '#f44336'; // Red for unavailable
        btnUnavail.style.color = '#fff';
        btnUnavail.style.borderColor = '#f44336';

        timeSettings.style.display = 'none';
    }
}

function saveDaySchedule() {
    console.log(">>> saveDaySchedule CALLED at", new Date());
    // CRITICAL: Check ALL date sources (multiple implementations exist)
    let targetDate = window.selectedScheduleDate;
    if (!targetDate && typeof selectedDate !== 'undefined') targetDate = selectedDate;
    if (!targetDate && typeof currentModalDate !== 'undefined') targetDate = currentModalDate;

    if (!window.currentDesignerId || !targetDate) {
        console.error("=== saveDaySchedule ABORT ===");
        console.error("DesignerID:", window.currentDesignerId);
        console.error("TargetDate:", targetDate);
        console.error("  window.selectedScheduleDate:", window.selectedScheduleDate);
        console.error("  selectedDate (local):", typeof selectedDate !== 'undefined' ? selectedDate : 'UNDEFINED');
        console.error("  currentModalDate (local):", typeof currentModalDate !== 'undefined' ? currentModalDate : 'UNDEFINED');
        if (window.showToast) window.showToast("Missing date reference", 'error');
        return;
    }

    // const available = window.selectedScheduleStatus; // Unreliable
    const btnAvail = document.getElementById('btn-status-available');
    const available = btnAvail.classList.contains('selected-avail');

    const startTime = document.getElementById('day-start-time').value;
    const endTime = document.getElementById('day-end-time').value;

    // DEBUG: Show user what is being sent
    // alert("DEBUG: Sending Available = " + available);

    const params = new URLSearchParams();
    params.append('designerId', window.currentDesignerId);
    params.append('date', targetDate);
    params.append('available', available);
    params.append('startTime', startTime);
    params.append('endTime', endTime);

    console.log(">>> Sending to api/schedule:");
    console.log("  designerId:", window.currentDesignerId);
    console.log("  date:", targetDate);
    console.log("  available:", available);
    console.log("  startTime:", startTime);
    console.log("  endTime:", endTime);

    fetch('api/schedule', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
        .then(res => res.json())
        .then(data => {
            if (data.true || data.success) { // Handle {"success":true}
                console.log(">>> Save successful, updating calendar for", targetDate, "available:", available);

                // Update local calendar data to reflect the save
                calendarAvailability[targetDate] = {
                    available: available === 'true' || available === true,
                    startTime: startTime,
                    endTime: endTime
                };

                closeScheduleModal();

                // Refresh the calendar display
                if (typeof renderCalendar === 'function') {
                    renderCalendar();
                }

                // Update Selected Dates panel
                if (typeof updateSelectedDatesPanel === 'function') {
                    updateSelectedDatesPanel();
                }

                if (window.showToast) window.showToast("Schedule Saved", 'success');

                // NUCLEAR OPTION: Force reload to prove data persisted
                setTimeout(() => {
                    window.location.reload();
                }, 500);

            } else {
                if (window.showToast) window.showToast("Error saving schedule: " + (data.error || "Unknown"), 'error');
                else alert("Error saving schedule");
            }
        })
        .catch(err => {
            console.error("Error saving schedule:", err);
            if (window.showToast) window.showToast("System Error saving schedule", 'error');
        });

}

// --- Schedule Settings (Capacity & Global Status) ---
function openScheduleSettings() {
    const data = window.currentUserData;
    if (!data) return;

    // Populate Settings Form
    if (document.getElementById('setting-max-hours')) document.getElementById('setting-max-hours').value = data.maxHours || 40;
    if (document.getElementById('setting-min-hours')) document.getElementById('setting-min-hours').value = data.minHours || 0;
    if (document.getElementById('setting-max-projects')) document.getElementById('setting-max-projects').value = data.maxProjects || 5;
    if (document.getElementById('setting-max-bookings')) document.getElementById('setting-max-bookings').value = data.maxBookings || 20;

    // Populate Status
    const statusSelect = document.getElementById('setting-status');
    if (statusSelect && data.status) {
        statusSelect.value = data.status;
    }

    // Populate Default Working Hours (load from localStorage or use defaults)
    const savedSettings = JSON.parse(localStorage.getItem('designer_schedule_settings') || '{}');
    if (document.getElementById('setting-start-time')) {
        document.getElementById('setting-start-time').value = savedSettings.startTime || '09:00';
    }
    if (document.getElementById('setting-end-time')) {
        document.getElementById('setting-end-time').value = savedSettings.endTime || '17:00';
    }

    // Populate Slot Availability
    if (document.getElementById('setting-slot-availability')) {
        document.getElementById('setting-slot-availability').value = savedSettings.slotAvailability || '';
    }

    document.getElementById('schedule-settings-modal').style.display = 'flex';
}

function closeScheduleSettings() {
    document.getElementById('schedule-settings-modal').style.display = 'none';
}

function saveScheduleSettings() {
    if (!window.currentStaffId || !window.currentUserData) return;

    // Construct full payload (merging existing profile data with new settings)
    const cur = window.currentUserData;
    const formData = new URLSearchParams();

    // ID & Role
    formData.append('id', window.currentStaffId);
    formData.append('role', 'DESIGNER');

    // Immutable / Existing Profile Data (CRITICAL: Oracle treats '' as NULL)
    // Only use fallbacks if values are genuinely missing, not if they're empty strings from form
    formData.append('email', cur.email && cur.email.trim() ? cur.email : 'noemail@example.com');
    formData.append('firstName', cur.firstName && cur.firstName.trim() ? cur.firstName : 'Unknown');
    formData.append('lastName', cur.lastName && cur.lastName.trim() ? cur.lastName : 'User');
    formData.append('phone', cur.phone && cur.phone.trim() ? cur.phone : '0000000000');
    formData.append('specialty', cur.specialty || '');
    formData.append('bio', cur.bio || '');

    // New Settings from Inputs
    formData.append('maxHours', document.getElementById('setting-max-hours').value || 40);
    formData.append('minHours', document.getElementById('setting-min-hours').value || 0);
    formData.append('maxProjects', document.getElementById('setting-max-projects').value || 5);
    formData.append('maxBookings', document.getElementById('setting-max-bookings').value || 20);

    // Status
    const statusSelect = document.getElementById('setting-status');
    formData.append('status', statusSelect ? statusSelect.value : (cur.status || 'Active'));

    // Save Default Working Hours and Slot Availability to localStorage
    const startTime = document.getElementById('setting-start-time')?.value || '09:00';
    const endTime = document.getElementById('setting-end-time')?.value || '17:00';
    const slotAvailability = document.getElementById('setting-slot-availability')?.value || '';
    localStorage.setItem('designer_schedule_settings', JSON.stringify({
        startTime: startTime,
        endTime: endTime,
        slotAvailability: slotAvailability
    }));

    // Update the Default Hours display card
    updateDefaultHoursDisplay(startTime, endTime);

    // Send Update
    fetch('admin/update-staff', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
    })
        .then(res => res.json())
        .then(data => {
            if (data.success || data.status === 'success') {
                if (window.showToast) {
                    window.showToast('Settings updated successfully!', 'success');
                } else {
                    alert('Settings updated successfully!');
                }
                closeScheduleSettings();

                // Update local data to reflect changes immediately
                window.currentUserData.maxHours = formData.get('maxHours');
                window.currentUserData.minHours = formData.get('minHours');
                window.currentUserData.maxProjects = formData.get('maxProjects');
                window.currentUserData.maxBookings = formData.get('maxBookings');
                window.currentUserData.status = formData.get('status');

                // Update info cards
                updateScheduleInfoCards();
            } else {
                if (window.showToast) {
                    window.showToast('Error: ' + data.message, 'error');
                } else {
                    alert('Error: ' + data.message);
                }
            }
        })
        .catch(err => {
            console.error(err);
            if (window.showToast) {
                window.showToast('An error occurred while saving settings.', 'error');
            } else {
                alert('An error occurred while saving settings.');
            }
        });
}

// Helper function to update default hours display
function updateDefaultHoursDisplay(startTime, endTime) {
    const hoursDisplay = document.getElementById('schedule-hours-display');
    if (hoursDisplay) {
        // Convert 24h to 12h format
        const formatTime = (time) => {
            const [hours, minutes] = time.split(':');
            const h = parseInt(hours);
            const ampm = h >= 12 ? 'PM' : 'AM';
            const h12 = h % 12 || 12;
            return `${h12}:${minutes} ${ampm}`;
        };
        hoursDisplay.textContent = `${formatTime(startTime)} - ${formatTime(endTime)}`;
    }
}

// ==========================================
// DESIGNER CALENDAR FUNCTIONALITY
// ==========================================

// Global calendar state
let calendarCurrentMonth = new Date().getMonth();
let calendarCurrentYear = new Date().getFullYear();
let calendarAvailability = {}; // Store availability per day
let selectedDayElement = null;
let selectedDate = null;

// Initialize calendar when My Schedule is shown
function initDesignerCalendar() {
    const calendarDays = document.getElementById('calendar-days');
    if (!calendarDays) return;

    // Setup navigation buttons
    const prevBtn = document.getElementById('prev-month-btn');
    const nextBtn = document.getElementById('next-month-btn');

    if (prevBtn) {
        prevBtn.onclick = function () {
            calendarCurrentMonth--;
            if (calendarCurrentMonth < 0) {
                calendarCurrentMonth = 11;
                calendarCurrentYear--;
            }
            loadCalendarAvailability(); // Reload for new month
        };
    }

    if (nextBtn) {
        nextBtn.onclick = function () {
            calendarCurrentMonth++;
            if (calendarCurrentMonth > 11) {
                calendarCurrentMonth = 0;
                calendarCurrentYear++;
            }
            loadCalendarAvailability(); // Reload for new month
        };
    }

    // Load saved availability from DATABASE via API
    loadCalendarAvailability();

    // Render the calendar
    renderCalendar();
}

// Load availability from DATABASE (not localStorage)
function loadCalendarAvailability() {
    if (!window.currentDesignerId) {
        console.warn("Cannot load calendar: designer ID not set");
        return;
    }

    const year = calendarCurrentYear;
    const month = String(calendarCurrentMonth + 1).padStart(2, '0');
    const monthParam = `${year}-${month}`;

    fetch(`api/schedule?designerId=${window.currentDesignerId}&month=${monthParam}`)
        .then(res => res.json())
        .then(schedules => {
            calendarAvailability = {};
            schedules.forEach(item => {
                // item.date is like "2026-01-23"
                calendarAvailability[item.date] = {
                    available: item.available,
                    startTime: item.start,
                    endTime: item.end
                };
            });
            renderCalendar();
            // Update Selected Dates panel with loaded data
            if (typeof updateSelectedDatesPanel === 'function') {
                updateSelectedDatesPanel();
            }
        })
        .catch(err => {
            console.error("Error loading schedule:", err);
        });
}

// Save availability to localStorage
function saveCalendarAvailability() {
    localStorage.setItem('designer_availability', JSON.stringify(calendarAvailability));
}

// Render the calendar
function renderCalendar() {
    const calendarDays = document.getElementById('calendar-days');
    const monthYearLabel = document.getElementById('calendar-month-year');

    if (!calendarDays) return;

    const months = ['January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'];

    // Update header
    if (monthYearLabel) {
        monthYearLabel.textContent = months[calendarCurrentMonth] + ' ' + calendarCurrentYear;
    }

    console.log(">>> RENDER CALENDAR DEBUG <<<");
    console.log("Current Availability Object:", JSON.stringify(calendarAvailability, null, 2));

    // Clear existing days
    calendarDays.innerHTML = '';

    // Add day headers
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    dayNames.forEach(day => {
        const dayHeader = document.createElement('div');
        dayHeader.style.cssText = 'font-weight: 600; color: #666; padding: 10px 0; text-align: center; font-size: 0.85em;';
        dayHeader.textContent = day;
        calendarDays.appendChild(dayHeader);
    });

    // Get first day of month and number of days
    const firstDay = new Date(calendarCurrentYear, calendarCurrentMonth, 1).getDay();
    const daysInMonth = new Date(calendarCurrentYear, calendarCurrentMonth + 1, 0).getDate();
    const today = new Date();

    // Add empty cells for days before first day of month
    for (let i = 0; i < firstDay; i++) {
        const emptyCell = document.createElement('div');
        emptyCell.style.cssText = 'aspect-ratio: 1;';
        calendarDays.appendChild(emptyCell);
    }

    // Add day cells
    for (let day = 1; day <= daysInMonth; day++) {
        const dateKey = `${calendarCurrentYear}-${String(calendarCurrentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        const dayCell = document.createElement('div');

        const isToday = today.getDate() === day &&
            today.getMonth() === calendarCurrentMonth &&
            today.getFullYear() === calendarCurrentYear;

        const isAvailable = calendarAvailability[dateKey]?.available;
        const isPastDate = new Date(calendarCurrentYear, calendarCurrentMonth, day) < new Date(today.getFullYear(), today.getMonth(), today.getDate());

        let bgColor = '#f8f9fa'; // Default gray
        let textColor = '#333';
        let border = 'none';

        if (isPastDate) {
            bgColor = '#e9ecef';
            textColor = '#aaa';
        } else if (isAvailable === true) {
            bgColor = '#c6a87c'; // Gold for available
            textColor = 'white';
        }
        // Unavailable dates remain default grey (#f8f9fa)

        if (isToday) {
            border = '2px solid #333';
        }

        dayCell.style.cssText = `
            aspect-ratio: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            background: ${bgColor};
            border-radius: 8px;
            cursor: ${isPastDate ? 'not-allowed' : 'pointer'};
            font-weight: ${isToday ? '700' : '500'};
            color: ${textColor};
            transition: all 0.2s;
            border: ${border};
            font-size: 0.9em;
        `;

        dayCell.textContent = day;
        dayCell.dataset.date = dateKey;
        dayCell.dataset.day = day;

        if (!isPastDate) {
            dayCell.onmouseenter = function () {
                if (!this.classList.contains('selected')) {
                    this.style.transform = 'scale(1.05)';
                    this.style.boxShadow = '0 3px 10px rgba(0,0,0,0.15)';
                }
            };
            dayCell.onmouseleave = function () {
                this.style.transform = 'scale(1)';
                this.style.boxShadow = 'none';
            };
            dayCell.onclick = function () {
                openDayModal(this, dateKey, day);
            };
        }

        calendarDays.appendChild(dayCell);
    }
}

// Open day detail modal
function openDayModal(element, dateKey, day) {
    selectedDayElement = element;
    selectedDate = dateKey;

    // Set globals for the API-based saveDaySchedule function
    window.selectedScheduleDate = dateKey;
    console.log("DEBUG: openDayModal set window.selectedScheduleDate =", window.selectedScheduleDate);
    // window.currentDesignerId should already be set by setupDesignerDashboard

    const modal = document.getElementById('schedule-day-modal');
    const titleEl = document.getElementById('modal-date-title');

    if (!modal) return;

    // Format the date for display
    const months = ['January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'];
    if (titleEl) {
        titleEl.textContent = `${months[calendarCurrentMonth]} ${day}, ${calendarCurrentYear}`;
    }

    // Load existing availability for this day
    const dayData = calendarAvailability[dateKey] || { available: false, startTime: '09:00', endTime: '17:00' };

    // Set time inputs
    const startInput = document.getElementById('day-start-time');
    const endInput = document.getElementById('day-end-time');
    if (startInput) startInput.value = dayData.startTime || '09:00';
    if (endInput) endInput.value = dayData.endTime || '17:00';

    // Update status buttons - CRITICAL: This sets window.selectedScheduleStatus
    setDayStatus(dayData.available || false);

    // Show modal
    modal.style.display = 'flex';
}

// Close day modal
function closeScheduleModal() {
    const modal = document.getElementById('schedule-day-modal');
    if (modal) {
        modal.style.display = 'none';
    }
    selectedDayElement = null;
    selectedDate = null;
}

// Update status button styles
function updateStatusButtons(isAvailable) {
    const availBtn = document.getElementById('btn-status-available');
    const unavailBtn = document.getElementById('btn-status-unavailable');

    if (availBtn && unavailBtn) {
        if (isAvailable) {
            availBtn.style.background = '#c6a87c';
            availBtn.style.color = 'white';
            availBtn.style.borderColor = '#c6a87c';
            unavailBtn.style.background = 'white';
            unavailBtn.style.color = '#333';
            unavailBtn.style.borderColor = '#ddd';
        } else {
            unavailBtn.style.background = '#dc3545';
            unavailBtn.style.color = 'white';
            unavailBtn.style.borderColor = '#dc3545';
            availBtn.style.background = 'white';
            availBtn.style.color = '#333';
            availBtn.style.borderColor = '#ddd';
        }
    }
}

// Set day status (available/unavailable)
function setDayStatus(isAvailable) {
    if (!selectedDate) return;

    const startTime = document.getElementById('day-start-time')?.value || '09:00';
    const endTime = document.getElementById('day-end-time')?.value || '17:00';

    calendarAvailability[selectedDate] = {
        available: isAvailable,
        startTime: startTime,
        endTime: endTime
    };

    updateStatusButtons(isAvailable);
}

// Save day schedule
// Legacy saveDaySchedule removed to avoid conflict with API-based implementation.

// Update the schedule info cards with current data
function updateScheduleInfoCards() {
    // Update status display
    const statusDisplay = document.getElementById('schedule-status-display');
    if (statusDisplay && window.currentUserData) {
        const status = window.currentUserData.status || 'Active';
        statusDisplay.textContent = status;

        // Update color based on status
        if (status === 'Active') {
            statusDisplay.style.color = '#28a745';
        } else if (status === 'Inactive') {
            statusDisplay.style.color = '#dc3545';
        } else {
            statusDisplay.style.color = '#ffc107';
        }
    }

    // Update capacity displays
    if (window.currentUserData) {
        const minHoursDisplay = document.getElementById('schedule-min-hours-display');
        const maxHoursDisplay = document.getElementById('schedule-max-hours-display');
        const maxProjectsDisplay = document.getElementById('schedule-max-projects-display');
        const maxBookingsDisplay = document.getElementById('schedule-max-bookings-display');

        if (minHoursDisplay) minHoursDisplay.textContent = window.currentUserData.minHours || '0';
        if (maxHoursDisplay) maxHoursDisplay.textContent = window.currentUserData.maxHours || '40';
        if (maxProjectsDisplay) maxProjectsDisplay.textContent = window.currentUserData.maxProjects || '5';
        if (maxBookingsDisplay) maxBookingsDisplay.textContent = window.currentUserData.maxBookings || '20';
    }

    // Update default working hours display from localStorage
    const savedSettings = JSON.parse(localStorage.getItem('designer_schedule_settings') || '{}');
    const startTime = savedSettings.startTime || '09:00';
    const endTime = savedSettings.endTime || '17:00';
    updateDefaultHoursDisplay(startTime, endTime);
}

// Initialize calendar when document is ready
document.addEventListener('DOMContentLoaded', function () {
    // Check if we're on designer dashboard
    if (document.getElementById('calendar-days')) {
        initDesignerCalendar();
        updateScheduleInfoCards();
    }
});

// Also initialize when My Schedule section is shown (for SPA navigation)
const navSchedule = document.getElementById('nav-my-schedule');
if (navSchedule) {
    navSchedule.addEventListener('click', function () {
        setTimeout(function () {
            initDesignerCalendar();
            updateScheduleInfoCards();
        }, 100);
    });
}

// ==========================================
// DYNAMIC PROGRESS BAR LOGIC (Added for Dual Flows)
// ==========================================
function renderProgressBar() {
    const progressContainer = document.querySelector('.booking-progress');
    if (!progressContainer) return;

    const temp = db.getTempBooking();
    const path = window.location.pathname;
    const page = path.substring(path.lastIndexOf('/') + 1);

    // Detect Flow: If category exists, it's Flow A (Style First). Otherwise Flow B (Date First).
    // Exception: If we are effectively starting Flow B (e.g. on booking-date.jsp and no category), ensure we treat it as Flow B.
    // Also, if we are on booking-category.jsp, it's definitely Flow A.
    const isFlowA = temp.category || page === 'booking-category.jsp';

    let steps = [];
    let currentStepIndex = -1;

    if (isFlowA) {
        // Flow A: Style -> Designer -> Date -> Time -> Summary
        steps = [
            { label: 'Style', page: 'booking-category.jsp' },
            { label: 'Designer', page: 'booking-designer.jsp' },
            { label: 'Date', page: 'booking-date.jsp' },
            { label: 'Time', page: 'booking-time.jsp' },
            { label: 'Summary', page: 'booking-summary.jsp' }
        ];
    } else {
        // Flow B: Date -> Time -> Designer -> Summary
        steps = [
            { label: 'Date', page: 'booking-date.jsp' },
            { label: 'Time', page: 'booking-time.jsp' },
            { label: 'Designer', page: 'booking-designer.jsp' },
            { label: 'Summary', page: 'booking-summary.jsp' }
        ];
    }

    // Find current step index
    currentStepIndex = steps.findIndex(s => s.page === page);

    // Generate HTML
    let html = '<div class="progress-track"></div>';

    steps.forEach((step, index) => {
        let statusClass = '';
        if (index < currentStepIndex) statusClass = 'completed';
        else if (index === currentStepIndex) statusClass = 'active';

        html += `
            <div class="progress-step ${statusClass}">
                <div class="step-circle">${index + 1}</div>
                <div class="step-label">${step.label}</div>
            </div>
        `;
    });

    progressContainer.innerHTML = html;
}

// Initialize Progress Bar on Load
document.addEventListener('DOMContentLoaded', () => {
    renderProgressBar();

    // Clear temp booking logic for Booking Method page
    if (document.getElementById('booking-method-page')) {
        const dateBtn = document.getElementById('method-date-btn');
        const catBtn = document.getElementById('method-category-btn');

        if (dateBtn) dateBtn.addEventListener('click', () => {
            db._set(db.STORAGE_KEYS.TEMP_BOOKING, {}); // Clear for Flow B
        });

        if (catBtn) catBtn.addEventListener('click', () => {
            db._set(db.STORAGE_KEYS.TEMP_BOOKING, {}); // Clear for Flow A
        });
    }
});

// ==========================================
// MULTI-SELECT SPECIALTY DROPDOWN (NO CHECKBOXES)
// ==========================================

function initSpecialtyMultiSelect() {
    const container = document.getElementById('my-specialty-multiselect');
    if (!container) return;

    const selectBox = container.querySelector('.select-box');
    const optionsPanel = document.getElementById('my-specialty-options');
    const selectedText = selectBox.querySelector('.selected-text');
    const hiddenInput = document.getElementById('my-specialty');

    selectBox.addEventListener('click', (e) => {
        if (selectBox.style.cursor === 'not-allowed') return;
        e.stopPropagation();
        optionsPanel.style.display = optionsPanel.style.display === 'block' ? 'none' : 'block';
    });

    const options = optionsPanel.querySelectorAll('.multiselect-option');
    options.forEach(option => {
        option.dataset.selected = 'false';
        option.addEventListener('click', (e) => {
            e.stopPropagation();
            const isSelected = option.dataset.selected === 'true';
            option.dataset.selected = (!isSelected).toString();
            option.style.backgroundColor = isSelected ? '' : '#c6a87c';
            option.style.color = isSelected ? '' : '#fff';
            updateDisplay();
        });
    });

    document.addEventListener('click', (e) => {
        if (!container.contains(e.target)) optionsPanel.style.display = 'none';
    });

    function updateDisplay() {
        const selected = Array.from(options).filter(o => o.dataset.selected === 'true');
        if (selected.length === 0) {
            selectedText.textContent = 'No specialty selected';
            selectedText.style.color = '#999';
            hiddenInput.value = '';
        } else {
            const values = selected.map(o => o.getAttribute('data-value'));
            selectedText.textContent = values.join(', ');
            selectedText.style.color = '#333';
            hiddenInput.value = values.join(',');
        }
    }

    window.setSpecialtyValues = function (specialtyString) {
        if (!specialtyString) {
            options.forEach(o => {
                o.dataset.selected = 'false';
                o.style.backgroundColor = '';
                o.style.color = '';
            });
            updateDisplay();
            return;
        }
        const values = specialtyString.split(',').map(s => s.trim());
        options.forEach(o => {
            const value = o.getAttribute('data-value');
            if (values.includes(value)) {
                o.dataset.selected = 'true';
                o.style.backgroundColor = '#c6a87c';
                o.style.color = '#fff';
            } else {
                o.dataset.selected = 'false';
                o.style.backgroundColor = '';
                o.style.color = '';
            }
        });
        updateDisplay();
    };

    updateDisplay();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initSpecialtyMultiSelect);
} else {
    initSpecialtyMultiSelect();
}


// ==========================================
// PROFILE EDITING LOGIC
// ==========================================

function toggleProfileEdit() {
    const form = document.getElementById('designer-profile-form');
    if (!form) return;

    // Enable inputs
    const inputs = form.querySelectorAll('.form-input');
    inputs.forEach(input => {
        if (input.id !== 'my-email') { // Keep email disabled
            input.disabled = false;
            input.style.background = '#fff';
            input.style.cursor = 'text';
        }
    });

    // Enable Multi-Select
    const multiSelectBox = document.querySelector('#my-specialty-multiselect .select-box');
    if (multiSelectBox) {
        multiSelectBox.style.cursor = 'pointer';
        multiSelectBox.style.background = '#fff';
        // Remove not-allowed check in click handler is handled by style check
    }

    // Toggle Buttons
    const editBtn = document.getElementById('btn-edit-profile');
    const actions = document.getElementById('profile-actions');

    if (editBtn) editBtn.style.display = 'none';
    if (actions) actions.style.display = 'flex';
}

function cancelProfileEdit() {
    const form = document.getElementById('designer-profile-form');
    if (!form) return;

    // Disable inputs
    const inputs = form.querySelectorAll('.form-input');
    inputs.forEach(input => {
        input.disabled = true;
        input.style.background = '#f9f9f9';
        input.style.cursor = 'not-allowed';
    });

    // Disable Multi-Select
    const multiSelectBox = document.querySelector('#my-specialty-multiselect .select-box');
    if (multiSelectBox) {
        multiSelectBox.style.cursor = 'not-allowed';
        multiSelectBox.style.background = '#f9f9f9';
        // Close dropdown if open
        const optionsPanel = document.getElementById('my-specialty-options');
        if (optionsPanel) optionsPanel.style.display = 'none';
    }

    // Toggle Buttons
    const editBtn = document.getElementById('btn-edit-profile');
    const actions = document.getElementById('profile-actions');

    if (editBtn) editBtn.style.display = 'inline-block';
    if (actions) actions.style.display = 'none';

    // Reset values (Reload profile data)
    if (window.currentUserData) {
        populateDesignerProfile(window.currentUserData);
    }
}

// ==========================================
// SCHEDULE MODAL LOGIC
// ==========================================

let currentModalDate = null;

function openScheduleModal(dateKey, isAvailable, startTime, endTime) {
    currentModalDate = dateKey;
    window.selectedScheduleDate = dateKey; // CRITICAL: Also set for saveDaySchedule
    const modal = document.getElementById('schedule-day-modal');
    if (!modal) return;

    // Set Title
    const dateObj = new Date(dateKey + 'T00:00:00');
    // Use local time parsing to avoid timezone shifts if possible, or append T00:00:00
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    document.getElementById('modal-date-title').textContent = dateObj.toLocaleDateString('en-US', options);

    // Set initial status UI
    updateDayAvailability(isAvailable);

    // Set Times
    document.getElementById('day-start-time').value = startTime || '09:00';
    document.getElementById('day-end-time').value = endTime || '17:00';

    modal.style.display = 'flex';
}

function closeScheduleModal() {
    const modal = document.getElementById('schedule-day-modal');
    if (modal) modal.style.display = 'none';
    currentModalDate = null;
}

function setDayStatus(isAvailable) {
    const btnAvail = document.getElementById('btn-status-available');
    const btnUnavail = document.getElementById('btn-status-unavailable');
    const timeSettings = document.getElementById('time-settings');

    if (isAvailable) {
        btnAvail.style.backgroundColor = '#d4edda';
        btnAvail.style.borderColor = '#c3e6cb';
        btnAvail.style.color = '#155724';

        btnUnavail.style.backgroundColor = 'white';
        btnUnavail.style.borderColor = '#ddd';
        btnUnavail.style.color = '#555';

        if (timeSettings) timeSettings.style.display = 'block';
    } else {
        btnAvail.style.backgroundColor = 'white';
        btnAvail.style.borderColor = '#ddd';
        btnAvail.style.color = '#555';

        btnUnavail.style.backgroundColor = '#f8d7da';
        btnUnavail.style.borderColor = '#f5c6cb';
        btnUnavail.style.color = '#721c24';

        if (timeSettings) timeSettings.style.display = 'none';
    }

    // Store current status state in the modal DOM for save function to read
    const modal = document.getElementById('schedule-day-modal');
    modal.dataset.status = isAvailable ? 'true' : 'false';
}

// Duplicate saveDaySchedule code removed. Use the implementation earlier in the file.
