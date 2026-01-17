package com.auth.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.auth.util.DBConnection;

@WebServlet("/api/diagnose")
public class DiagnosticServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        
        out.println("=== Auth Service Diagnostic ===");
        
        try (Connection conn = DBConnection.getConnection()) {
            out.println("DB Connection: SUCCESS");
            
            // Check Roles
            out.println("\n--- ROLES ---");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM ROLES")) {
                while (rs.next()) {
                    out.println(rs.getInt("RoleID") + ": " + rs.getString("RoleName"));
                }
            }

            // Check Users
            out.println("\n--- USERS ---");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT UserID, Email, PasswordHash, RoleID FROM USERS")) {
                while (rs.next()) {
                    out.println("ID: " + rs.getInt("UserID") + 
                                ", Email: [" + rs.getString("Email") + "]" +
                                ", Pass: [" + rs.getString("PasswordHash") + "]" +
                                ", RoleID: " + rs.getInt("RoleID"));
                }
            }
            
        } catch (Exception e) {
            out.println("DB Connection FAILED: " + e.getMessage());
            e.printStackTrace(out);
        }
    }
}
