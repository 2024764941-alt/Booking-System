<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="com.dottstudio.model.User" %>
        <% User user=(User) session.getAttribute("user"); if (user==null) { response.sendRedirect("login.jsp"); return;
            } if (!"DESIGNER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect("index.jsp"); return; } %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Designer Dashboard - DotsStudio</title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link
                    href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
                <style>
                    /* Force override any caching issues */
                    .dashboard {
                        position: relative !important;
                    }

                    .main-content {
                        margin-left: 260px !important;
                        width: calc(100% - 260px) !important;
                        max-width: calc(100vw - 260px) !important;
                        overflow-x: hidden !important;
                    }
                </style>
                <link rel="stylesheet"
                    href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
                <style>
                    /* Shared Dashboard Styles from Admin Panel */
                    body {
                        background-color: #f5f7fa;
                        margin: 0;
                        font-family: 'Lato', sans-serif;
                    }

                    .dashboard {
                        display: flex;
                        min-height: 100vh;
                    }

                    .sidebar {
                        width: 260px;
                        background: linear-gradient(180deg, #111111 0%, #1a1a1a 100%);
                        color: #ecf0f1;
                        display: flex;
                        flex-direction: column;
                        position: fixed;
                        height: 100vh;
                        z-index: 100;
                    }

                    .sidebar-header {
                        padding: 25px 20px;
                        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
                        text-align: center;
                    }

                    .sidebar-logo {
                        font-size: 1.5em;
                        font-weight: 700;
                        color: #fff;
                        margin-bottom: 5px;
                        font-family: 'Playfair Display', serif;
                    }

                    .sidebar-subtitle {
                        font-size: 0.85em;
                        color: #bdc3c7;
                        font-weight: 400;
                    }

                    .sidebar-nav {
                        flex: 1;
                        padding-top: 20px;
                        display: flex;
                        flex-direction: column;
                        width: 100%;
                    }

                    .nav-item {
                        display: grid !important;
                        grid-template-columns: 24px 1fr !important;
                        gap: 16px !important;
                        align-items: center !important;
                        padding: 15px 20px !important;
                        text-decoration: none !important;
                        color: #ecf0f1 !important;
                        transition: all 0.3s !important;
                        border-left: 3px solid transparent !important;
                        min-height: 50px !important;
                        box-sizing: border-box !important;
                    }

                    .nav-item .nav-icon {
                        width: 24px !important;
                        min-width: 24px !important;
                        max-width: 24px !important;
                        height: 24px !important;
                        display: flex !important;
                        align-items: center !important;
                        justify-content: flex-start !important;
                        flex-shrink: 0 !important;
                        margin: 0 !important;
                        padding: 0 !important;
                    }

                    .nav-item .nav-label {
                        font-weight: 500 !important;
                        font-size: 0.95rem !important;
                        letter-spacing: 0.5px !important;
                        white-space: nowrap !important;
                        margin: 0 !important;
                        padding: 0 !important;
                    }

                    .nav-item:hover {
                        background-color: rgba(255, 255, 255, 0.1) !important;
                        border-left-color: #888888 !important;
                    }

                    .nav-item.active {
                        background-color: rgba(255, 255, 255, 0.15) !important;
                        border-left-color: #888888 !important;
                    }

                    .sidebar-footer {
                        margin-top: auto;
                        border-top: 1px solid rgba(255, 255, 255, 0.1);
                        padding: 20px;
                    }

                    .main-content {
                        margin-left: 260px;
                        flex: 1;
                        padding: 40px;
                        min-height: 100vh;
                        width: calc(100vw - 260px);
                        max-width: calc(100vw - 260px);
                        box-sizing: border-box;
                        overflow-x: hidden;
                    }

                    .dashboard-section {
                        display: none;
                        animation: fadeIn 0.3s ease-in;
                    }

                    .dashboard-section.active {
                        display: block;
                    }

                    .section-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 30px;
                        padding-bottom: 20px;
                        border-bottom: 3px solid #e8eaed;
                    }

                    .section-header h2 {
                        font-size: 1.8em;
                        font-weight: 700;
                        color: #2c3e50;
                        margin: 0;
                        letter-spacing: 0.5px;
                        font-family: 'Playfair Display', serif;
                    }

                    .section-subtitle {
                        color: #7f8c8d;
                        font-size: 0.95em;
                        margin-top: 5px;
                    }

                    .table-wrapper {
                        width: 100%;
                        overflow-x: auto;
                        margin-top: 20px;
                        background: white;
                        border-radius: 8px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
                    }

                    .modern-table {
                        width: 100%;
                        border-collapse: collapse;
                    }

                    .modern-table th {
                        padding: 15px;
                        text-align: left;
                        background: #f8f9fa;
                        border-bottom: 2px solid #e9ecef;
                        color: #495057;
                        font-weight: 600;
                    }

                    .modern-table td {
                        padding: 15px;
                        border-bottom: 1px solid #e9ecef;
                        vertical-align: middle;
                    }

                    @keyframes fadeIn {
                        from {
                            opacity: 0;
                        }

                        to {
                            opacity: 1;
                        }
                    }

                    /* Calendar Styles */
                    .calendar-container {
                        padding: 20px;
                        background: #fff;
                        border: 1px solid #ddd;
                        border-radius: 12px;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
                        max-width: 700px;
                        box-sizing: border-box;
                    }

                    .calendar-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 20px;
                    }

                    .calendar-header h3 {
                        margin: 0;
                        font-size: 1.2em;
                        color: #2c3e50;
                    }

                    .calendar-grid {
                        display: grid;
                        grid-template-columns: repeat(7, 1fr);
                        gap: 8px;
                    }

                    .calendar-grid>div {
                        text-align: center;
                    }

                    .calendar-day {
                        aspect-ratio: 1;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        background: #f8f9fa;
                        border-radius: 4px;
                        cursor: pointer;
                        font-weight: 500;
                        transition: all 0.2s;
                        border: 1px solid transparent;
                    }

                    .calendar-day:hover {
                        background: #e9ecef;
                        border-color: #adb5bd;
                    }

                    .calendar-day.available {
                        background: #c6a87c;
                        color: white;
                        border-color: #b0966a;
                    }

                    .calendar-day.empty {
                        background: transparent;
                        cursor: default;
                    }
                </style>
            </head>

            <body>
                <div id="app">
                    <script>
                        const sessionUser = {
                            firstName: "<%= user.getFirstName() %>",
                            lastName: "<%= user.getLastName() %>",
                            email: "<%= user.getEmail() %>",
                            role: "<%= user.getRoleName() %>",
                            id: <%= user.getId() %>
            };
                        localStorage.setItem('dott_current_user', JSON.stringify(sessionUser));
                    </script>

                    <div id="designer-dashboard-page"></div> <!-- Marker for script.js to know we are here -->

                    <section id="designer-dashboard" class="page active">
                        <div class="dashboard">
                            <script>
                                // Robust source of truth for the logged-in user
                                window.serverCurrentUserEmail = "<%= user.getEmail() %>";
                            </script>
                            <!-- Sidebar Navigation -->
                            <aside class="sidebar">
                                <div class="sidebar-header">
                                    <div class="sidebar-logo">DotsStudio</div>
                                    <div class="sidebar-subtitle">Designer Panel</div>
                                </div>

                                <nav class="sidebar-nav">
                                    <a href="#" id="nav-bookings" class="nav-item active">
                                        <span class="nav-icon"><i class="fas fa-calendar-check"></i></span>
                                        <span class="nav-label">My Bookings</span>
                                    </a>
                                    <a href="#" id="nav-my-profile" class="nav-item">
                                        <span class="nav-icon"><i class="fas fa-user-circle"></i></span>
                                        <span class="nav-label">My Profile</span>
                                    </a>
                                    <a href="#" id="nav-my-schedule" class="nav-item">
                                        <span class="nav-icon"><i class="fas fa-clock"></i></span>
                                        <span class="nav-label">My Schedule</span>
                                    </a>
                                </nav>

                                <div class="sidebar-footer">
                                    <a href="index.jsp" class="nav-item">
                                        <span class="nav-icon"><i class="fas fa-home"></i></span>
                                        <span class="nav-label">Home</span>
                                    </a>
                                    <a href="#" id="nav-logout" class="nav-item">
                                        <span class="nav-icon"><i class="fas fa-sign-out-alt"></i></span>
                                        <span class="nav-label">Logout</span>
                                    </a>
                                </div>
                            </aside>

                            <!-- Main Content Area -->
                            <main class="main-content">

                                <!-- My Bookings Section -->
                                <div id="section-bookings" class="dashboard-section active">
                                    <div class="section-header">
                                        <div>
                                            <h2>My Bookings</h2>
                                            <p class="section-subtitle">Upcoming appointments assigned to you</p>
                                        </div>
                                    </div>
                                    <div class="table-wrapper">
                                        <table class="modern-table">
                                            <thead>
                                                <tr>
                                                    <th style="text-align: center;">ID</th>
                                                    <th style="text-align: center;">Client</th>
                                                    <th style="text-align: center;">Contact</th>
                                                    <th style="text-align: center;">Date & Time</th>
                                                    <th style="text-align: center;">Notes</th>
                                                    <th style="text-align: center;">Status</th>
                                                    <th style="text-align: center;">Action</th>
                                                </tr>
                                            </thead>
                                            <tbody id="my-bookings-tbody">
                                                <tr>
                                                    <td colspan="7" style="padding: 20px; text-align: center;">
                                                        Loading...
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>

                                <!-- My Schedule Section -->
                                <div id="section-my-schedule" class="dashboard-section">
                                    <div class="section-header"
                                        style="display: flex; justify-content: space-between; align-items: center;">
                                        <div>
                                            <h2>My Schedule</h2>
                                            <p class="section-subtitle">Manage your availability</p>
                                        </div>
                                        <button onclick="openScheduleSettings()" class="btn secondary"
                                            style="border-radius: 20px; padding: 8px 20px;">
                                            <i class="fas fa-cog"></i> Settings
                                        </button>
                                    </div>

                                    <!-- Schedule Settings Modal -->
                                    <div id="schedule-settings-modal"
                                        style="display: none; position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.5); z-index: 1001;">
                                        <div
                                            style="display: flex; align-items: center; justify-content: center; width: 100%; height: 100%;">
                                            <div
                                                style="background: white; padding: 30px; border-radius: 12px; width: 500px; max-width: 90%; box-shadow: 0 5px 20px rgba(0,0,0,0.2);">
                                                <h3 style="margin-top: 0; margin-bottom: 20px; color: #333;">Schedule
                                                    Settings &
                                                    Capacity</h3>

                                                <!-- Status -->
                                                <div style="margin-bottom: 20px;">
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 8px;">Global
                                                        Status</label>
                                                    <select id="setting-status" class="form-input"
                                                        style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                                                        <option value="Active">Active</option>
                                                        <option value="Inactive">Inactive</option>
                                                        <option value="On Leave">On Leave</option>
                                                    </select>
                                                </div>

                                                <!-- Capacity Grid -->
                                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                                                    <div>
                                                        <label
                                                            style="font-size: 0.9em; font-weight: 600; color: #666; display: block; margin-bottom: 5px;">Max
                                                            Hours</label>
                                                        <input type="number" id="setting-max-hours" class="form-input"
                                                            style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                                                    </div>
                                                    <div>
                                                        <label
                                                            style="font-size: 0.9em; font-weight: 600; color: #666; display: block; margin-bottom: 5px;">Min
                                                            Hours</label>
                                                        <input type="number" id="setting-min-hours" class="form-input"
                                                            style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                                                    </div>
                                                    <div>
                                                        <label
                                                            style="font-size: 0.9em; font-weight: 600; color: #666; display: block; margin-bottom: 5px;">Max
                                                            Projects</label>
                                                        <input type="number" id="setting-max-projects"
                                                            class="form-input"
                                                            style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                                                    </div>
                                                    <div>
                                                        <label
                                                            style="font-size: 0.9em; font-weight: 600; color: #666; display: block; margin-bottom: 5px;">Max
                                                            Bookings</label>
                                                        <input type="number" id="setting-max-bookings"
                                                            class="form-input"
                                                            style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                                                    </div>
                                                </div>

                                                <!-- Default Working Hours -->
                                                <div
                                                    style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee;">
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 12px;">
                                                        <i class="fas fa-clock"
                                                            style="margin-right: 8px; color: #c6a87c;"></i>Default
                                                        Working
                                                        Hours
                                                    </label>
                                                    <div
                                                        style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                                                        <div>
                                                            <label
                                                                style="font-size: 0.85em; font-weight: 600; color: #666; display: block; margin-bottom: 5px;">Start
                                                                Time</label>
                                                            <input type="time" id="setting-start-time" value="09:00"
                                                                class="form-input"
                                                                style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                                                        </div>
                                                        <div>
                                                            <label
                                                                style="font-size: 0.85em; font-weight: 600; color: #666; display: block; margin-bottom: 5px;">End
                                                                Time</label>
                                                            <input type="time" id="setting-end-time" value="17:00"
                                                                class="form-input"
                                                                style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                                                        </div>
                                                    </div>
                                                    <small style="display: block; margin-top: 8px; color: #888;">These
                                                        times
                                                        will be used as defaults when setting daily
                                                        availability.</small>
                                                </div>

                                                <!-- Specific Slot Availability -->
                                                <div
                                                    style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee;">
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 8px; text-transform: uppercase; font-size: 0.85em; letter-spacing: 0.5px;">
                                                        Specific Slot Availability (e.g. 09:00, 14:00)
                                                    </label>
                                                    <input type="text" id="setting-slot-availability"
                                                        placeholder="e.g. 09:00, 14:00 (comma separated)"
                                                        class="form-input"
                                                        style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 0.95em;">
                                                    <small
                                                        style="display: block; margin-top: 8px; color: #888;">Overrides
                                                        specific dates if set.</small>
                                                </div>

                                                <div
                                                    style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 25px;">
                                                    <button onclick="closeScheduleSettings()" class="btn secondary"
                                                        style="padding: 10px 20px; border-radius: 6px;">Cancel</button>
                                                    <button onclick="saveScheduleSettings()" class="btn primary"
                                                        style="padding: 10px 20px; border-radius: 6px; background: #c6a87c; color: white; border: none;">Save
                                                        Settings</button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Schedule Overview Cards -->
                                    <div
                                        style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 25px;">
                                        <!-- Status Card -->
                                        <div
                                            style="background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); border-left: 4px solid #28a745;">
                                            <div
                                                style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
                                                <i class="fas fa-circle" style="color: #28a745; font-size: 0.8em;"></i>
                                                <span style="font-weight: 600; color: #555;">Status</span>
                                            </div>
                                            <div id="schedule-status-display"
                                                style="font-size: 1.4em; font-weight: 700; color: #28a745;">Active</div>
                                        </div>

                                        <!-- Working Hours Card -->
                                        <div
                                            style="background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); border-left: 4px solid #c6a87c;">
                                            <div
                                                style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
                                                <i class="fas fa-clock" style="color: #c6a87c;"></i>
                                                <span style="font-weight: 600; color: #555;">Default Hours</span>
                                            </div>
                                            <div id="schedule-hours-display"
                                                style="font-size: 1.2em; font-weight: 600; color: #333;">9:00 AM - 5:00
                                                PM
                                            </div>
                                        </div>

                                        <!-- Min Hours Card -->
                                        <div
                                            style="background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); border-left: 4px solid #17a2b8;">
                                            <div
                                                style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
                                                <i class="fas fa-hourglass-start" style="color: #17a2b8;"></i>
                                                <span style="font-weight: 600; color: #555;">Min Hours/Week</span>
                                            </div>
                                            <div id="schedule-min-hours-display"
                                                style="font-size: 1.4em; font-weight: 700; color: #333;">0</div>
                                        </div>

                                        <!-- Max Hours Card -->
                                        <div
                                            style="background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); border-left: 4px solid #6c5ce7;">
                                            <div
                                                style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
                                                <i class="fas fa-hourglass-end" style="color: #6c5ce7;"></i>
                                                <span style="font-weight: 600; color: #555;">Max Hours/Week</span>
                                            </div>
                                            <div id="schedule-max-hours-display"
                                                style="font-size: 1.4em; font-weight: 700; color: #333;">40</div>
                                        </div>

                                        <!-- Max Projects Card -->
                                        <div
                                            style="background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); border-left: 4px solid #fd7e14;">
                                            <div
                                                style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
                                                <i class="fas fa-project-diagram" style="color: #fd7e14;"></i>
                                                <span style="font-weight: 600; color: #555;">Max Bookings</span>
                                            </div>
                                            <div id="schedule-max-projects-display"
                                                style="font-size: 1.4em; font-weight: 700; color: #333;">5</div>
                                        </div>

                                    </div>

                                    <!-- Flex Wrapper for Calendar & Selection Panel -->
                                    <div
                                        style="display: flex; gap: 20px; align-items: flex-start; flex-wrap: wrap; justify-content: center;">
                                        <!-- Calendar Container (smaller) -->
                                        <div class="calendar-container"
                                            style="max-width: 700px; flex: 1; min-width: 300px;">

                                            <!-- Calendar Controls -->
                                            <div class="calendar-header">
                                                <div style="display: flex; align-items: center; gap: 15px;">
                                                    <button id="prev-month-btn"
                                                        style="background: none; border: 1px solid #ddd; width: 32px; height: 32px; border-radius: 50%; cursor: pointer;">&lt;</button>
                                                    <h3 id="calendar-month-year">Month Year</h3>
                                                    <button id="next-month-btn"
                                                        style="background: none; border: 1px solid #ddd; width: 32px; height: 32px; border-radius: 50%; cursor: pointer;">&gt;</button>
                                                </div>
                                                <div style="font-size: 0.9em; color: #666;">
                                                    <span
                                                        style="display: inline-block; width: 10px; height: 10px; background: #c6a87c; border-radius: 50%; margin-right: 5px;"></span>
                                                    Available
                                                    <span
                                                        style="display: inline-block; width: 10px; height: 10px; background: #f8f9fa; border: 1px solid #ddd; border-radius: 50%; margin-left: 10px; margin-right: 5px;"></span>
                                                    Unavailable
                                                </div>
                                            </div>

                                            <!-- Days Header -->
                                            <div class="calendar-grid"
                                                style="margin-bottom: 10px; font-weight: 600; color: #888;">
                                                <div>Sun</div>
                                                <div>Mon</div>
                                                <div>Tue</div>
                                                <div>Wed</div>
                                                <div>Thu</div>
                                                <div>Fri</div>
                                                <div>Sat</div>
                                            </div>

                                            <!-- Calendar Body -->
                                            <div id="calendar-days" class="calendar-grid">
                                                <!-- Days injected here -->
                                            </div>

                                            <!-- Day Detail Modal (Hidden by default) -->
                                            <div id="schedule-day-modal"
                                                style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center;">
                                                <div
                                                    style="background: white; padding: 30px; border-radius: 12px; width: 400px; max-width: 90%; box-shadow: 0 5px 20px rgba(0,0,0,0.2);">
                                                    <div
                                                        style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                                        <h3 id="modal-date-title" style="margin: 0; color: #333;">
                                                            January 1,
                                                            2026</h3>
                                                        <button onclick="closeScheduleModal()"
                                                            style="background: none; border: none; font-size: 1.2em; cursor: pointer; color: #888;">&times;</button>
                                                    </div>

                                                    <div style="margin-bottom: 20px;">
                                                        <label
                                                            style="display: block; font-weight: 600; color: #555; margin-bottom: 10px;">Availability
                                                            Status</label>
                                                        <div style="display: flex; gap: 10px;">
                                                            <button type="button" id="btn-status-available"
                                                                onclick="updateDayAvailability(true)"
                                                                style="flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white;">Available</button>
                                                            <button type="button" id="btn-status-unavailable"
                                                                onclick="updateDayAvailability(false)"
                                                                style="flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white;">Unavailable</button>
                                                        </div>
                                                    </div>

                                                    <div id="time-settings" style="margin-bottom: 25px;">
                                                        <label
                                                            style="display: block; font-weight: 600; color: #555; margin-bottom: 15px;">Working
                                                            Hours</label>
                                                        <div
                                                            style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                                                            <div>
                                                                <label
                                                                    style="font-size: 0.85em; color: #666; display: block; margin-bottom: 5px;">Start
                                                                    Time</label>
                                                                <input type="time" id="day-start-time" value="09:00"
                                                                    style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                                                            </div>
                                                            <div>
                                                                <label
                                                                    style="font-size: 0.85em; color: #666; display: block; margin-bottom: 5px;">End
                                                                    Time</label>
                                                                <input type="time" id="day-end-time" value="17:00"
                                                                    style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <div style="display: flex; justify-content: flex-end; gap: 10px;">
                                                        <button onclick="closeScheduleModal()" class="btn secondary"
                                                            style="padding: 10px 20px; border-radius: 6px;">Cancel</button>
                                                        <button onclick="saveDaySchedule()" class="btn primary"
                                                            style="padding: 10px 20px; border-radius: 6px; background: #c6a87c; color: white; border: none;">Save
                                                            Schedule</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- INSERTED SELECTION PANEL -->
                                        <div id="selection-panel-container">
                                            <div class="sel-panel">
                                                <div class="sel-header">
                                                    <h3>Selected Dates</h3>
                                                    <span class="sel-badge">2</span>
                                                </div>

                                                <div class="sel-list-container">
                                                    <!-- Date Item 1 -->
                                                    <div class="sel-item">
                                                        <div class="sel-date-group">
                                                            <div class="sel-icon">
                                                                <svg width="20" height="20" viewBox="0 0 24 24"
                                                                    fill="none" stroke="currentColor" stroke-width="2">
                                                                    <rect x="3" y="4" width="18" height="18" rx="2"
                                                                        ry="2">
                                                                    </rect>
                                                                    <line x1="16" y1="2" x2="16" y2="6"></line>
                                                                    <line x1="8" y1="2" x2="8" y2="6"></line>
                                                                    <line x1="3" y1="10" x2="21" y2="10"></line>
                                                                </svg>
                                                            </div>
                                                            <div class="sel-details">
                                                                <span class="sel-date-text">Jan 12</span>
                                                                <span class="sel-time-text">9:00 AM</span>
                                                            </div>
                                                        </div>
                                                        <button class="sel-remove-btn" aria-label="Remove date">
                                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                                stroke="currentColor" stroke-width="2"
                                                                stroke-linecap="round" stroke-linejoin="round">
                                                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                                                <line x1="6" y1="6" x2="18" y2="18"></line>
                                                            </svg>
                                                        </button>
                                                    </div>

                                                    <!-- Date Item 2 -->
                                                    <div class="sel-item">
                                                        <div class="sel-date-group">
                                                            <div class="sel-icon">
                                                                <svg width="20" height="20" viewBox="0 0 24 24"
                                                                    fill="none" stroke="currentColor" stroke-width="2">
                                                                    <rect x="3" y="4" width="18" height="18" rx="2"
                                                                        ry="2">
                                                                    </rect>
                                                                    <line x1="16" y1="2" x2="16" y2="6"></line>
                                                                    <line x1="8" y1="2" x2="8" y2="6"></line>
                                                                    <line x1="3" y1="10" x2="21" y2="10"></line>
                                                                </svg>
                                                            </div>
                                                            <div class="sel-details">
                                                                <span class="sel-date-text">Jan 13</span>
                                                                <span class="sel-time-text">2:00 PM</span>
                                                            </div>
                                                        </div>
                                                        <button class="sel-remove-btn" aria-label="Remove date">
                                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                                stroke="currentColor" stroke-width="2"
                                                                stroke-linecap="round" stroke-linejoin="round">
                                                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                                                <line x1="6" y1="6" x2="18" y2="18"></line>
                                                            </svg>
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <style>
                                            /* 
                                       ISOLATED STYLES FOR SELECTION PANEL 
                                       Prefix: sel-
                                    */
                                            #selection-panel-container {
                                                /* Suggesting a width, but it feels responsive to parent */
                                                width: 100%;
                                                max-width: 320px;
                                                box-sizing: border-box;
                                                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                                            }

                                            .sel-panel {
                                                background: #ffffff;
                                                border: 1px solid #e2e8f0;
                                                border-radius: 16px;
                                                padding: 24px;
                                                box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
                                            }

                                            .sel-header {
                                                display: flex;
                                                justify-content: space-between;
                                                align-items: center;
                                                margin-bottom: 20px;
                                                padding-bottom: 12px;
                                                border-bottom: 1px solid #f1f5f9;
                                                margin-top: 0;
                                            }

                                            .sel-header h3 {
                                                margin: 0;
                                                font-size: 16px;
                                                font-weight: 700;
                                                color: #1e293b;
                                                letter-spacing: -0.02em;
                                            }

                                            .sel-badge {
                                                background: #0f172a;
                                                color: #fff;
                                                font-size: 12px;
                                                font-weight: 600;
                                                padding: 2px 8px;
                                                border-radius: 9999px;
                                            }

                                            .sel-list-container {
                                                display: flex;
                                                flex-direction: column;
                                                gap: 12px;
                                            }

                                            .sel-item {
                                                display: flex;
                                                justify-content: space-between;
                                                align-items: center;
                                                padding: 12px;
                                                background: #f8fafc;
                                                border: 1px solid #e2e8f0;
                                                border-radius: 12px;
                                                transition: all 0.2s ease;
                                            }

                                            .sel-item:hover {
                                                background: #ffffff;
                                                border-color: #cbd5e1;
                                                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
                                                transform: translateY(-1px);
                                            }

                                            .sel-date-group {
                                                display: flex;
                                                align-items: center;
                                                gap: 12px;
                                            }

                                            .sel-icon {
                                                color: #64748b;
                                                display: flex;
                                                align-items: center;
                                            }

                                            .sel-details {
                                                display: flex;
                                                flex-direction: column;
                                                line-height: 1.3;
                                            }

                                            .sel-date-text {
                                                font-size: 14px;
                                                font-weight: 600;
                                                color: #334155;
                                            }

                                            .sel-time-text {
                                                font-size: 13px;
                                                color: #64748b;
                                            }

                                            .sel-remove-btn {
                                                background: transparent;
                                                border: none;
                                                color: #94a3b8;
                                                cursor: pointer;
                                                padding: 6px;
                                                border-radius: 8px;
                                                display: flex;
                                                align-items: center;
                                                justify-content: center;
                                                transition: all 0.2s ease;
                                            }

                                            /* Using !important to ensure no conflict with global button styles if any */
                                            .sel-remove-btn:hover {
                                                background: #fee2e2 !important;
                                                color: #ef4444 !important;
                                            }

                                            .sel-remove-btn:active {
                                                background: #fecaca !important;
                                                transform: scale(0.95);
                                            }
                                        </style>

                                    </div> <!-- End of flex wrapper -->
                                </div>

                                <!-- My Profile Section -->

                                <div id="section-my-profile" class="dashboard-section">
                                    <div class="section-header"
                                        style="display: flex; justify-content: space-between; align-items: center;">
                                        <div>
                                            <h2>My Profile</h2>
                                            <p class="section-subtitle">Manage your public profile and settings</p>
                                        </div>
                                        <button id="btn-edit-profile" onclick="toggleProfileEdit()"
                                            class="btn secondary" style="border-radius: 20px; padding: 8px 20px;">
                                            <i class="fas fa-pencil-alt"></i> Edit Profile
                                        </button>
                                    </div>

                                    <div
                                        style="background: white; padding: 40px; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); max-width: 900px; width: 100%; margin: 0 auto; box-sizing: border-box;">
                                        <form id="designer-profile-form">
                                            <!-- Header / Avatar Area (Optional placeholder) -->
                                            <div
                                                style="display: flex; align-items: center; gap: 20px; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #eee;">
                                                <div
                                                    style="width: 80px; height: 80px; background: linear-gradient(135deg, #c6a87c, #b08d55); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 2em; font-weight: bold;">
                                                    <i class="fas fa-user"></i>
                                                </div>
                                                <div>
                                                    <h3 style="margin: 0; color: #333;">Personal Details</h3>
                                                    <p style="margin: 5px 0 0; color: #888; font-size: 0.9em;">Your
                                                        contact
                                                        and login information</p>
                                                </div>
                                            </div>

                                            <div
                                                style="display: grid; grid-template-columns: 1fr 1fr; gap: 25px; margin-bottom: 25px;">
                                                <div>
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 8px;">First
                                                        Name</label>
                                                    <input type="text" id="my-firstname" class="form-input" disabled
                                                        required
                                                        style="width: 100%; padding: 12px; border: 1px solid #e0e0e0; border-radius: 8px; background: #f9f9f9; color: #555;">
                                                </div>
                                                <div>
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 8px;">Last
                                                        Name</label>
                                                    <input type="text" id="my-lastname" class="form-input" disabled
                                                        required
                                                        style="width: 100%; padding: 12px; border: 1px solid #e0e0e0; border-radius: 8px; background: #f9f9f9; color: #555;">
                                                </div>
                                            </div>

                                            <div
                                                style="display: grid; grid-template-columns: 1fr 1fr; gap: 25px; margin-bottom: 25px;">
                                                <div>
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 8px;">Email
                                                        Address</label>
                                                    <input type="email" id="my-email" class="form-input" disabled
                                                        readonly
                                                        style="width: 100%; padding: 12px; border: 1px solid #e0e0e0; border-radius: 8px; background: #eee; color: #777; cursor: not-allowed;">
                                                    <small style="color: #bbb; display: block; margin-top: 4px;">Email
                                                        cannot be changed</small>
                                                </div>
                                                <div>
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 8px;">Phone
                                                        Number</label>
                                                    <input type="tel" id="my-phone" class="form-input" disabled
                                                        style="width: 100%; padding: 12px; border: 1px solid #e0e0e0; border-radius: 8px; background: #f9f9f9; color: #555;">
                                                </div>
                                            </div>

                                            <div style="margin-bottom: 30px;">
                                                <h3
                                                    style="margin: 30px 0 15px; color: #333; font-size: 1.1em; border-bottom: 1px solid #eee; padding-bottom: 10px;">
                                                    Professional Profile</h3>

                                                <div style="margin-bottom: 20px;">
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 8px;">Specialty</label>

                                                    <!-- Hidden input to store actual value -->
                                                    <input type="hidden" id="my-specialty" value="">

                                                    <!-- Custom Multi-Select Dropdown -->
                                                    <div id="my-specialty-multiselect" class="custom-multiselect"
                                                        style="position: relative;">
                                                        <!-- Display Box -->
                                                        <div class="select-box"
                                                            style="width: 100%; padding: 12px; border: 1px solid #e0e0e0; border-radius: 8px; background: #f9f9f9; color: #555; cursor: not-allowed; min-height: 44px; display: flex; align-items: center; justify-content: space-between;">
                                                            <span class="selected-text" style="flex: 1; color: #555;">No
                                                                specialty selected</span>
                                                            <i class="fas fa-chevron-down"
                                                                style="color: #999; font-size: 0.8em;"></i>
                                                        </div>

                                                        <!-- Dropdown Options -->
                                                        <div id="my-specialty-options" class="multiselect-options"
                                                            style="display: none; position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #e0e0e0; border-radius: 8px; margin-top: 4px; max-height: 250px; overflow-y: auto; z-index: 1000; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
                                                            <div class="multiselect-option" data-value="Modern"
                                                                style="padding: 12px 16px; cursor: pointer; transition: background 0.2s; font-size: 14px;">
                                                                <span>Modern</span>
                                                            </div>
                                                            <div class="multiselect-option" data-value="Bohemian"
                                                                style="padding: 12px 16px; cursor: pointer; transition: background 0.2s; font-size: 14px;">
                                                                <span>Bohemian</span>
                                                            </div>
                                                            <div class="multiselect-option" data-value="Minimalist"
                                                                style="padding: 12px 16px; cursor: pointer; transition: background 0.2s; font-size: 14px;">
                                                                <span>Minimalist</span>
                                                            </div>
                                                            <div class="multiselect-option" data-value="Industrial"
                                                                style="padding: 12px 16px; cursor: pointer; transition: background 0.2s; font-size: 14px;">
                                                                <span>Industrial</span>
                                                            </div>
                                                            <div class="multiselect-option" data-value="Scandinavian"
                                                                style="padding: 12px 16px; cursor: pointer; transition: background 0.2s; font-size: 14px;">
                                                                <span>Scandinavian</span>
                                                            </div>
                                                            <div class="multiselect-option" data-value="Traditional"
                                                                style="padding: 12px 16px; cursor: pointer; transition: background 0.2s; font-size: 14px;">
                                                                <span>Traditional</span>
                                                            </div>
                                                            <div class="multiselect-option" data-value="Eclectic"
                                                                style="padding: 12px 16px; cursor: pointer; transition: background 0.2s; font-size: 14px;">
                                                                <span>Eclectic</span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div>
                                                    <label
                                                        style="display: block; font-weight: 600; color: #444; margin-bottom: 8px;">Bio
                                                        / Experience</label>
                                                    <textarea id="my-bio" rows="4" class="form-input" disabled
                                                        style="width: 100%; padding: 12px; border: 1px solid #e0e0e0; border-radius: 8px; background: #f9f9f9; color: #555; font-family: inherit; resize: vertical;"></textarea>
                                                </div>
                                            </div>



                                            <!-- Hidden Actions -->
                                            <div id="profile-actions"
                                                style="display: none; justify-content: flex-end; gap: 15px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;">
                                                <button type="button" onclick="cancelProfileEdit()"
                                                    class="btn secondary"
                                                    style="background: #f0f0f0; color: #666; border: none; padding: 10px 25px; border-radius: 8px; cursor: pointer;">Cancel</button>
                                                <button type="submit" class="btn primary"
                                                    style="background: linear-gradient(135deg, #c6a87c 0%, #a68b5e 100%); color: white; border: none; padding: 10px 30px; border-radius: 8px; cursor: pointer; font-weight: 600; box-shadow: 0 4px 15px rgba(198, 168, 124, 0.4);">Save
                                                    Changes</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                    </section>
                </div>

                <!-- Scripts -->
                <script src="custom-confirm.js?v=<%= System.currentTimeMillis() %>"></script>
                <script src="selected-dates-panel.js?v=<%= System.currentTimeMillis() %>"></script>
                <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
            </body>

            </html>