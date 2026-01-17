package com.dottstudio.controller;

import com.dottstudio.util.DBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/search-users")
public class DesignerSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String term = request.getParameter("term");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (term == null || term.trim().isEmpty()) {
            out.print("[]");
            return;
        }

        List<String> emails = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            // Updated Query to join with ROLES table
            // Updated Query to fetch details for populating the form
            String sql = "SELECT u.Email, u.Phone, u.FirstName, u.LastName " +
                    "FROM USERS u " +
                    "JOIN ROLES r ON u.RoleID = r.RoleID " +
                    "WHERE LOWER(u.Email) LIKE LOWER(?) AND r.RoleName = 'CUSTOMER'";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + term + "%");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String email = rs.getString("Email");
                        String phone = rs.getString("Phone");
                        if (phone == null) phone = "";
                        String fName = rs.getString("FirstName");
                        if (fName == null) fName = "";
                        String lName = rs.getString("LastName");
                        if (lName == null) lName = "";
                        
                        // Simple JSON Object construction
                        String jsonObj = String.format("{\"email\":\"%s\", \"phone\":\"%s\", \"firstName\":\"%s\", \"lastName\":\"%s\"}", 
                                email, phone, fName, lName);
                        emails.add(jsonObj);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
            return;
        }

        // Manual JSON Array construction
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < emails.size(); i++) {
            json.append(emails.get(i));
            if (i < emails.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        out.print(json.toString());
    }
}
