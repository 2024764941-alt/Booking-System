package com.dottstudio.util; // Force Recompile v2

import java.sql.*;
import com.dottstudio.model.*;

public class UserDAO {

    // Authenticate and return the correct subclass (User, Admin, or Designer)
    public static User login(String email, String password) throws SQLException {
        String sql = "SELECT u.*, r.RoleName " +
                "FROM USERS u " +
                "JOIN ROLES r ON u.RoleID = r.RoleID " +
                "WHERE u.Email = ? AND u.PasswordHash = ?"; // In prod, hash check logic here

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password); // Hashed in real app

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("RoleName");
                    int userId = rs.getInt("UserID");

                    // If Designer, fetch extra details
                    if ("DESIGNER".equalsIgnoreCase(role)) {
                        return getDesignerDetails(conn, userId, rs);
                    }
                    // If Admin, fetch extra details
                    else if ("ADMIN".equalsIgnoreCase(role)) {
                        return getAdminDetails(conn, userId, rs);
                    }
                    // Default Customer/User
                    else {
                        return mapUser(rs);
                    }
                }
            }
        }
        return null;
    }

    // Helper to map basic User fields
    private static User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("UserID"));
        u.setEmail(rs.getString("Email"));
        u.setFirstName(rs.getString("FirstName"));
        u.setLastName(rs.getString("LastName"));
        u.setPhone(rs.getString("Phone"));
        u.setAddress(rs.getString("Address"));
        u.setRoleId(rs.getInt("RoleID"));
        u.setRoleName(rs.getString("RoleName"));

        int referralId = rs.getInt("ReferralID");
        if (!rs.wasNull()) {
            u.setReferralId(referralId);
        }

        int referredBy = rs.getInt("ReferredByUserID");
        if (!rs.wasNull()) {
            u.setReferredByUserId(referredBy);
        }

        return u;
    }

    private static User getDesignerDetails(Connection conn, int userId, ResultSet userRs) throws SQLException {
        // NEW SCHEMA: Updated column names
        String sql = "SELECT d.DesignerID, d.Bio, d.PrimarySpecialty, d.Status, d.EmploymentType, " +
                "s.StaffID, s.ManagerID, s.StaffType, s.JoinDate " +
                "FROM STAFF s " +
                "JOIN DESIGNERS d ON s.StaffID = d.StaffID " +
                "WHERE s.UserID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Designer d = new Designer();

                    // Map Common User Fields
                    d.setId(userRs.getInt("UserID"));
                    d.setEmail(userRs.getString("Email"));
                    d.setFirstName(userRs.getString("FirstName"));
                    d.setLastName(userRs.getString("LastName"));
                    d.setPhone(userRs.getString("Phone"));
                    d.setAddress(userRs.getString("Address"));
                    d.setRoleId(userRs.getInt("RoleID"));
                    d.setRoleName(userRs.getString("RoleName"));

                    // Map Common Staff/Designer Fields
                    d.setStaffId(rs.getInt("StaffID"));
                    int managerId = rs.getInt("ManagerID");
                    if (!rs.wasNull()) {
                        d.setManagerId(managerId);
                    }
                    d.setPosition(rs.getString("StaffType")); // Use StaffType as position
                    d.setJoinDate(rs.getDate("JoinDate"));

                    d.setDesignerId(rs.getInt("DesignerID"));
                    d.setBio(rs.getString("Bio"));
                    d.setSpecialty(rs.getString("PrimarySpecialty")); // Map PrimarySpecialty to Specialty

                    return d;
                }
            }
        }
        return mapUser(userRs); // Fallback
    }

    private static User getAdminDetails(Connection conn, int userId, ResultSet userRs) throws SQLException {
        // NEW SCHEMA: ADMINS merged into STAFF with StaffType discriminator
        String sql = "SELECT s.StaffID, s.StaffType, s.Department, s.ManagerID, s.JoinDate, s.Salary " +
                "FROM STAFF s " +
                "WHERE s.UserID = ? AND s.StaffType = 'ADMIN'";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Admin a = new Admin();
                    // User fields
                    a.setId(userRs.getInt("UserID"));
                    a.setEmail(userRs.getString("Email"));
                    a.setFirstName(userRs.getString("FirstName"));
                    a.setLastName(userRs.getString("LastName"));
                    a.setPhone(userRs.getString("Phone"));
                    a.setAddress(userRs.getString("Address"));
                    a.setRoleId(userRs.getInt("RoleID"));
                    a.setRoleName(userRs.getString("RoleName"));

                    // Staff fields
                    a.setStaffId(rs.getInt("StaffID"));

                    int managerId = rs.getInt("ManagerID");
                    if (!rs.wasNull()) {
                        a.setManagerId(managerId);
                    }

                    // Use StaffType as position
                    a.setPosition(rs.getString("StaffType"));
                    a.setJoinDate(rs.getDate("JoinDate"));

                    // Admin fields (from STAFF table now)
                    a.setAdminId(rs.getInt("StaffID")); // Use StaffID as AdminID
                    a.setDepartment(rs.getString("Department"));

                    return a;
                }
            }
        }
        return mapUser(userRs); // Fallback
    }

    public static boolean registerCustomer(String fullName, String email, String phone, String address,
            String passwordHash, String referralSource, String friendEmail) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Resolve Referral Source (Auto-Insert if missing)
            Integer referralId = null;
            if (referralSource != null && !referralSource.trim().isEmpty()) {
                String refSourceSql = "SELECT ReferralID FROM REFERRALS WHERE ReferralSource = ?";
                try (PreparedStatement stmt = conn.prepareStatement(refSourceSql)) {
                    stmt.setString(1, referralSource);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            referralId = rs.getInt("ReferralID");
                        }
                    }
                }

                // If not found, insert it
                if (referralId == null) {
                    System.out.println(">>> DEBUG: Referral source not found, inserting: " + referralSource);
                    String insertRefSql = "INSERT INTO REFERRALS (ReferralSource) VALUES (?)";
                    // PostgreSQL / Standard JDBC
                    try (PreparedStatement stmt = conn.prepareStatement(insertRefSql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, referralSource);
                        int affected = stmt.executeUpdate();
                        if (affected > 0) {
                            try (ResultSet rs = stmt.getGeneratedKeys()) {
                                if (rs.next()) {
                                    referralId = rs.getInt(1);
                                    System.out.println(">>> DEBUG: Created new ReferralID: " + referralId);
                                }
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println(">>> DEBUG: SQL Error inserting referral: " + e.getMessage());
                        // e.printStackTrace(); // Optional
                    }
                } else {
                    System.out.println(">>> DEBUG: Found existing ReferralID: " + referralId);
                }
            } else {
                System.out.println(">>> DEBUG: ReferralSource is null or empty");
            }

            // 2. Resolve Friend Referral (Recursive)
            Integer referredByUserId = null;
            if (friendEmail != null && !friendEmail.trim().isEmpty()) {
                System.out.println(">>> DEBUG: Looking up referrer email: " + friendEmail);
                String refUserSql = "SELECT UserID FROM USERS WHERE Email = ?";
                try (PreparedStatement stmt = conn.prepareStatement(refUserSql)) {
                    stmt.setString(1, friendEmail);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            referredByUserId = rs.getInt("UserID");
                            System.out.println(">>> DEBUG: Found referrer UserID: " + referredByUserId);
                        } else {
                            System.out.println(">>> DEBUG: Referrer email not found in database.");
                        }
                    }
                }
            }

            // 3. Get Customer Role ID
            int roleId = 3; // Default Customer
            String roleSql = "SELECT RoleID FROM ROLES WHERE RoleName = 'CUSTOMER'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(roleSql)) {
                if (rs.next())
                    roleId = rs.getInt("RoleID");
            }

            // 4. Insert User
            String firstName = "";
            String lastName = "";
            if (fullName.contains(" ")) {
                String[] parts = fullName.split(" ", 2);
                firstName = parts[0];
                lastName = parts[1];
            } else {
                firstName = fullName;
            }

            String insertSql = "INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, Phone, Address, RoleID, ReferralID, ReferredByUserID) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, email);
                stmt.setString(2, passwordHash);
                stmt.setString(3, firstName);
                stmt.setString(4, lastName);
                stmt.setString(5, phone);
                stmt.setString(6, address);
                stmt.setInt(7, roleId);

                if (referralId != null)
                    stmt.setInt(8, referralId);
                else
                    stmt.setNull(8, java.sql.Types.INTEGER);

                if (referredByUserId != null)
                    stmt.setInt(9, referredByUserId);
                else
                    stmt.setNull(9, java.sql.Types.INTEGER);

                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public static boolean registerUser(User user, String password) throws SQLException {
        // Logic to insert into USERS
        // Return true if success
        // This needs to handle RoleID lookup or input
        return false; // Placeholder
    }

    public static boolean updateUser(User user, String newPassword) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE USERS SET FirstName = ?, LastName = ?, Phone = ?, Address = ?");
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            sql.append(", PasswordHash = ?");
        }
        sql.append(" WHERE UserID = ?");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getAddress());

            int paramIndex = 5;
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                stmt.setString(paramIndex++, newPassword); // valid in prod this should be hashed
            }

            stmt.setInt(paramIndex, user.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // New method for updating Staff/Designer details
    public static boolean updateStaff(int staffId, String firstName, String lastName, String phone, String specialty,
            String bio,
            int maxHours, int minHours, int maxProjects, int maxBookings, String status) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Get UserID from StaffID
            int userId = -1;
            String getUserSql = "SELECT UserID FROM STAFF WHERE StaffID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getUserSql)) {
                stmt.setInt(1, staffId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        userId = rs.getInt("UserID");
                }
            }

            if (userId == -1)
                return false;

            // 2. Update USERS table (Name, Phone)
            String updateUserSql = "UPDATE USERS SET FirstName = ?, LastName = ?, Phone = ? WHERE UserID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateUserSql)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, phone);
                stmt.setInt(4, userId);
                int rows = stmt.executeUpdate();
                System.out.println("DEBUG: UserDAO updated USERS table. Rows affected: " + rows + " for UserID: " + userId);
            }

            // 2.5 Note: Status is now in DESIGNERS table, not STAFF
            // Status update is handled in the DESIGNERS update below

            // 3. Update or Insert DESIGNERS table
            // Check if designer record exists
            String checkDesignerSql = "SELECT DesignerID FROM DESIGNERS WHERE StaffID = ?";
            boolean isDesigner = false;
            try (PreparedStatement stmt = conn.prepareStatement(checkDesignerSql)) {
                stmt.setInt(1, staffId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        isDesigner = true;
                }
            }

            if (isDesigner) {
                // UPDATE existing record
                String updateDesignerSql = "UPDATE DESIGNERS SET PrimarySpecialty = ?, Bio = ?, " +
                        "MaxHoursPerWeek = ?, MinHoursGuaranteed = ?, MaxSimultaneousProjects = ?, MaxBookingsPerWeek = ?, Status = ? "
                        +
                        "WHERE StaffID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateDesignerSql)) {
                    stmt.setString(1, specialty);
                    stmt.setString(2, bio);
                    stmt.setInt(3, maxHours);
                    stmt.setInt(4, minHours);
                    stmt.setInt(5, maxProjects);
                    stmt.setInt(6, maxBookings);
                    stmt.setString(7, status);
                    stmt.setInt(8, staffId);
                    stmt.executeUpdate();
                }
            } else {
                // INSERT missing record (Self-Healing)
                String insertDesignerSql = "INSERT INTO DESIGNERS (StaffID, PrimarySpecialty, Bio, MaxHoursPerWeek, MinHoursGuaranteed, MaxSimultaneousProjects, MaxBookingsPerWeek, Status, EmploymentType) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'FULL_TIME')";
                try (PreparedStatement stmt = conn.prepareStatement(insertDesignerSql)) {
                    stmt.setInt(1, staffId);
                    stmt.setString(2, specialty != null ? specialty : "");
                    stmt.setString(3, bio != null ? bio : "");
                    stmt.setInt(4, maxHours);
                    stmt.setInt(5, minHours);
                    stmt.setInt(6, maxProjects);
                    stmt.setInt(7, maxBookings);
                    stmt.setString(8, status != null ? status : "Active");
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // New method for Admin Dashboard synchronization
    public static java.util.List<Designer> getAllDesigners() throws SQLException {
        java.util.List<Designer> designers = new java.util.ArrayList<>();
        
        String sql = "SELECT s.StaffID, u.FirstName, u.LastName, u.Email, u.Phone, " +
                     "d.DesignerID, d.Bio, d.PrimarySpecialty, d.Status, " +
                     "d.MaxHoursPerWeek, d.MinHoursGuaranteed, d.MaxSimultaneousProjects, d.MaxBookingsPerWeek, " +
                     "(SELECT COUNT(*) FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.Status NOT IN ('Cancelled', 'Completed')) AS CurrentProjects, " +
                     "(SELECT COUNT(*) FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.Status = 'Completed') AS CompletedProjects " +
                     "FROM STAFF s " +
                     "JOIN USERS u ON s.UserID = u.UserID " +
                     "JOIN DESIGNERS d ON s.StaffID = d.StaffID " +
                     "ORDER BY u.FirstName";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Designer d = new Designer();
                d.setStaffId(rs.getInt("StaffID"));
                d.setFirstName(rs.getString("FirstName"));
                d.setLastName(rs.getString("LastName"));
                d.setEmail(rs.getString("Email"));
                d.setPhone(rs.getString("Phone"));
                
                d.setDesignerId(rs.getInt("DesignerID"));
                d.setBio(rs.getString("Bio"));
                d.setSpecialty(rs.getString("PrimarySpecialty"));
                d.setStatus(rs.getString("Status"));
                
                // Handle nulls/defaults for capacity
                d.setMaxHoursPerWeek(rs.getInt("MaxHoursPerWeek"));
                if (rs.wasNull()) d.setMaxHoursPerWeek(40);
                
                d.setMinHoursGuaranteed(rs.getInt("MinHoursGuaranteed"));
                if (rs.wasNull()) d.setMinHoursGuaranteed(0);
                
                d.setMaxSimultaneousProjects(rs.getInt("MaxSimultaneousProjects"));
                if (rs.wasNull()) d.setMaxSimultaneousProjects(5);
                
                d.setMaxBookingsPerWeek(rs.getInt("MaxBookingsPerWeek"));
                if (rs.wasNull()) d.setMaxBookingsPerWeek(20);
                
                d.setCurrentProjects(rs.getInt("CurrentProjects"));
                d.setCompletedProjects(rs.getInt("CompletedProjects"));
                
                designers.add(d);
            }
        }
        return designers;
    }

    public static Designer getDesignerByUserId(int userId) throws SQLException {
        String sql = "SELECT s.StaffID, u.FirstName, u.LastName, u.Email, u.Phone, " +
                     "d.DesignerID, d.Bio, d.PrimarySpecialty, d.Status, " +
                     "d.MaxHoursPerWeek, d.MinHoursGuaranteed, d.MaxSimultaneousProjects, d.MaxBookingsPerWeek, " +
                     "(SELECT COUNT(*) FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.Status NOT IN ('Cancelled', 'Completed')) AS CurrentProjects, " +
                     "(SELECT COUNT(*) FROM BOOKINGS b WHERE b.DesignerID = d.DesignerID AND b.Status = 'Completed') AS CompletedProjects " +
                     "FROM STAFF s " +
                     "JOIN USERS u ON s.UserID = u.UserID " +
                     "JOIN DESIGNERS d ON s.StaffID = d.StaffID " +
                     "WHERE u.UserID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Designer d = new Designer();
                    d.setId(userId);
                    d.setStaffId(rs.getInt("StaffID"));
                    d.setFirstName(rs.getString("FirstName"));
                    d.setLastName(rs.getString("LastName"));
                    d.setEmail(rs.getString("Email"));
                    d.setPhone(rs.getString("Phone"));
                    
                    d.setDesignerId(rs.getInt("DesignerID"));
                    d.setBio(rs.getString("Bio"));
                    d.setSpecialty(rs.getString("PrimarySpecialty"));
                    d.setStatus(rs.getString("Status"));
                    d.setRoleName("DESIGNER"); // Crucial for dashboard access check
                    
                    d.setMaxHoursPerWeek(rs.getInt("MaxHoursPerWeek"));
                    if (rs.wasNull()) d.setMaxHoursPerWeek(40);
                    
                    d.setMinHoursGuaranteed(rs.getInt("MinHoursGuaranteed"));
                    if (rs.wasNull()) d.setMinHoursGuaranteed(0);
                    
                    d.setMaxSimultaneousProjects(rs.getInt("MaxSimultaneousProjects"));
                    if (rs.wasNull()) d.setMaxSimultaneousProjects(5);
                    
                    d.setMaxBookingsPerWeek(rs.getInt("MaxBookingsPerWeek"));
                    if (rs.wasNull()) d.setMaxBookingsPerWeek(20);
                    
                    d.setCurrentProjects(rs.getInt("CurrentProjects"));
                    d.setCompletedProjects(rs.getInt("CompletedProjects"));
                    
                    return d;
                }
            }
        }
        return null;
    }
}
