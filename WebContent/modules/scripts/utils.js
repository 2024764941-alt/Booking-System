// VALIDATION UTILITY MODULE
// Form validation and data validation helpers

class ValidationUtils {
    // Validate email format
    static isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    // Validate phone format
    static isValidPhone(phone) {
        const phoneRegex = /^[\d\s\-\+\(\)]{10,}$/;
        return phoneRegex.test(phone);
    }

    // Validate password strength (min 6 characters)
    static isValidPassword(password) {
        return password && password.length >= 6;
    }

    // Validate passwords match
    static passwordsMatch(password, confirmPassword) {
        return password === confirmPassword;
    }

    // Validate form fields are not empty
    static validateRequired(value) {
        return value && value.trim() !== '';
    }

    // Validate date is in future
    static isDateInFuture(dateString) {
        const selectedDate = new Date(dateString);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return selectedDate > today;
    }

    // Validate all login fields
    static validateLogin(email, password) {
        const errors = [];
        
        if (!this.validateRequired(email)) errors.push('Email is required');
        if (!this.isValidEmail(email)) errors.push('Invalid email format');
        if (!this.validateRequired(password)) errors.push('Password is required');
        
        return { isValid: errors.length === 0, errors };
    }

    // Validate all register fields
    static validateRegister(firstName, lastName, email, phone, address, password, confirmPassword) {
        const errors = [];
        
        if (!this.validateRequired(firstName)) errors.push('First name is required');
        if (!this.validateRequired(lastName)) errors.push('Last name is required');
        if (!this.validateRequired(email)) errors.push('Email is required');
        if (!this.isValidEmail(email)) errors.push('Invalid email format');
        if (!this.validateRequired(phone)) errors.push('Phone is required');
        if (!this.isValidPhone(phone)) errors.push('Invalid phone format');
        if (!this.validateRequired(address)) errors.push('Address is required');
        if (!this.validateRequired(password)) errors.push('Password is required');
        if (!this.isValidPassword(password)) errors.push('Password must be at least 6 characters');
        if (!this.passwordsMatch(password, confirmPassword)) errors.push('Passwords do not match');
        
        return { isValid: errors.length === 0, errors };
    }
}

// FORMAT UTILITY MODULE
// Date and data formatting helpers

class FormatUtils {
    // Format date to readable string
    static formatDate(dateString) {
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return new Date(dateString + 'T00:00:00').toLocaleDateString('en-US', options);
    }

    // Format time to 12-hour format
    static formatTime(time24) {
        const [hours, minutes] = time24.split(':');
        const hour = parseInt(hours);
        const period = hour >= 12 ? 'PM' : 'AM';
        const display = (hour % 12 || 12).toString().padStart(2, '0');
        return `${display}:${minutes} ${period}`;
    }

    // Format full date and time
    static formatDateTime(dateString, time24) {
        return `${this.formatDate(dateString)} at ${this.formatTime(time24)}`;
    }

    // Generate booking reference number
    static generateBookingRef(bookingId) {
        const timestamp = Date.now().toString().slice(-6);
        return `BK${bookingId.toString().padStart(4, '0')}-${timestamp}`;
    }

    // Truncate text
    static truncate(text, length) {
        return text.length > length ? text.substring(0, length) + '...' : text;
    }

    // Format currency
    static formatCurrency(amount) {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    }
}

// DOM UTILITY MODULE
// DOM manipulation helpers

class DOMUtils {
    // Show element
    static show(element) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.style.display = '';
    }

    // Hide element
    static hide(element) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.style.display = 'none';
    }

    // Toggle element visibility
    static toggle(element) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.style.display = element.style.display === 'none' ? '' : 'none';
    }

    // Add CSS class
    static addClass(element, className) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.classList.add(className);
    }

    // Remove CSS class
    static removeClass(element, className) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.classList.remove(className);
    }

    // Toggle CSS class
    static toggleClass(element, className) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.classList.toggle(className);
    }

    // Set text content
    static setText(element, text) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.textContent = text;
    }

    // Get input value
    static getValue(elementId) {
        const element = document.getElementById(elementId);
        return element ? element.value : '';
    }

    // Set input value
    static setValue(elementId, value) {
        const element = document.getElementById(elementId);
        if (element) element.value = value;
    }

    // Clear input
    static clearInput(elementId) {
        this.setValue(elementId, '');
    }

    // Show notification/toast
    static showNotification(message, type = 'success') {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    // Show loading indicator
    static showLoading(element) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.classList.add('loading');
    }

    // Hide loading indicator
    static hideLoading(element) {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) element.classList.remove('loading');
    }
}

// Export all utilities
const validation = ValidationUtils;
const formatter = FormatUtils;
const dom = DOMUtils;