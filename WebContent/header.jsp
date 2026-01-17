<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <header>
        <nav>
            <div class="logo">DotsStudio</div>
            <ul>
                <li><a href="index.jsp">Home</a></li>
                <li><a href="index.jsp#philosophy">Philosophy</a></li>
                <li><a href="index.jsp#portfolio">Portfolio</a></li>
                <li><a href="index.jsp#process">Process</a></li>

                <!-- Auth Links -->
                <!-- Use JSTL logic or simple inline style checks if EL available -->
                <li>
                    <a href="login.jsp" id="nav-login" style="${not empty sessionScope.user ? 'display:none;' : ''}">
                        Login
                    </a>
                </li>

                <!-- User Dropdown: Visible if user logged in -->
                <li class="dropdown-container" id="nav-user-dropdown"
                    style="${not empty sessionScope.user ? '' : 'display:none;'}">
                    <a href="#" id="user-dropdown-toggle">
                        <!-- Display Name using EL -->
                        ${not empty sessionScope.user.firstName ? sessionScope.user.firstName : 'User'}
                    </a>
                    <ul class="dropdown-menu">
                        <li><a href="profile.jsp">My Bookings</a></li>
                        <li><a href="profile.jsp">Profile Settings</a></li>
                        <li class="divider"></li>
                        <li><a href="#" id="nav-logout-dropdown">Logout</a></li>
                    </ul>
                </li>

                <!-- Admin/Other Links -->
                <li><a href="admin-dashboard.jsp" id="nav-dashboard" style="display: none;">Dashboard</a></li>
                <li><a href="#" id="nav-logout-customer" style="display: none;">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Sync Server Session to LocalStorage using EL -->
    <%-- Only render script if user exists --%>


        <%-- Simple scriptlet just for the IF, but use EL inside to avoid imports --%>
            <% if (session.getAttribute("user") !=null) { %>
                <script>
                    console.log("Session detected (via JSP Header)");
                    const serverUser = {
                        firstName: "${sessionScope.user.firstName}",
                        lastName: "${sessionScope.user.lastName}",
                        email: "${sessionScope.user.email}",
                        phone: "${sessionScope.user.phone}",
                        address: "${sessionScope.user.address}",
                        role: "${sessionScope.user.role}",
                        password: "HIDDEN_FOR_SECURITY"
                    };

                    // Update LocalStorage to match Server Session
                    localStorage.setItem('dott_current_user', JSON.stringify(serverUser));

                    // Attempt to update UI immediately
                    if (typeof setupNavigation === 'function') {
                        setupNavigation();
                    } else {
                        window.addEventListener('load', function () {
                            if (typeof setupNavigation === 'function') setupNavigation();
                        });
                    }
                </script>
                <% } %>

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
                                <button class="custom-btn custom-btn-primary"
                                    onclick="window.closeCustomModal()">OK</button>
                            </div>
                        </div>
                    </div>