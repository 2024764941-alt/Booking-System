// DATA MODULE
// Centralized data management and state

class DataManager {
    constructor() {
        // User data
        this.users = [
            { 
                firstName: 'John', 
                lastName: 'Doe', 
                email: 'john@example.com', 
                phone: '1234567890', 
                address: '123 Main St', 
                password: 'password' 
            }
        ];

        // Designer data
        this.designers = [
            { 
                id: 1, 
                name: 'Alice Johnson', 
                specialty: 'Modern Minimalist', 
                availability: ['09:00', '10:00', '14:00'] 
            },
            { 
                id: 2, 
                name: 'Bob Smith', 
                specialty: 'Traditional Elegance', 
                availability: ['11:00', '15:00', '16:00'] 
            },
            { 
                id: 3, 
                name: 'Carol Lee', 
                specialty: 'Contemporary Fusion', 
                availability: ['08:00', '12:00', '17:00'] 
            }
        ];

        // Booking data
        this.bookings = [
            { 
                id: 1, 
                client: 'John Doe', 
                email: 'john@example.com', 
                phone: '1234567890', 
                designer: 'Alice Johnson', 
                date: '2026-01-02', 
                time: '09:00', 
                notes: 'Kitchen design', 
                status: 'Confirmed' 
            },
            { 
                id: 2, 
                client: 'Jane Smith', 
                email: 'jane@example.com', 
                phone: '0987654321', 
                designer: 'Bob Smith', 
                date: '2026-01-03', 
                time: '11:00', 
                notes: 'Living room', 
                status: 'Pending' 
            }
        ];

        // Current session
        this.currentUser = null;
        this.currentBooking = {};
    }

    // ===== USER METHODS =====
    registerUser(firstName, lastName, email, phone, address, password) {
        if (this.users.find(u => u.email === email)) {
            return { success: false, message: 'Email already registered' };
        }
        
        const newUser = { firstName, lastName, email, phone, address, password };
        this.users.push(newUser);
        return { success: true, user: newUser };
    }

    loginUser(email, password) {
        const user = this.users.find(u => u.email === email && u.password === password);
        if (user) {
            this.currentUser = user;
            return { success: true, user };
        }
        return { success: false, message: 'Invalid credentials' };
    }

    logoutUser() {
        this.currentUser = null;
        this.currentBooking = {};
    }

    getCurrentUser() {
        return this.currentUser;
    }

    // ===== DESIGNER METHODS =====
    getAllDesigners() {
        return this.designers;
    }

    addDesigner(name, specialty, availability) {
        const newDesigner = {
            id: this.designers.length + 1,
            name,
            specialty,
            availability
        };
        this.designers.push(newDesigner);
        return newDesigner;
    }

    updateDesigner(id, name, specialty, availability) {
        const designer = this.designers.find(d => d.id === id);
        if (designer) {
            designer.name = name;
            designer.specialty = specialty;
            designer.availability = availability;
            return { success: true, designer };
        }
        return { success: false, message: 'Designer not found' };
    }

    deleteDesigner(id) {
        const index = this.designers.findIndex(d => d.id === id);
        if (index > -1) {
            this.designers.splice(index, 1);
            return { success: true };
        }
        return { success: false, message: 'Designer not found' };
    }

    // ===== BOOKING METHODS =====
    getAllBookings() {
        return this.bookings;
    }

    createBooking(designerName, date, time, phone, notes) {
        if (!this.currentUser) {
            return { success: false, message: 'User not logged in' };
        }

        const bookingId = this.getNextBookingId();
        const newBooking = {
            id: bookingId,
            client: `${this.currentUser.firstName} ${this.currentUser.lastName}`,
            email: this.currentUser.email,
            phone,
            designer: designerName,
            date,
            time,
            notes,
            status: 'Confirmed'
        };

        this.bookings.push(newBooking);
        this.currentBooking = newBooking;
        return { success: true, booking: newBooking };
    }

    updateBookingStatus(bookingId, status) {
        const booking = this.bookings.find(b => b.id === bookingId);
        if (booking) {
            booking.status = status;
            return { success: true, booking };
        }
        return { success: false, message: 'Booking not found' };
    }

    deleteBooking(bookingId) {
        const index = this.bookings.findIndex(b => b.id === bookingId);
        if (index > -1) {
            this.bookings.splice(index, 1);
            return { success: true };
        }
        return { success: false, message: 'Booking not found' };
    }

    getNextBookingId() {
        if (this.bookings.length === 0) return 1;
        return Math.max(...this.bookings.map(b => b.id)) + 1;
    }

    getLastBooking() {
        return this.bookings[this.bookings.length - 1];
    }

    // ===== BOOKING FLOW METHODS =====
    setCurrentBookingDate(date) {
        this.currentBooking.date = date;
    }

    setCurrentBookingDesigner(designerName) {
        this.currentBooking.designer = designerName;
    }

    setCurrentBookingTime(time) {
        this.currentBooking.time = time;
    }

    getCurrentBooking() {
        return this.currentBooking;
    }

    resetBooking() {
        this.currentBooking = {};
    }

    // ===== STATS METHODS =====
    getBookingStats() {
        const today = new Date().toISOString().split('T')[0];
        
        return {
            totalBookings: this.bookings.length,
            upcomingAppointments: this.bookings.filter(b => new Date(b.date) > new Date()).length,
            todayConsultations: this.bookings.filter(b => b.date === today).length
        };
    }
}

// Export for use in other modules
const dataManager = new DataManager();