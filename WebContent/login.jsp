<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Login - DotsStudio</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
    </head>

    <body>
        <div id="app">
            <section id="login" class="page active">
                <header>
                    <nav>
                        <div class="logo">DotsStudio</div>
                        <ul>
                            <li><a href="index.jsp">Home</a></li>
                            <li class="dropdown-container" id="nav-user-dropdown" style="display: none;">
                                <a href="#" id="user-dropdown-toggle">User Name</a>
                                <ul class="dropdown-menu">
                                    <li><a href="profile.html">My Bookings</a></li>
                                    <li><a href="profile.html">Profile Settings</a></li>
                                    <li class="divider"></li>
                                    <li><a href="#" id="nav-logout-dropdown">Logout</a></li>
                                </ul>
                            </li>
                        </ul>
                    </nav>
                </header>
                <div class="auth-container">
                    <h2>Login</h2>

                    <% String error=request.getParameter("error"); if (error !=null) { %>
                        <p style="color: red; text-align: center; margin-bottom: 1rem;">
                            <%= error %>
                        </p>
                        <% } String success=request.getParameter("success"); if (success !=null) { %>
                            <p style="color: green; text-align: center; margin-bottom: 1rem;">
                                <%= success %>
                            </p>
                            <% } %>

                                <form id="jsp-login-form" action="LoginServlet" method="post">
                                    <div class="form-group">
                                        <label for="login-email">Email</label>
                                        <input type="email" id="login-email" name="email" required>
                                    </div>
                                    <div class="form-group">
                                        <label for="login-password">Password</label>
                                        <input type="password" id="login-password" name="password" required>
                                    </div>
                                    <button type="submit" class="btn"
                                        style="display: block; margin: 0 auto;">Login</button>
                                </form>
                                <p style="text-align: center; margin-top: 1rem;">Don't have an account? <a
                                        href="register.jsp" id="to-register">Register here</a></p>
                </div>
            </section>
        </div>
        <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
    </body>

    </html>