package com.dottstudio.model;

import java.sql.Date;

public class Staff extends User {
    private int staffId;
    private Integer managerId; // Recursive
    private String position;
    private Date joinDate;

    // Additional Helper (Manager Name)
    private String managerName;

    public Staff() {
        super();
    }

    public Staff(int staffId, Integer managerId, String position, Date joinDate) {
        this.staffId = staffId;
        this.managerId = managerId;
        this.position = position;
        this.joinDate = joinDate;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
}
