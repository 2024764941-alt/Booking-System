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

import com.dottstudio.model.Designer;
import com.dottstudio.model.User;
import com.dottstudio.util.UserDAO;

@WebServlet("/designer/update-settings")
public class DesignerSettingsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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

        if (!(user instanceof Designer)) {
            out.print("{\"success\": false, \"message\": \"Access denied. Not a designer.\"}");
            return;
        }

        Designer designer = (Designer) user;

        try {
            // Get Parameters
            String status = request.getParameter("status");
            int maxHours = parseInt(request.getParameter("maxHours"), 40);
            int minHours = parseInt(request.getParameter("minHours"), 0);
            int maxProjects = parseInt(request.getParameter("maxProjects"), 5);
            int maxBookings = parseInt(request.getParameter("maxBookings"), 20);
            
            // These might be passed if we want to update profile same time, 
            // but for now we keep existing values if not provided
            String specialty = request.getParameter("specialty"); 
            if (specialty == null) specialty = designer.getSpecialty();
            
            String bio = request.getParameter("bio");
            if (bio == null) bio = designer.getBio();

            // Update Database
            // Note: UpdateStaff requires firstName/lastName/phone too, so we pass current values
            boolean success = UserDAO.updateStaff(
                designer.getStaffId(),
                designer.getFirstName(),
                designer.getLastName(),
                designer.getPhone(),
                specialty,
                bio,
                maxHours,
                minHours,
                maxProjects,
                maxBookings,
                status
            );

            if (success) {
                // Update Session Object so page refresh shows new data
                designer.setStatus(status);
                designer.setMaxHoursPerWeek(maxHours);
                designer.setMinHoursGuaranteed(minHours);
                designer.setMaxSimultaneousProjects(maxProjects);
                designer.setMaxBookingsPerWeek(maxBookings);
                designer.setSpecialty(specialty);
                designer.setBio(bio);
                
                session.setAttribute("user", designer); // Re-set to ensure replication if clustering (not needed here but good practice)

                out.print("{\"success\": true, \"message\": \"Settings updated successfully\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Database update failed\"}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"System error: " + e.getMessage() + "\"}");
        }
    }

    private int parseInt(String val, int def) {
        try {
            if (val != null && !val.isEmpty()) return Integer.parseInt(val);
        } catch (Exception e) {}
        return def;
    }
}
