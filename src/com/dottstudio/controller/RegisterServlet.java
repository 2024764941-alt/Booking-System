package com.dottstudio.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.dottstudio.util.PasswordUtils;
import com.dottstudio.util.UserDAO;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String password = request.getParameter("password");
        String referral = request.getParameter("referral");
        String friendEmail = request.getParameter("friendEmail");

        System.out.println(">>> DEBUG: RegisterServlet Hit for: " + email);
        System.out.println(">>> DEBUG: Referral: " + referral);
        System.out.println(">>> DEBUG: FriendEmail: " + friendEmail);

        if (email == null || password == null) {
            response.sendRedirect("register.jsp?error=Missing Fields");
            return;
        }

        String confirmPassword = request.getParameter("confirm-password");

        System.out.println(">>> DEBUG: Validating Passwords");
        System.out.println(">>> Password: " + (password != null ? "Len:" + password.length() : "NULL"));
        System.out.println(">>> Confirm:  " + (confirmPassword != null ? "Len:" + confirmPassword.length() : "NULL"));
        boolean match = false;
        if (password != null && confirmPassword != null) {
            match = password.trim().equals(confirmPassword.trim());
        }
        System.out.println(">>> Match (Trimmed): " + match);

        if (!match) {
            System.out.println(">>> DEBUG: Validation FAILED - Mismatch");
            response.sendRedirect("register.jsp?error="
                    + java.net.URLEncoder.encode("The passwords entered do not match. Please try again.", "UTF-8"));
            return;
        }

        try {
            // MICROSERVICE INTEGRATION: Call Auth Service
            String authServiceUrl = "http://localhost:8081/auth/api/register";
            
            String fullName = firstName + " " + lastName;
            String urlParameters = "email=" + java.net.URLEncoder.encode(email, "UTF-8") + 
                                   "&password=" + java.net.URLEncoder.encode(password, "UTF-8") +
                                   "&firstName=" + java.net.URLEncoder.encode(firstName, "UTF-8") +
                                   "&lastName=" + java.net.URLEncoder.encode(lastName, "UTF-8") +
                                   "&phone=" + java.net.URLEncoder.encode(phone, "UTF-8") +
                                   "&address=" + java.net.URLEncoder.encode(address, "UTF-8");

            java.net.URL url = new java.net.URL(authServiceUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(urlParameters.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println(">>> Auth Service Register Response: " + responseCode);

            if (responseCode == 200) {
                 response.sendRedirect("login.jsp?success=Registration Successful (Auth Service)");
            } else {
                 response.sendRedirect("register.jsp?error=Registration Failed (Email might exist)");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("register.jsp?error=System Error: " + e.getMessage());
        }
    }
}
