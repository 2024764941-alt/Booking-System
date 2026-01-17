<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Register - DotsStudio</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
    </head>

    <body>
        <div id="app">
            <section id="register" class="page active">
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
                    <h2>Register</h2>

                    <% String error=request.getParameter("error"); if (error !=null) { %>
                        <p style="color: red; text-align: center; margin-bottom: 1rem;">
                            <%= error %>
                        </p>
                        <% } %>

                            <form id="jsp-register-form" action="RegisterServlet" method="post">
                                <div class="form-group">
                                    <label for="first-name">First Name</label>
                                    <input type="text" id="first-name" name="firstName" required>
                                </div>
                                <div class="form-group">
                                    <label for="last-name">Last Name</label>
                                    <input type="text" id="last-name" name="lastName" required>
                                </div>
                                <div class="form-group">
                                    <label for="register-email">Email</label>
                                    <input type="email" id="register-email" name="email" placeholder="example@email.com"
                                        required>
                                </div>
                                <div class="form-group">
                                    <label for="phone">Phone Number</label>
                                    <input type="tel" id="phone" name="phone" required>
                                </div>
                                <div class="form-group">
                                    <label for="address">Address</label>
                                    <textarea id="address" name="address" rows="3" required></textarea>
                                </div>
                                <div class="form-group">
                                    <label for="register-password">Password</label>
                                    <input type="password" id="register-password" name="password" required>
                                </div>
                                <div class="form-group">
                                    <label for="confirm-password">Confirm Password</label>
                                    <input type="password" id="confirm-password" name="confirm-password" required>
                                </div>
                                <div class="form-group">
                                    <label for="referral">How did you find us?</label>
                                    <select id="referral" name="referral" required onchange="toggleFriendEmail()"
                                        style="width: 100%; padding: 0.85rem 1rem; border: 1px solid rgba(69, 70, 65, 0.15); border-radius: 10px; background: #FFFFFF; font-size: 1rem; color: #454641; transition: all 0.3s ease;">
                                        <option value="" disabled selected>Select an option</option>
                                        <option value="friends">Friends</option>
                                        <option value="social_media">Social Media</option>
                                        <option value="search_engine">Search Engine</option>
                                        <option value="advertisement">Advertisement</option>
                                        <option value="other">Other</option>
                                    </select>
                                </div>
                                <div class="form-group" id="friend-email-group" style="display: none;">
                                    <label for="friend-email">Friend's Email</label>
                                    <input type="email" id="friend-email" name="friendEmail"
                                        placeholder="Enter your friend's email">
                                </div>

                                <script>
                                    function toggleFriendEmail() {
                                        var referral = document.getElementById("referral").value;
                                        var friendGroup = document.getElementById("friend-email-group");
                                        var friendInput = document.getElementById("friend-email");

                                        if (referral === "friends") {
                                            friendGroup.style.display = "block";
                                            friendInput.setAttribute("required", "required");
                                        } else {
                                            friendGroup.style.display = "none";
                                            friendInput.removeAttribute("required");
                                            friendInput.value = "";
                                        }
                                    }
                                </script>
                                <button type="submit" class="btn"
                                    style="display: block; margin: 0 auto;">Register</button>
                            </form>
                            <p style="text-align: center; margin-top: 1rem;">Already have an account? <a
                                    href="login.jsp" id="to-login">Login here</a></p>
                </div>
            </section>
            </section>
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
                    <button class="custom-btn custom-btn-primary" onclick="window.closeCustomModal()">OK</button>
                </div>
            </div>
        </div>

        <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
    </body>

    </html>