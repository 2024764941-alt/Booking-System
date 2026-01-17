package com.dottstudio.controller;

import com.dottstudio.util.DBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/availability")
public class AvailabilityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String designerName = request.getParameter("designer");
        String date = request.getParameter("date");

        if (designerName == null || date == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Missing designer or date\"}");
            return;
        }

        StringBuilder json = new StringBuilder("[");

        try (Connection conn = DBConnection.getConnection()) {

            // Query to find booked times for this designer on this date
            // We join BOOKINGS -> BOOKING_DESIGNERS -> DESIGNERS -> STAFF -> USERS
            // To match the designer Name provided by the frontend

            String sql = "SELECT b.BookingTime " +
                    "FROM BOOKINGS b " +
                    "JOIN DESIGNERS d ON b.DesignerID = d.DesignerID " +
                    "JOIN STAFF s ON d.StaffID = s.StaffID " +
                    "JOIN USERS u ON s.UserID = u.UserID " +
                    "WHERE (u.FirstName || ' ' || u.LastName) = ? " +
                    "AND TO_CHAR(b.BookingDate, 'YYYY-MM-DD') = ? " +
                    "AND b.Status != 'Cancelled'";

            System.out
                    .println("AvailabilityServlet Debug: Checking for Designer: " + designerName + " on Date: " + date);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, designerName);
                stmt.setString(2, date);

                try (ResultSet rs = stmt.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        String bookedTime = rs.getString("BookingTime");
                        System.out.println("AvailabilityServlet Debug: Found booked time: " + bookedTime);
                        if (!first)
                            json.append(",");
                        first = false;
                        json.append("\"").append(bookedTime).append("\"");
                    }
                }
            }
            System.out.println("AvailabilityServlet Debug: Completed check. Result: " + json.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error\"}");
            return;
        }

        json.append("]");
        out.print(json.toString());
    }
}
