package com.auth.util;

import java.sql.*;
import com.auth.model.User;

public class UserDAO {

    // Updated for BCrypt
    public static User login(String email, String password) throws SQLException {
        // 1. Fetch user by Email only
        String sql = "SELECT u.UserID, u.Email, u.PasswordHash, u.FirstName, u.LastName, u.Phone, u.Address, u.RoleID, r.RoleName " +
                     "FROM USERS u " +
                     "JOIN ROLES r ON u.RoleID = r.RoleID " +
                     "WHERE u.Email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("PasswordHash");

                    // 2. Verify Password using BCrypt
                    if (org.mindrot.jbcrypt.BCrypt.checkpw(password, storedHash)) {
                        User u = new User();
                        u.setId(rs.getInt("UserID"));
                        u.setEmail(rs.getString("Email"));
                        u.setFirstName(rs.getString("FirstName"));
                        u.setLastName(rs.getString("LastName"));
                        u.setPhone(rs.getString("Phone"));
                        u.setAddress(rs.getString("Address"));
                        u.setRoleId(rs.getInt("RoleID"));
                        u.setRoleName(rs.getString("RoleName"));
                        return u;
                    }
                }
            }
        }
        return null; // Not found or Invalid Password
    }

    public static boolean register(User user, String plainPassword) throws SQLException {
        // Hash the password
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt());

        String sql = "INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, Phone, Address, RoleID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, hashedPassword); // Storing Hash
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getAddress());
            stmt.setInt(7, user.getRoleId() > 0 ? user.getRoleId() : 3); 

            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }
}
