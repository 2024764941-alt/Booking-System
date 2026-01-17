<%@ page import="com.dottstudio.model.User" %>
    <% User user=(User) session.getAttribute("user"); if (user==null) { response.sendRedirect("login.jsp"); return; } if
        ("DESIGNER".equalsIgnoreCase(user.getRoleName())) { response.sendRedirect("designer-dashboard.jsp"); return; }
        if (!"ADMIN".equalsIgnoreCase(user.getRoleName())) { response.sendRedirect("index.jsp"); return; } boolean
        isAdmin=true; %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Admin Dashboard - DotsStudio</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link
                href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
                rel="stylesheet">
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
                integrity="sha512-9usAa10IRO0HhonpyAIVpjrylPvoDwiPUiKdWk5t3PyolY1cOd4DSE0Ga+ri4AuTroPR5aQvXU9xC6qOPnzFeg=="
                crossorigin="anonymous" referrerpolicy="no-referrer" />
            <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
        </head>

        <body>
            <style>
                /* ========== ADMIN DASHBOARD LAYOUT ALIGNMENT ========== */
                #admin-dashboard {
                    min-height: 100vh;
                    background: #f5f7fa;
                }

                .dashboard {
                    display: flex;
                    min-height: 100vh;
                    width: 100%;
                }

                .sidebar {
                    width: 260px;
                    min-width: 260px;
                    background: linear-gradient(180deg, #111111 0%, #1a1a1a 100%);
                    color: #ecf0f1;
                    display: flex;
                    flex-direction: column;
                    position: fixed;
                    left: 0;
                    top: 0;
                    height: 100vh;
                    overflow-y: auto;
                    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
                    z-index: 1000;
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
                }

                .sidebar-subtitle {
                    font-size: 0.85em;
                    color: #bdc3c7;
                    font-weight: 400;
                }

                .sidebar-footer {
                    margin-top: auto;
                    border-top: 1px solid rgba(255, 255, 255, 0.1);
                }

                .main-content {
                    margin-left: 260px;
                    flex: 1;
                    padding: 40px;
                    width: calc(100% - 260px);
                    max-width: 1600px;
                    min-height: 100vh;
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
                }

                .section-subtitle {
                    color: #7f8c8d;
                    font-size: 0.95em;
                    margin-top: 5px;
                }

                /* Table Wrapper Alignment */
                .table-wrapper {
                    width: 100%;
                    overflow-x: auto;
                    margin-top: 20px;
                }

                .modern-table {
                    width: 100%;
                    border-collapse: collapse;
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
                    border-radius: 8px;
                }

                .calendar-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 20px;
                }

                .calendar-header h4 {
                    margin: 0;
                    color: #2c3e50;
                }

                .calendar-grid {
                    display: grid;
                    grid-template-columns: repeat(7, 1fr);
                    gap: 8px;
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

                /* Custom Multi-Select Styles */
                .multiselect-container {
                    position: relative;
                    width: 100%;
                }

                .select-box {
                    position: relative;
                    display: flex;
                    align-items: stretch;
                    width: 100%;
                    padding: 0;
                    border: 1px solid #ddd;
                    border-radius: 6px;
                    cursor: pointer;
                    background: #fff;
                    min-height: 42px;
                    overflow: hidden;
                }

                .select-box .text {
                    flex-grow: 1;
                    padding: 10px;
                    display: flex;
                    align-items: center;
                    color: #555;
                }

                .select-box .arrow-box {
                    width: 40px;
                    min-width: 40px;
                    background: transparent;
                    border-left: 1px solid transparent;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    transition: all 0.2s;
                }

                .select-box:hover .arrow-box {
                    background: #e9ecef;
                    border-left: 1px solid #ddd;
                }

                .select-box .arrow-box:after {
                    content: '';
                    display: inline-block;
                    width: 6px;
                    height: 6px;
                    border: solid #555;
                    border-width: 0 2px 2px 0;
                    transform: rotate(45deg);
                    margin-top: -3px;
                    /* visual alignment */
                }



                .checkboxes {
                    display: none;
                    position: absolute;
                    top: 100%;
                    left: 0;
                    right: 0;
                    border: 1px solid #ddd;
                    background: #fff;
                    z-index: 1000;
                    max-height: 200px;
                    overflow-y: auto;
                    border-radius: 0 0 6px 6px;
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                }

                .checkboxes label {
                    display: block;
                    padding: 8px 12px;
                    cursor: pointer;
                    border-bottom: 1px solid #f0f0f0;
                    transition: background 0.2s;
                }

                .checkboxes label:hover {
                    background: #f8f9fa;
                }

                .checkboxes.active {
                    display: block;
                }

                /* Refined Styles for Highlight Mode */
                .checkboxes label input[type="checkbox"] {
                    display: none !important;
                }

                .checkboxes label {
                    display: flex !important;
                    justify-content: space-between;
                    align-items: center;
                }

                .checkboxes label.selected {
                    background: #fdf8f3;
                    color: #c6a87c;
                    font-weight: 600;
                }

                .checkboxes label.selected:after {
                    content: '';
                    display: inline-block;
                    width: 6px;
                    height: 12px;
                    border: solid #c6a87c;
                    border-width: 0 2px 2px 0;
                    transform: rotate(45deg);
                    margin-right: 5px;
                }

                .checkboxes::-webkit-scrollbar {
                    width: 6px;
                }

                .checkboxes::-webkit-scrollbar-thumb {
                    background: #ccc;
                    border-radius: 3px;
                }
            </style>
            <div id="app">
                <!-- Sync Session User to LocalStorage for script.js compatibility -->
                <script>
                    const sessionUser = {
                        firstName: "<%= user.getFirstName() %>",
                        lastName: "<%= user.getLastName() %>",
                        email: "<%= user.getEmail() %>",
                        role: "<%= user.getRoleName() %>", // Ensure this matches 'ADMIN' or 'DESIGNER'
                        id: <%= user.getId() %>
            };
                    localStorage.setItem('dott_current_user', JSON.stringify(sessionUser));

                    // Also set legacy flag if needed
                    if (sessionUser.role === 'ADMIN') {
                        sessionStorage.setItem('admin_logged_in', 'true');
                    }
                </script>
                <div id="admin-dashboard-page"></div>

                <section id="admin-dashboard" class="page active">
                    <div class="dashboard">
                        <!-- Sidebar Navigation -->
                        <aside class="sidebar">
                            <div class="sidebar-header">
                                <div class="sidebar-logo">DotsStudio</div>
                                <div class="sidebar-subtitle">
                                    <%= isAdmin ? "Admin Panel" : "Designer Panel" %>
                                </div>
                            </div>

                            <nav class="sidebar-nav">
                                <style>
                                    .sidebar-nav {
                                        display: flex;
                                        flex-direction: column;
                                        width: 100%;
                                    }

                                    .sidebar-nav .nav-item {
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

                                    .sidebar-nav .nav-item .nav-icon {
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

                                    .sidebar-nav .nav-item .nav-icon svg {
                                        width: 20px !important;
                                        height: 20px !important;
                                        display: block !important;
                                    }

                                    .sidebar-nav .nav-item .nav-label {
                                        font-weight: 500 !important;
                                        font-size: 0.95rem !important;
                                        letter-spacing: 0.5px !important;
                                        white-space: nowrap !important;
                                        margin: 0 !important;
                                        padding: 0 !important;
                                    }

                                    .sidebar-nav .nav-item:hover {
                                        background-color: rgba(255, 255, 255, 0.1) !important;
                                        border-left-color: #888888 !important;
                                    }

                                    .sidebar-nav .nav-item.active {
                                        background-color: rgba(255, 255, 255, 0.15) !important;
                                        border-left-color: #888888 !important;
                                    }
                                </style>
                                <a href="#" id="nav-overview" class="nav-item active">
                                    <span class="nav-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                                            stroke="currentColor" stroke-width="2">
                                            <rect x="3" y="3" width="7" height="7" />
                                            <rect x="14" y="3" width="7" height="7" />
                                            <rect x="14" y="14" width="7" height="7" />
                                            <rect x="3" y="14" width="7" height="7" />
                                        </svg></span>
                                    <span class="nav-label">Overview</span>
                                </a>
                                <a href="#" id="nav-bookings" class="nav-item">
                                    <span class="nav-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                                            stroke="currentColor" stroke-width="2">
                                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                                            <polyline points="14 2 14 8 20 8" />
                                            <line x1="16" y1="13" x2="8" y2="13" />
                                            <line x1="16" y1="17" x2="8" y2="17" />
                                            <line x1="10" y1="9" x2="8" y2="9" />
                                        </svg></span>
                                    <span class="nav-label">Bookings</span>
                                </a>
                                <% if (isAdmin) { %>
                                    <a href="#" id="nav-designers" class="nav-item">
                                        <span class="nav-icon"><svg width="20" height="20" viewBox="0 0 24 24"
                                                fill="none" stroke="currentColor" stroke-width="2">
                                                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                                                <circle cx="9" cy="7" r="4" />
                                                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                                                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                                            </svg></span>
                                        <span class="nav-label">Staff</span>
                                    </a>
                                    <a href="#" id="nav-designer-mgmt" class="nav-item">
                                        <span class="nav-icon"><svg width="20" height="20" viewBox="0 0 24 24"
                                                fill="none" stroke="currentColor" stroke-width="2">
                                                <path
                                                    d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                                            </svg></span>
                                        <span class="nav-label">Designer</span>
                                    </a>
                                    <% } %>
                            </nav>

                            <div class="sidebar-footer">
                                <a href="index.jsp" class="nav-item">
                                    <span class="nav-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                                            stroke="currentColor" stroke-width="2">
                                            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                                            <polyline points="9 22 9 12 15 12 15 22" />
                                        </svg></span>
                                    <span class="nav-label">Home</span>
                                </a>
                                <a href="#" id="nav-logout" class="nav-item logout">
                                    <span class="nav-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                                            stroke="currentColor" stroke-width="2">
                                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                                            <polyline points="16 17 21 12 16 7" />
                                            <line x1="21" y1="12" x2="9" y2="12" />
                                        </svg></span>
                                    <span class="nav-label">Logout</span>
                                </a>
                            </div>
                        </aside>

                        <!-- Main Content Area -->
                        <main class="main-content">

                            <!-- Dashboard Overview Section -->
                            <div id="overview" class="dashboard-section active">
                                <div class="section-header">
                                    <h2>Dashboard Overview</h2>
                                    <p class="section-subtitle">Real-time statistics and insights</p>
                                </div>

                                <!-- Inline CSS for Interactive Cards -->
                                <style>
                                    .stat-card {
                                        cursor: pointer;
                                        transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
                                    }

                                    .stat-card:hover {
                                        transform: translateY(-2px);
                                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                                    }

                                    .stat-card.active {
                                        border: 2px solid var(--accent-color, #c6a87c);
                                        background-color: var(--secondary-color, #f9f9f9);
                                    }

                                    /* Modal & Tabs CSS */
                                    .large-modal {
                                        max-width: 600px;
                                        width: 90%;
                                    }

                                    .modal-tabs {
                                        display: flex;
                                        border-bottom: 1px solid #ddd;
                                        margin-bottom: 20px;
                                    }

                                    .tab-btn {
                                        padding: 10px 20px;
                                        border: none;
                                        background: none;
                                        cursor: pointer;
                                        font-weight: 600;
                                        color: #666;
                                        outline: none;
                                        border-bottom: 3px solid transparent;
                                    }

                                    .tab-btn.active {
                                        color: #c6a87c;
                                        border-bottom-color: #c6a87c;
                                    }

                                    .tab-pane {
                                        display: none;
                                    }

                                    .tab-pane.active {
                                        display: block;
                                        animation: fadeIn 0.3s;
                                    }

                                    /* Form Grid */
                                    .form-row {
                                        display: flex;
                                        gap: 15px;
                                    }

                                    .form-group.half {
                                        flex: 1;
                                    }

                                    .dashed-box {
                                        border: 1px dashed #ccc;
                                        padding: 15px;
                                        border-radius: 8px;
                                        margin-bottom: 15px;
                                        background: #fafafa;
                                    }

                                    .box-title {
                                        font-size: 0.85em;
                                        font-weight: bold;
                                        color: #888;
                                        margin-top: 0;
                                        margin-bottom: 10px;
                                        text-transform: uppercase;
                                    }

                                    .checkbox-group {
                                        display: flex;
                                        flex-wrap: wrap;
                                        gap: 10px;
                                    }

                                    .checkbox-group label {
                                        display: flex;
                                        align-items: center;
                                        gap: 5px;
                                        font-size: 0.9em;
                                        cursor: pointer;
                                    }

                                    @keyframes fadeIn {
                                        from {
                                            opacity: 0;
                                        }

                                        to {
                                            opacity: 1;
                                        }
                                    }

                                    /* ==================== Designer Management Styles ==================== */
                                    #designer-management {
                                        animation: fadeIn 0.3s ease-in;
                                    }

                                    .section-header {
                                        margin-bottom: 30px;
                                    }

                                    .section-header h2 {
                                        font-size: 1.5em;
                                        font-weight: 700;
                                        color: #2c3e50;
                                        letter-spacing: 0.5px;
                                        margin-bottom: 0;
                                    }

                                    .designer-mgmt-controls {
                                        display: flex;
                                        gap: 12px;
                                        align-items: center;
                                        flex-wrap: wrap;
                                    }

                                    .filter-dropdown {
                                        padding: 10px 16px;
                                        border: 2px solid #e0e0e0;
                                        border-radius: 8px;
                                        background: white;
                                        font-size: 0.95em;
                                        font-weight: 500;
                                        color: #444;
                                        cursor: pointer;
                                        transition: all 0.2s ease;
                                        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
                                    }

                                    .filter-dropdown:hover {
                                        border-color: #0d47a1;
                                        box-shadow: 0 4px 8px rgba(90, 159, 212, 0.2);
                                    }

                                    .filter-btn {
                                        padding: 10px 20px;
                                        background: #f8f9fa;
                                        border: 2px solid #e0e0e0;
                                        border-radius: 8px;
                                        font-weight: 500;
                                        font-size: 0.9em;
                                        color: #555;
                                        cursor: pointer;
                                        transition: all 0.3s ease;
                                        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
                                    }

                                    .filter-btn:hover {
                                        background: #fff;
                                        border-color: #0d47a1;
                                        transform: translateY(-2px);
                                        box-shadow: 0 4px 12px rgba(13, 71, 161, 0.25);
                                    }

                                    .filter-btn.active {
                                        background: linear-gradient(135deg, #0d47a1 0%, #0a3d91 100%);
                                        color: white;
                                        border-color: #0d47a1;
                                        box-shadow: 0 44px 12px rgba(13, 71, 161, 0.35);
                                    }

                                    .btn.primary {
                                        background: linear-gradient(135deg, #0d47a1 0%, #0a3d91 100%);
                                        color: white;
                                        border: none;
                                        padding: 11px 24px;
                                        border-radius: 8px;
                                        font-weight: 600;
                                        font-size: 0.95em;
                                        cursor: pointer;
                                        transition: all 0.3s ease;
                                        box-shadow: 0 4px 12px rgba(13, 71, 161, 0.35);
                                    }

                                    .btn.primary:hover {
                                        transform: translateY(-2px);
                                        box-shadow: 0 6px 20px rgba(13, 71, 161, 0.4);
                                    }

                                    /* Type Badges - Modern Pills */
                                    .type-badge {
                                        display: inline-flex;
                                        align-items: center;
                                        gap: 6px;
                                        padding: 6px 14px;
                                        border-radius: 20px;
                                        font-size: 0.85em;
                                        font-weight: 700;
                                        text-transform: uppercase;
                                        letter-spacing: 0.5px;
                                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                                    }

                                    .type-badge.full {
                                        background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
                                        color: #1565c0;
                                        border: 1px solid #90caf9;
                                    }

                                    .type-badge.part {
                                        background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
                                        color: #6a1b9a;
                                        border: 1px solid #ce93d8;
                                    }



                                    /* Table Enhancements */
                                    .designer-mgmt-table {
                                        background: #e8eaed;
                                        border-radius: 12px;
                                        overflow: hidden;
                                        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
                                    }

                                    .designer-mgmt-table thead {
                                        background: linear-gradient(135deg, #d1d5db 0%, #c4c9cf 100%);
                                    }

                                    .designer-mgmt-table thead th {
                                        font-weight: 700;
                                        text-transform: uppercase;
                                        letter-spacing: 0.8px;
                                        font-size: 0.8em;
                                        color: #555;
                                        padding: 16px 20px;
                                        border-bottom: 3px solid #9ca3af;
                                        text-align: left;
                                        /* Enforce left alignment */
                                    }

                                    .designer-mgmt-table tbody tr {
                                        border-bottom: 1px solid #d1d5db;
                                        transition: all 0.2s ease;
                                        background: #f5f7fa;
                                    }

                                    .designer-mgmt-table tbody tr:hover {
                                        background: #e8eaed;
                                        transform: scale(1.005);
                                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
                                    }

                                    .designer-mgmt-table tbody td {
                                        padding: 18px 20px;
                                        vertical-align: middle;
                                    }

                                    .designer-mgmt-table tbody td strong {
                                        font-size: 1.05em;
                                        color: #2c3e50;
                                        font-weight: 600;
                                    }

                                    .designer-mgmt-table tbody td small {
                                        color: #7f8c8d;
                                        font-size: 0.85em;
                                    }

                                    /* Action Button */
                                    .designer-mgmt-table .btn.edit {
                                        background: linear-gradient(135deg, #0d47a1 0%, #0a3d91 100%);
                                        color: white;
                                        border: none;
                                        padding: 8px 16px;
                                        border-radius: 8px;
                                        font-size: 1.1em;
                                        cursor: pointer;
                                        transition: all 0.3s ease;
                                        box-shadow: 0 2px 8px rgba(13, 71, 161, 0.25);
                                    }

                                    .designer-mgmt-table .btn.edit:hover {
                                        background: linear-gradient(135deg, #4a8bc2 0%, #3a7ab2 100%);
                                        transform: translateY(-2px) rotate(90deg);
                                        box-shadow: 0 4px 16px rgba(13, 71, 161, 0.4);
                                    }

                                    /* Hint Text */
                                    .hint-text {
                                        text-align: center;
                                        color: #95a5a6;
                                        font-size: 0.9em;
                                        margin-top: 20px;
                                        font-style: italic;
                                    }

                                    /* Radio Button Group */
                                    .radio-group {
                                        display: flex;
                                        gap: 20px;
                                        padding: 10px 0;
                                    }

                                    .radio-group label {
                                        display: flex;
                                        align-items: center;
                                        gap: 8px;
                                        font-weight: 500;
                                        cursor: pointer;
                                        padding: 8px 16px;
                                        border-radius: 8px;
                                        transition: all 0.2s ease;
                                    }

                                    .radio-group label:hover {
                                        background: #f8f9fa;
                                    }

                                    .radio-group input[type="radio"] {
                                        width: 18px;
                                        height: 18px;
                                        cursor: pointer;
                                        accent-color: #0d47a1;
                                    }
                                </style>

                                <div class="stats">
                                    <div class="stat-card active" id="card-total" onclick="filterBookings('all')">
                                        <div class="stat-icon"><svg width="24" height="24" viewBox="0 0 24 24"
                                                fill="none" stroke="currentColor" stroke-width="2">
                                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                                                <polyline points="14 2 14 8 20 8" />
                                                <line x1="16" y1="13" x2="8" y2="13" />
                                                <line x1="16" y1="17" x2="8" y2="17" />
                                                <line x1="10" y1="9" x2="8" y2="9" />
                                            </svg></div>
                                        <div class="stat-info">
                                            <h3>Total Bookings</h3>
                                            <p id="total-bookings" class="stat-value">0</p>
                                        </div>

                                    </div>

                                    <div class="stat-card" id="card-upcoming" onclick="filterBookings('upcoming')">
                                        <div class="stat-icon"><svg width="24" height="24" viewBox="0 0 24 24"
                                                fill="none" stroke="currentColor" stroke-width="2">
                                                <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                                                <line x1="16" y1="2" x2="16" y2="6" />
                                                <line x1="8" y1="2" x2="8" y2="6" />
                                                <line x1="3" y1="10" x2="21" y2="10" />
                                            </svg></div>
                                        <div class="stat-info">
                                            <h3>Upcoming</h3>
                                            <p id="upcoming-appointments" class="stat-value">0</p>
                                        </div>

                                    </div>

                                    <div class="stat-card" id="card-today" onclick="filterBookings('today')">
                                        <div class="stat-icon"><svg width="24" height="24" viewBox="0 0 24 24"
                                                fill="none" stroke="currentColor" stroke-width="2">
                                                <circle cx="12" cy="12" r="10" />
                                                <polyline points="12 6 12 12 16 14" />
                                            </svg></div>
                                        <div class="stat-info">
                                            <h3>Today</h3>
                                            <p id="today-consultations" class="stat-value">0</p>
                                        </div>

                                    </div>
                                </div>
                            </div>

                            <!-- Manage Bookings Section -->
                            <div id="manage-bookings" class="dashboard-section">
                                <div class="section-header">
                                    <div>
                                        <h2>MANAGE BOOKINGS</h2>
                                        <p class="section-subtitle">View and update booking statuses</p>
                                    </div>
                                </div>
                                <div class="table-wrapper">
                                    <table class="modern-table designer-mgmt-table">
                                        <thead>
                                            <tr>
                                                <th style="text-align: center;">ID</th>
                                                <th style="text-align: center;">Client</th>
                                                <th style="text-align: center;">Contact</th>
                                                <th style="text-align: center;">Designer</th>
                                                <th style="text-align: center;">Date</th>
                                                <th style="text-align: center;">Time</th>
                                                <th style="text-align: center; min-width: 200px; max-width: 300px;">
                                                    Notes</th>
                                                <th style="text-align: center;">Status</th>
                                                <th style="text-align: center;">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody id="bookings-tbody">
                                            <!-- Bookings populated here -->
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            <!-- Manage Staff Section -->
                            <div id="manage-designers" class="dashboard-section">
                                <div class="section-header">
                                    <div>
                                        <h2>MANAGE STAFF</h2>
                                        <p class="section-subtitle">Manage team members and permissions</p>
                                    </div>
                                    <button id="add-designer-btn" class="btn primary">+ Add Staff</button>
                                </div>
                                <div class="table-wrapper">
                                    <table class="modern-table designer-mgmt-table">
                                        <thead>
                                            <tr>
                                                <th style="text-align: center;">Name</th>
                                                <th style="text-align: center;">Role</th>
                                                <th style="text-align: center;">Specialty</th>
                                                <th style="text-align: center;">Joined</th>
                                                <th style="text-align: center;">Contact / Status</th>
                                                <th style="text-align: center;">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody id="designers-tbody">
                                            <!-- Staff populated here -->
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            <!-- Designer Management Section (NEW) -->
                            <div id="designer-management" class="dashboard-section">
                                <style>
                                    /* Force alignment for designer management table */
                                    .designer-mgmt-table th,
                                    .designer-mgmt-table td {
                                        text-align: center !important;
                                        vertical-align: middle !important;
                                    }

                                    .designer-mgmt-table td div:not(.capacity-track):not(.capacity-fill) {
                                        margin: 0 auto !important;
                                    }

                                    .designer-mgmt-table td strong {
                                        display: block;
                                        text-align: center;
                                    }

                                    /* FIX: Ensure capacity bar fills from left */
                                    .designer-mgmt-table td div.capacity-track {
                                        justify-content: flex-start !important;
                                        align-items: stretch !important;
                                        display: flex !important;
                                        margin-top: 5px !important;
                                    }

                                    .designer-mgmt-table td div.capacity-fill {
                                        margin: 0 !important;
                                        flex-grow: 0 !important;
                                    }
                                </style>
                                <div class="section-header"
                                    style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 3px solid #f0f0f0;">
                                    <div>
                                        <h2
                                            style="margin: 0; font-size: 1.8em; font-weight: 700; color: #2c3e50; letter-spacing: 0.5px;">
                                            DESIGNER MANAGEMENT
                                        </h2>
                                        <p style="margin: 5px 0 0 0; color: #7f8c8d; font-size: 0.95em;">Manage your
                                            design team's schedules and capacity</p>
                                    </div>
                                    <div class="designer-mgmt-controls">
                                        <!-- Add Button Access moved to Staff Tab -->
                                    </div>
                                </div>
                                <div class="table-wrapper">
                                    <table class="modern-table designer-mgmt-table">
                                        <thead>
                                            <tr>
                                                <th style="text-align: center;">Design Team Member</th>
                                                <th style="text-align: center;">Bio</th>
                                                <th style="text-align: center;">Specialty</th>
                                                <th style="text-align: center;">Load</th>
                                                <th style="text-align: center;">Completed</th>
                                                <th style="text-align: center;">Availability/Status</th>
                                                <th style="text-align: center;">Capacity</th>
                                            </tr>
                                        </thead>
                                        <tbody id="designer-mgmt-tbody">
                                            <!-- Server-Side Synced Data -->
                                            <% try { java.util.List<com.dottstudio.model.Designer> designerList =
                                                com.dottstudio.util.UserDAO.getAllDesigners();
                                                if (designerList == null || designerList.isEmpty()) {
                                                %>
                                                <tr>
                                                    <td colspan="7" style="text-align:center;">No designers found.</td>
                                                </tr>
                                                <% } else { for (com.dottstudio.model.Designer d : designerList) {
                                                    String statusBadgeClass="full" ; String statusText="Active" ; if
                                                    (d.getStatus() !=null) { statusText=d.getStatus(); } if
                                                    ("Active".equalsIgnoreCase(statusText)) { statusBadgeClass="full" ;
                                                    } else { statusBadgeClass="part" ; } %>
                                                    <tr>
                                                        <td
                                                            style="text-align: center !important; vertical-align: middle;">
                                                            <div
                                                                style="display:flex; flex-direction:column; align-items: center;">
                                                                <strong style="font-size:1em; color:#333;">
                                                                    <%= d.getFirstName() %>
                                                                        <%= d.getLastName() %>
                                                                </strong>
                                                                <small style="color:#888;">
                                                                    <%= d.getEmail() %>
                                                                </small>
                                                            </div>
                                                        </td>
                                                        <td
                                                            style="text-align: center !important; vertical-align: middle;">
                                                            <div style="max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin: 0 auto;"
                                                                title="<%= d.getBio() != null ? d.getBio() : "" %>">
                                                                <%= d.getBio() !=null && !d.getBio().isEmpty() ?
                                                                    d.getBio() : "-" %>
                                                            </div>
                                                        </td>
                                                        <td
                                                            style="text-align: center !important; vertical-align: middle;">
                                                            <%= d.getSpecialty() !=null ? d.getSpecialty() : "General"
                                                                %>
                                                        </td>
                                                        <td
                                                            style="vertical-align: middle !important; padding: 10px !important;">
                                                            <% int current=d.getCurrentProjects(); int
                                                                max=d.getMaxSimultaneousProjects(); int percent=(max> 0)
                                                                ? (int)((current * 100.0) / max) : 0;
                                                                String barColor = (percent >= 100) ? "#dc3545" :
                                                                ((percent >= 75) ? "#ffc107" : "#28a745");
                                                                %>
                                                                <!-- Table based progress widget -->
                                                                <table
                                                                    style="width: 100px; margin: 0 auto !important; border-collapse: collapse; border: none !important;">
                                                                    <tr>
                                                                        <td
                                                                            style="text-align: center !important; border: none !important; padding: 0 0 4px 0 !important; font-weight: bold; color: <%= barColor %>;">
                                                                            <%= current %> / <%= max %>
                                                                        </td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td
                                                                            style="width: 100%; background-color: #e9ecef !important; height: 6px !important; border-radius: 3px !important; border: none !important; padding: 0 !important; text-align: left !important; vertical-align: top !important;">
                                                                            <span
                                                                                style="display: block !important; width: <%= percent %>%; background-color: <%= barColor %>; height: 6px !important; border-radius: 3px !important; margin: 0 !important;"></span>
                                                                        </td>
                                                                    </tr>
                                                                </table>

                                                        </td>
                                                        <td
                                                            style="text-align: center !important; vertical-align: middle;">
                                                            <strong>
                                                                <%= d.getCompletedProjects() %>
                                                            </strong>
                                                        </td>
                                                        <td
                                                            style="text-align: center !important; vertical-align: middle;">
                                                            <span class="type-badge <%= statusBadgeClass %>"><i
                                                                    class="fas fa-circle"
                                                                    style="font-size:0.5em; margin-right:4px;"></i>
                                                                <%= statusText %>
                                                            </span>
                                                        </td>
                                                        <td
                                                            style="text-align: center !important; vertical-align: middle;">
                                                            <small>Max Hrs: <%= d.getMaxHoursPerWeek() %></small><br>
                                                            <small>Max Bks: <%= d.getMaxBookingsPerWeek() %></small>
                                                        </td>
                                                    </tr>
                                                    <% } } } catch (Exception e) { e.printStackTrace(); %>
                                                        <tr>
                                                            <td colspan="7" style="text-align:center; color:red;">Error
                                                                loading
                                                                data.</td>
                                                        </tr>
                                                        <% } %>
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                        </main>
                    </div>
                </section>

                <!-- Designer Management Modal (NEW) -->
                <div id="designer-mgmt-modal" class="modal">
                    <div class="modal-content large-modal">
                        <div class="modal-header">
                            <h3 id="designer-mgmt-modal-title">Edit Designer</h3>
                            <span class="close-designer-modal">&times;</span>
                        </div>
                        <div class="modal-body">
                            <!-- 2 Tabs -->
                            <div class="modal-tabs">
                                <button class="tab-btn active" onclick="switchDesignerTab('basic')">Basic
                                    Info</button>
                                <button class="tab-btn" onclick="switchDesignerTab('schedule')">Schedule</button>
                            </div>

                            <form id="designer-mgmt-form">
                                <!-- Tab 1: Basic Info -->
                                <div id="designer-tab-basic" class="tab-pane active">
                                    <div class="form-row">
                                        <div class="form-group half">
                                            <label for="designer-email">Email</label>
                                            <input type="email" id="designer-email" required readonly>
                                        </div>
                                        <div class="form-group half">
                                            <label for="designer-name">Full Name</label>
                                            <input type="text" id="designer-name" required>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="designer-specialty">Specialty</label>

                                        <select id="designer-specialty" class="form-control" required
                                            style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                                            <option value="">Select Specialty...</option>
                                            <option value="Modern">Modern</option>
                                            <option value="Minimalist">Minimalist</option>
                                            <option value="Bohemian">Bohemian</option>
                                            <option value="Industrial">Industrial</option>
                                            <option value="Scandinavian">Scandinavian</option>
                                            <option value="Traditional">Traditional</option>
                                            <option value="Eclectic">Eclectic</option>
                                        </select>
                                    </div>
                                    <div class="form-group">
                                        <label for="designer-bio">Bio</label>
                                        <textarea id="designer-bio" rows="3"
                                            placeholder="Short description..."></textarea>
                                    </div>
                                </div>

                                <!-- Tab 2: Schedule -->
                                <div id="designer-tab-schedule" class="tab-pane">
                                    <div class="calendar-container">
                                        <div class="calendar-header">
                                            <button type="button" class="btn secondary"
                                                onclick="changeMonth(-1)">&lt;</button>
                                            <h4 id="calendar-month-year">January 2026</h4>
                                            <button type="button" class="btn secondary"
                                                onclick="changeMonth(1)">&gt;</button>
                                        </div>
                                        <div class="calendar-grid" id="schedule-calendar">
                                            <!-- Dynmically Populated -->
                                        </div>
                                        <div class="calendar-legend"
                                            style="margin-top: 10px; display: flex; gap: 15px; font-size: 0.9em;">
                                            <span style="display: flex; align-items: center; gap: 5px;">
                                                <div
                                                    style="width: 15px; height: 15px; background: #e0e0e0; border-radius: 3px;">
                                                </div> Unavailable
                                            </span>
                                            <span style="display: flex; align-items: center; gap: 5px;">
                                                <div
                                                    style="width: 15px; height: 15px; background: #c6a87c; border-radius: 3px;">
                                                </div> Available
                                            </span>
                                        </div>
                                        <p class="hint-text">Click dates to toggle availability.</p>
                                    </div>
                                </div>



                                <div class="form-actions"
                                    style="margin-top: 20px; border-top: 1px solid #eee; padding-top: 15px;">
                                    <button type="button" class="btn secondary"
                                        onclick="document.getElementById('designer-mgmt-modal').style.display='none'">Cancel</button>
                                    <button type="submit" class="btn primary">Save Changes</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <!-- End Designer Management Modal -->

                <!-- Edit/Promote User Modal -->
                <div id="promote-modal" class="modal">
                    <div class="modal-content large-modal">
                        <div class="modal-header">
                            <h3>Manage Staff</h3>
                            <span class="close-modal">&times;</span>
                        </div>
                        <div class="modal-body">
                            <!-- Tabs -->
                            <!-- Tabs (Hidden by default, shown via JS for Edit Mode) -->
                            <div class="modal-tabs" style="display:none;">
                                <button class="tab-btn active" onclick="switchTab('basic')">Basic Info</button>
                                <button class="tab-btn" onclick="switchTab('schedule')">Schedule</button>
                                <button class="tab-btn" onclick="switchTab('capacity')">Capacity & Limits</button>
                            </div>

                            <!-- Section Header (shown when tabs are hidden for ADMIN) -->
                            <div id="section-header-admin"
                                style="display: none; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 2px solid #e0e0e0;">
                                <h4 style="margin: 0; font-size: 1.1em; color: #2c3e50; font-weight: 600;">BASIC INFO
                                </h4>
                            </div>

                            <form id="promote-form">
                                <!-- Tab: Basic Info -->
                                <div id="tab-basic" class="tab-pane active">
                                    <input type="hidden" id="promote-staff-id" name="id">
                                    <!-- ... content ... -->

                                    <div class="form-group">
                                        <label for="promote-search">Search User (Email to Promote)</label>
                                        <input type="text" id="promote-search" placeholder="Type email..."
                                            autocomplete="off">
                                        <ul id="search-results" class="autocomplete-results"></ul>
                                        <small style="color:#666; font-size: 0.8em;">Leave empty if editing existing
                                            staff.</small>
                                    </div>

                                    <div class="form-row">
                                        <div class="form-group half">
                                            <label for="promote-email">Email</label>
                                            <input type="email" id="promote-email" required>
                                        </div>
                                        <div class="form-group half">
                                            <label for="promote-phone">Phone</label>
                                            <input type="text" id="promote-phone" placeholder="e.g. +123456789">
                                        </div>
                                    </div>
                                    <div class="form-row">
                                        <div class="form-group half">
                                            <label for="promote-role">Role</label>
                                            <select id="promote-role" required onchange="toggleDesignerFields()">
                                                <option value="DESIGNER">Designer</option>
                                                <option value="ADMIN">Admin</option>
                                            </select>
                                        </div>
                                        <div class="form-group half">
                                            <label for="promote-status">Status</label>
                                            <select id="promote-status">
                                                <option value="Active">Active</option>
                                                <option value="Inactive">Inactive</option>
                                                <option value="On Leave">On Leave</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div id="new-user-fields" class="dashed-box">
                                        <p class="box-title">New User Details (Required if creating new)</p>
                                        <div class="form-row">
                                            <div class="form-group half">
                                                <label for="promote-firstname">First Name</label>
                                                <input type="text" id="promote-firstname" placeholder="John">
                                            </div>
                                            <div class="form-group half">
                                                <label for="promote-lastname">Last Name</label>
                                                <input type="text" id="promote-lastname" placeholder="Doe">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="promote-password">Password</label>
                                            <input type="password" id="promote-password" placeholder="********">
                                        </div>
                                    </div>

                                    <div id="designer-basic-fields">
                                        <div class="form-group">
                                            <label>Specialty</label>
                                            <div class="multiselect-container" id="specialty-multiselect">
                                                <div class="select-box" onclick="toggleAppSpecialties(event)">
                                                    <span class="text">Select Specialty...</span>
                                                    <span class="arrow-box"></span>
                                                </div>
                                                <div class="checkboxes" id="specialty-options">
                                                    <label><input type="checkbox" value="Modern"
                                                            onchange="updateAppSpecialtyDisplay()"> Modern</label>
                                                    <label><input type="checkbox" value="Minimalist"
                                                            onchange="updateAppSpecialtyDisplay()"> Minimalist</label>
                                                    <label><input type="checkbox" value="Bohemian"
                                                            onchange="updateAppSpecialtyDisplay()"> Bohemian</label>
                                                    <label><input type="checkbox" value="Industrial"
                                                            onchange="updateAppSpecialtyDisplay()"> Industrial</label>
                                                    <label><input type="checkbox" value="Scandinavian"
                                                            onchange="updateAppSpecialtyDisplay()"> Scandinavian</label>
                                                    <label><input type="checkbox" value="Traditional"
                                                            onchange="updateAppSpecialtyDisplay()"> Traditional</label>
                                                    <label><input type="checkbox" value="Eclectic"
                                                            onchange="updateAppSpecialtyDisplay()"> Eclectic</label>
                                                </div>
                                            </div>
                                            <input type="hidden" id="promote-specialty" name="specialty">
                                        </div>
                                        <div class="form-group">
                                            <label for="promote-bio">Bio (Short)</label>
                                            <textarea id="promote-bio" rows="2"
                                                placeholder="Short description..."></textarea>
                                        </div>
                                    </div>
                                </div>

                        </div>

                        <!-- Tab: Schedule -->
                        <div id="tab-schedule" class="tab-pane">
                            <div class="calendar-container"
                                style="width: 100%; max-width: 100%; border: none; padding: 0; box-shadow: none;">
                                <div class="calendar-header" style="margin-bottom: 15px;">
                                    <div style="display: flex; align-items: center; gap: 15px;">
                                        <button type="button" onclick="changeMonthPromote(-1)"
                                            style="background: none; border: 1px solid #ddd; width: 32px; height: 32px; border-radius: 50%; cursor: pointer;">&lt;</button>
                                        <h3 id="calendar-month-year-promote"
                                            style="margin: 0; font-size: 1.1em; color: #333;">Month Year</h3>
                                        <button type="button" onclick="changeMonthPromote(1)"
                                            style="background: none; border: 1px solid #ddd; width: 32px; height: 32px; border-radius: 50%; cursor: pointer;">&gt;</button>
                                    </div>
                                    <div style="font-size: 0.8em; color: #666;">
                                        <span
                                            style="display: inline-block; width: 8px; height: 8px; background: #c6a87c; border-radius: 50%; margin-right: 4px;"></span>
                                        Available
                                    </div>
                                </div>
                                <div id="schedule-calendar-promote" class="calendar-grid">
                                    <!-- JS will populate this -->
                                </div>
                            </div>

                            <div class="form-row" style="margin-top: 20px;">
                                <div class="form-group half">
                                    <label for="work-start">Shift Start</label>
                                    <input type="time" id="work-start" value="09:00">
                                </div>
                                <div class="form-group half">
                                    <label for="work-end">Shift End</label>
                                    <input type="time" id="work-end" value="17:00">
                                </div>
                            </div>

                            <!-- Legacy/Specific Slots (kept for compatibility) -->
                            <div class="form-group"
                                style="margin-top:15px; border-top:1px dashed #ddd; padding-top:10px;">
                                <label for="promote-availability">Specific Slot Availability</label>
                                <input type="text" id="promote-availability"
                                    placeholder="e.g. 09:00, 14:00 (comma separated)">
                                <small style="color:#888">Overrides shift hours for specific slots.</small>
                            </div>
                        </div>

                        <!-- Tab: Capacity -->
                        <div id="tab-capacity" class="tab-pane">
                            <div class="form-row">
                                <div class="form-group half">
                                    <label for="max-projects">Max Simultaneous Projects</label>
                                    <input type="number" id="max-projects" value="5">
                                </div>
                                <div class="form-group half">
                                    <label for="max-bookings">Max Bookings / Week</label>
                                    <input type="number" id="max-bookings" value="20">
                                </div>
                            </div>
                            <div class="form-row">
                                <div class="form-group half">
                                    <label for="max-hours">Max Hours / Week</label>
                                    <input type="number" id="max-hours" value="40">
                                </div>
                                <div class="form-group half">
                                    <label for="min-hours">Min Guaranteed Hours</label>
                                    <input type="number" id="min-hours" value="0">
                                </div>
                            </div>
                        </div>


                        <div class="form-actions"
                            style="margin-top: 20px; border-top: 1px solid #eee; padding-top: 15px;">
                            <button type="button" class="btn secondary"
                                onclick="document.getElementById('promote-modal').style.display='none'">Cancel</button>
                            <button type="button" class="btn primary" onclick="window.submitPromoteForm()">Save
                                Changes</button>
                        </div>
                        </form>
                    </div>
                </div>
            </div>
            <!-- End Modal -->
            </div>

            <!-- Custom Global Modal -->
            <div id="custom-global-modal" class="custom-global-modal">
                <div class="custom-global-modal-content">
                    <div class="custom-global-modal-header">
                        <h3 id="custom-modal-title">Alert</h3>
                    </div>
                    <div class="custom-global-modal-body">
                        <p id="custom-modal-message">Message goes here.</p>
                    </div>
                    <div class="custom-global-modal-footer" id="custom-modal-footer">
                        <button class="custom-btn custom-btn-primary" id="custom-alert-ok"
                            onclick="window.closeCustomModal()">OK</button>
                    </div>
                </div>
            </div>


            <script src="script.js?v=<%= System.currentTimeMillis() %>_debug_v2"></script>
            <script src="admin-autocomplete.js?v=<%= System.currentTimeMillis() %>"></script>
        </body>

        </html>