package com.dottstudio.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.dottstudio.model.User;
import com.dottstudio.util.UserDAO;
import com.dottstudio.util.PasswordUtils;

@WebServlet("/UpdateProfileServlet")
public class UpdateProfileServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            out.print("{\"success\": false, \"message\": \"User not logged in\"}");
            return;
        }

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String password = request.getParameter("password");

        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            out.print("{\"success\": false, \"message\": \"First name and Last name are required\"}");
            return;
        }

        // Update user object in memory
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setAddress(address);

        String hashedPassword = null;
        if (password != null && !password.trim().isEmpty()) {
            hashedPassword = PasswordUtils.hashPassword(password);
        }

        try {
            boolean updated = UserDAO.updateUser(user, hashedPassword);
            if (updated) {
                // Update session attribute
                session.setAttribute("user", user);
                out.print("{\"success\": true, \"message\": \"Profile updated successfully\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Failed to update profile in database\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"System error: " + e.getMessage() + "\"}");
        }
    }
}
