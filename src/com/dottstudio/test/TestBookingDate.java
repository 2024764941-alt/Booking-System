package com.dottstudio.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestBookingDate {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
        String user = "dott_admin";
        String pass = "dott123";

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement stmt = conn.createStatement();

            System.out.println("--- Checking Booking Dates ---");
            ResultSet rs = stmt
                    .executeQuery("SELECT id, booking_date FROM bookings ORDER BY id DESC FETCH FIRST 5 ROWS ONLY");

            while (rs.next()) {
                System.out.println(
                        "ID: " + rs.getInt("id") + " | Date Raw Value: [" + rs.getString("booking_date") + "]");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
