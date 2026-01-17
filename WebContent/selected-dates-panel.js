// Selected Dates Panel Functionality
function updateSelectedDatesPanel() {
    const panel = document.querySelector('.sel-list-container');
    const badge = document.querySelector('.sel-badge');
    if (!panel) return;

    // Get all available dates from calendarAvailability
    const availableDates = Object.keys(calendarAvailability)
        .filter(dateKey => calendarAvailability[dateKey].available === true)
        .sort();

    // Update badge count
    if (badge) {
        badge.textContent = availableDates.length;
    }

    // Clear existing items
    panel.innerHTML = '';

    if (availableDates.length === 0) {
        panel.innerHTML = '<p style="padding: 15px; text-align: center; color: #999;">No available dates selected</p>';
        return;
    }

    // Add each available date
    availableDates.forEach(dateKey => {
        const data = calendarAvailability[dateKey];
        const dateObj = new Date(dateKey);
        const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        const dateStr = `${monthNames[dateObj.getMonth()]} ${dateObj.getDate()}`;

        // Format time to 12hr
        const formatTime = (time) => {
            const [h, m] = time.split(':');
            const hour = parseInt(h);
            const ampm = hour >= 12 ? 'PM' : 'AM';
            const h12 = hour % 12 || 12;
            return `${h12}:${m} ${ampm}`;
        };

        const itemHTML = `
            <div class="sel-item" data-date="${dateKey}">
                <div class="sel-date-group">
                    <div class="sel-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                            <line x1="16" y1="2" x2="16" y2="6"></line>
                            <line x1="8" y1="2" x2="8" y2="6"></line>
                            <line x1="3" y1="10" x2="21" y2="10"></line>
                        </svg>
                    </div>
                    <div class="sel-details">
                        <span class="sel-date-text">${dateStr}</span>
                        <span class="sel-time-text">${formatTime(data.startTime)}</span>
                    </div>
                </div>
                <button class="sel-remove-btn" onclick="removeScheduleDate('${dateKey}')" aria-label="Remove date">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="18" y1="6" x2="6" y2="18"></line>
                        <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                </button>
            </div>
        `;
        panel.insertAdjacentHTML('beforeend', itemHTML);
    });
}

// Remove a scheduled date
function removeScheduleDate(dateKey) {
    showConfirmDialog('Remove this available date?', () => {
        // Send delete/set unavailable to API
        const params = new URLSearchParams();
        params.append('designerId', window.currentDesignerId);
        params.append('date', dateKey);
        params.append('available', 'false');
        params.append('startTime', '09:00');
        params.append('endTime', '17:00');

        fetch('api/schedule', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    // Update local data
                    if (calendarAvailability[dateKey]) {
                        calendarAvailability[dateKey].available = false;
                    }
                    // Refresh UI
                    if (typeof renderCalendar === 'function') renderCalendar();
                    updateSelectedDatesPanel();
                    if (window.showToast) window.showToast('Date removed', 'success');
                }
            })
            .catch(err => console.error('Error removing date:', err));
    });
}

