package com.master.dailydose.details;

import java.util.ArrayList;

public class TaskDetail {
    private String taskName;
    private ArrayList<String> selectedTags;
    private boolean monthPlan;
    private boolean weekPlan;
    private boolean workPlan;
    private boolean yearPlan;
    private String status;
    private String id;
    private String date;
    private String time;

    public TaskDetail() {
        // Default constructor required for Firestore serialization
    }

    public TaskDetail(String taskName, String date, String time, ArrayList<String> selectedTags, boolean monthPlan, boolean weekPlan, boolean workPlan, boolean yearPlan) {
        this.taskName = taskName;
        this.date = date;
        this.time = time;
        this.selectedTags = selectedTags;
        this.monthPlan = monthPlan;
        this.weekPlan = weekPlan;
        this.workPlan = workPlan;
        this.yearPlan = yearPlan;
        this.status = "";
        this.id = ""; // Initialize id if needed
    }

    // Getters and setters

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public ArrayList<String> getSelectedTags() { return selectedTags; }
    public void setSelectedTags(ArrayList<String> selectedTags) { this.selectedTags = selectedTags; }

    public boolean isMonthPlan() { return monthPlan; }
    public void setMonthPlan(boolean monthPlan) { this.monthPlan = monthPlan; }

    public boolean isWeekPlan() { return weekPlan; }
    public void setWeekPlan(boolean weekPlan) { this.weekPlan = weekPlan; }

    public boolean isWorkPlan() { return workPlan; }
    public void setWorkPlan(boolean workPlan) { this.workPlan = workPlan; }

    public boolean isYearPlan() { return yearPlan; }
    public void setYearPlan(boolean yearPlan) { this.yearPlan = yearPlan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
