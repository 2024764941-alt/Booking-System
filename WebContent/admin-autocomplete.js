// Auto-fill phone number when searching for user email in Add Staff modal
// [REVERTED by User Request] - Search Logic Disabled to stop 404s
document.addEventListener('DOMContentLoaded', function () {
    const searchInput = document.getElementById('promote-search');
    const resultsList = document.getElementById('search-results');
    const emailField = document.getElementById('promote-email');
    const phoneField = document.getElementById('promote-phone');

    if (!searchInput || !resultsList) return;

    let searchTimeout;

    searchInput.addEventListener('input', function () {
        // Feature disabled as requested.
        // To re-enable, we need a working API endpoint (UserSearchServlet).
        return;
    });

    // Hide results when clicking outside
    document.addEventListener('click', function (e) {
        if (!searchInput.contains(e.target) && !resultsList.contains(e.target)) {
            resultsList.style.display = 'none';
        }
    });
});
