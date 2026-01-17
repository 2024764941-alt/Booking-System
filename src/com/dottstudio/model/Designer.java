package com.dottstudio.model; // Force Recompile v2

public class Designer extends Staff {
    private int designerId;
    private String bio;
    private String specialty;
    private String employmentCategory; // "FULL" or "PART"

    public Designer() {
        super();
    }

    // Unified Designer Model - Removed Employment Type Specifics

    public int getDesignerId() {
        return designerId;
    }

    public void setDesignerId(int designerId) {
        this.designerId = designerId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    private String status;
    private int maxHoursPerWeek;
    private int minHoursGuaranteed;
    private int maxSimultaneousProjects;
    private int maxBookingsPerWeek;
    private int currentProjects; // Transient/Calculated
    private int completedProjects; // Transient/Calculated

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }
    public void setMaxHoursPerWeek(int maxHoursPerWeek) {
        this.maxHoursPerWeek = maxHoursPerWeek;
    }
    public int getMinHoursGuaranteed() {
        return minHoursGuaranteed;
    }
    public void setMinHoursGuaranteed(int minHoursGuaranteed) {
        this.minHoursGuaranteed = minHoursGuaranteed;
    }
    public int getMaxSimultaneousProjects() {
        return maxSimultaneousProjects;
    }
    public void setMaxSimultaneousProjects(int maxSimultaneousProjects) {
        this.maxSimultaneousProjects = maxSimultaneousProjects;
    }
    public int getMaxBookingsPerWeek() {
        return maxBookingsPerWeek;
    }
    public void setMaxBookingsPerWeek(int maxBookingsPerWeek) {
        this.maxBookingsPerWeek = maxBookingsPerWeek;
    }
    public int getCurrentProjects() {
        return currentProjects;
    }
    public void setCurrentProjects(int currentProjects) {
        this.currentProjects = currentProjects;
    }
    public int getCompletedProjects() {
        return completedProjects;
    }
    public void setCompletedProjects(int completedProjects) {
        this.completedProjects = completedProjects;
    }


    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
