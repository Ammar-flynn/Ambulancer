package com.example.Ambulancer.services;

public class SessionManager {

    private static String hospitalName;
    private static String hospitalBranch;

    private SessionManager() {}

    public static void setHospital(String name, String branch) {
        hospitalName = name;
        hospitalBranch = branch;
    }

    public static String getHospitalName() {
        return hospitalName;
    }

    public static String getHospitalBranch() {
        return hospitalBranch;
    }

    public static boolean isLoggedIn() {
        return hospitalName != null;
    }

    public static void clear() {
        hospitalName = null;
        hospitalBranch = null;
    }
}
