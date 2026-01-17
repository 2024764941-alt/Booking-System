package com.dottstudio.model;

import java.sql.Timestamp;

public class User {
    private int id; // Maps to UserID
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private int roleId;
    private String roleName; // Helper for display (fetched via join)
    private Integer referralId;
    private Integer referredByUserId; // Recursive
    private Timestamp createdAt;

    public User() {
    }

    public User(int id, String email, String passwordHash, String firstName, String lastName, String phone,
            String address, int roleId, Integer referralId, Integer referredByUserId) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.roleId = roleId;
        this.referralId = referralId;
        this.referredByUserId = referredByUserId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getReferralId() {
        return referralId;
    }

    public void setReferralId(Integer referralId) {
        this.referralId = referralId;
    }

    public Integer getReferredByUserId() {
        return referredByUserId;
    }

    public void setReferredByUserId(Integer referredByUserId) {
        this.referredByUserId = referredByUserId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Legacy support / Helper
    public String getRole() {
        if (roleName != null) return roleName;
        // Fallback based on RoleID constants
        switch (roleId) {
            case 1: return "ADMIN";
            case 2: return "DESIGNER";
            case 3: return "CUSTOMER";
            default: return String.valueOf(roleId);
        }
    }
}
