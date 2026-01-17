package com.dottstudio.controller;

import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.dottstudio.util.UserDAO;

@WebServlet("/admin/update-staff")
public class UpdateStaffServlet extends HttpServlet {
    // FORCE_UPDATE_V2
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // DEBUG LOGGING
            String idStr = request.getParameter("id");
            System.out.println("UpdateStaffServlet POST: ID=" + idStr);
            
            if (idStr == null || idStr.isEmpty() || "undefined".equals(idStr)) {
                 response.setStatus(400); // Bad Request
                 response.getWriter().write("{\"status\":\"error\", \"message\":\"Missing or invalid Staff ID\"}");
                 return;
            }

            int id = Integer.parseInt(idStr);
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String phone = request.getParameter("phone");
            String role = request.getParameter("role");
            String status = request.getParameter("status");
            String specialty = request.getParameter("specialty");
            String bio = request.getParameter("bio");

            System.out.println("DEBUG: UpdateStaffServlet processing ID: " + id);
            System.out.println("DEBUG: New Name: " + firstName + " " + lastName);


            // Safely parse params individually
            int maxHours = parseOrDefault(request.getParameter("maxHoursPerWeek"), request.getParameter("maxHours"), 40);
            int minHours = parseOrDefault(request.getParameter("minHoursGuaranteed"), request.getParameter("minHours"), 0);
            int maxProjects = parseOrDefault(request.getParameter("maxSimultaneousProjects"), request.getParameter("maxProjects"), 5);
            int maxBookings = parseOrDefault(request.getParameter("maxBookingsPerWeek"), request.getParameter("maxBookings"), 20);

            boolean success = UserDAO.updateStaff(id, firstName, lastName, phone, specialty, bio, maxHours, minHours,
                    maxProjects, maxBookings, status);

            if (success) {
                // Use simple manually constructed JSON with minimal risk
                response.getWriter().write("{\"status\":\"success\", \"message\":\"Settings saved successfully\"}");
            } else {
                response.setStatus(400);
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to update database record\"}");
            }

        } catch (Throwable e) {
            e.printStackTrace();
            response.setStatus(500);
            String safeMsg = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "Unknown System Error";
            response.getWriter().write("{\"status\":\"error\", \"message\":\"" + safeMsg + "\"}");
        }
    }

    private int parseOrDefault(String primary, String secondary, int defaultValue) {
        try {
            if (primary != null && !primary.isEmpty())
                return Integer.parseInt(primary);
            if (secondary != null && !secondary.isEmpty())
                return Integer.parseInt(secondary);
        } catch (NumberFormatException e) {
            // ignore
        }
        return defaultValue;
    }
}
