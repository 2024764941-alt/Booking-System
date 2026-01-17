package com.dottstudio.test;

import com.dottstudio.util.PasswordUtils;
import com.dottstudio.util.UserDAO;
import com.dottstudio.model.User;

public class TestRegisteredCustomer {
    public static void main(String[] args) {
        System.out.println("Testing login with registered customers...\n");

        String[][] testAccounts = {
                { "w@gmail.com", "w" },
                { "a@gmail.com", "a" },
                { "jane@example.com", "admin123" }
        };

        for (String[] account : testAccounts) {
            String email = account[0];
            String password = account[1];

            System.out.println("Testing: " + email + " with password: " + password);

            try {
                String hashedPassword = PasswordUtils.hashPassword(password);
                System.out.println("  Hash: " + hashedPassword);

                User user = UserDAO.login(email, hashedPassword);

                if (user != null) {
                    System.out.println("  ✓ SUCCESS - User ID: " + user.getId());
                    System.out.println("  Name: " + user.getFirstName() + " " + user.getLastName());
                    System.out.println("  Role: " + user.getRoleName());
                } else {
                    System.out.println("  ✗ FAILED - User is null (wrong credentials)");
                }

            } catch (Exception e) {
                System.out.println("  ✗ ERROR:");
                e.printStackTrace();
            }

            System.out.println();
        }
    }
}
