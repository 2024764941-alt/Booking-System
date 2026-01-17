<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>My Profile - DotsStudio</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
    </head>

    <body>
        <div id="app">
            <section id="profile-page" class="page active">

                <jsp:include page="header.jsp" />

                <div class="container" style="margin-top: 2rem;">
                    <div class="page-header">
                        <h1>My Profile</h1>
                        <p>Manage your bookings and personal details</p>
                    </div>

                    <div class="profile-layout" style="margin-left: 0; padding-left: 0; display: block;">

                        <!-- Tab Navigation -->
                        <div class="profile-tabs"
                            style="margin-bottom: 2rem; display: flex; gap: 1rem; border-bottom: 2px solid #eee;">
                            <button class="tab-btn active" data-target="bookings"
                                style="padding: 1rem 2rem; border: none; background: none; font-family: 'Lato', sans-serif; font-size: 1.1rem; cursor: pointer; border-bottom: 3px solid transparent;">My
                                Bookings</button>
                            <button class="tab-btn" data-target="settings"
                                style="padding: 1rem 2rem; border: none; background: none; font-family: 'Lato', sans-serif; font-size: 1.1rem; cursor: pointer; border-bottom: 3px solid transparent;">Profile
                                Settings</button>
                        </div>

                        <!-- Left Column: Bookings -->
                        <div class="profile-section tab-content active" id="my-bookings-section">
                            <h2>My Bookings</h2>
                            <div id="customer-bookings-list" class="bookings-grid">
                                <!-- Bookings will be populated here -->
                                <p class="empty-state">Loading bookings...</p>
                            </div>
                        </div>

                        <!-- Right Column: Settings -->
                        <div class="profile-section tab-content" id="profile-settings-section" style="display: none;">
                            <h2>Profile Settings</h2>
                            <form id="profile-form" class="auth-form">
                                <div class="form-group">
                                    <label for="profile-firstname">First Name</label>
                                    <input type="text" id="profile-firstname" required>
                                </div>
                                <div class="form-group">
                                    <label for="profile-lastname">Last Name</label>
                                    <input type="text" id="profile-lastname" required>
                                </div>
                                <div class="form-group">
                                    <label for="profile-email">Email</label>
                                    <input type="email" id="profile-email" disabled title="Email cannot be changed">
                                </div>
                                <div class="form-group">
                                    <label for="profile-phone">Phone Number</label>
                                    <input type="tel" id="profile-phone">
                                </div>
                                <div class="form-group">
                                    <label for="profile-address">Address</label>
                                    <input type="text" id="profile-address">
                                </div>
                                <div class="form-group">
                                    <label for="profile-password">New Password (leave blank to keep)</label>
                                    <input type="password" id="profile-password">
                                </div>
                                <button type="submit" class="btn primary">Save Changes</button>
                            </form>
                        </div>
                    </div>
                </div>

                <jsp:include page="footer.jsp" />
            </section>
        </div>
        <script src="script.js?v=<%= new java.util.Date().getTime() %>"></script>
    </body>

    </html>