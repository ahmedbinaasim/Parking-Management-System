package com.pms.controllers;

// Session.java
public class Session {
    private static Integer currentUserId;
    private static String username;
    private static String userRole;

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int id) {
        currentUserId = id;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String name) {
        username = name;
    }

    public static String getUserRole() {
        return userRole;
    }

    public static void setUserRole(String role) {
        userRole = role;
    }

    public static void clearSession() {
        currentUserId = null;
        username = null;
        userRole = null;
    }

    public static boolean isLoggedIn() {
        return currentUserId != null && username != null && userRole != null;
    }
}