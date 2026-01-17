package com.auth.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.auth.util.UserDAO;
import com.auth.model.User;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Setup CORS and JSON headers
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        
        PrintWriter out = response.getWriter();
        
        // Simple parameter extraction (In a real microservice, we'd parse JSON body)
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        System.out.println(">>> Auth Service: Login request for " + email);

        if (email == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Missing email or password\"}");
            return;
        }

        try {
            // Note: Password should be hashed before comparing if DB stores hash.
            // For now, mirroring existing logic where DAO checks against "PasswordHash" column.
            // Assuming the client calling this sends the raw password and we hash it here,
            // OR checks against plain text if that's what was inserted.
            // The default admin we inserted has 'admin123', so let's match exact string for now.
            // In production, use BCrypt. verify(password, hash).
            
            User user = UserDAO.login(email, password);

            if (user != null) {
                // Success
                String json = String.format(
                    "{\"success\": true, \"user\": {\"id\": %d, \"email\": \"%s\", \"role\": \"%s\", \"phone\": \"%s\", \"firstName\": \"%s\", \"lastName\": \"%s\"}}",
                    user.getId(), user.getEmail(), user.getRoleName(), (user.getPhone() != null ? user.getPhone() : ""), user.getFirstName(), user.getLastName()
                );
                out.print(json);
            } else {
                // Failed
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\": false, \"message\": \"Invalid credentials\"}");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Database error\"}");
        }
    }
}
