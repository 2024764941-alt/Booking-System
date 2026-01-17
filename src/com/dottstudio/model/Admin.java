package com.dottstudio.model;

public class Admin extends Staff {
    private int adminId;
    private String department;

    public Admin() {
        super();
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
