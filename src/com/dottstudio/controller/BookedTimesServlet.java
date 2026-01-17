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

@WebServlet("/api/booked-times")
public class BookedTimesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String date = request.getParameter("date");

        if (date == null || date.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Missing date parameter\"}");
            return;
        }

        StringBuilder json = new StringBuilder("[");

        try (Connection conn = DBConnection.getConnection()) {
            // Query to get all booked times for a specific date
            // This aggregates times across all designers and removes duplicates
            String sql = "SELECT DISTINCT BookingTime " +
                    "FROM BOOKINGS " +
                    "WHERE TO_CHAR(BookingDate, 'YYYY-MM-DD') = ? " +
                    "AND Status != 'Cancelled' " +
                    "ORDER BY BookingTime";

            System.out.println("BookedTimesServlet: Fetching booked times for date: " + date);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, date);

                try (ResultSet rs = stmt.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        String bookedTime = rs.getString("BookingTime");
                        if (!first)
                            json.append(",");
                        first = false;
                        json.append("\"").append(bookedTime).append("\"");
                        System.out.println("BookedTimesServlet: Found booked time: " + bookedTime);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + escape(e.getMessage()) + "\"}");
            return;
        }

        json.append("]");
        out.print(json.toString());
        System.out.println("BookedTimesServlet: Response: " + json.toString());
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
