package com.example.dsproject;

public class Hospital {
    private final String name;
    private final String branch;

    public Hospital(String name, String branch) {
        this.name = name;
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public String getBranch() {
        return branch;
    }
}