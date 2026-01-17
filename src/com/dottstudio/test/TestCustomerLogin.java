package com.dottstudio.test;

import com.dottstudio.util.DBConnection;
import com.dottstudio.util.PasswordUtils;
import com.dottstudio.util.UserDAO;
import com.dottstudio.model.User;

public class TestCustomerLogin {
    public static void main(String[] args) {
        System.out.println("Testing Customer Login...");

        String email = "jane@example.com";
        String password = "admin123";

        try {
            System.out.println("Hashing password...");
            String hashedPassword = PasswordUtils.hashPassword(password);
            System.out.println("Hash: " + hashedPassword);

            System.out.println("\nAttempting login...");
            User user = UserDAO.login(email, hashedPassword);

            if (user != null) {
                System.out.println("SUCCESS!");
                System.out.println("User ID: " + user.getId());
                System.out.println("Name: " + user.getFirstName() + " " + user.getLastName());
                System.out.println("Email: " + user.getEmail());
                System.out.println("Role Name: " + user.getRoleName());
                System.out.println("Role ID: " + user.getRoleId());
            } else {
                System.out.println("FAILED: User is null");
            }

        } catch (Exception e) {
            System.out.println("ERROR:");
            e.printStackTrace();
        }
    }
}
