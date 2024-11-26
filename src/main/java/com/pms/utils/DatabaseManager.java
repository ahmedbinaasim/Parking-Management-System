package com.pms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseManager {
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static Connection connection;

    static {
        // try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
        //     // Check if the file was found
        //     if (input == null) {
        //         System.err.println("Error: db.properties file not found in the classpath.");
        //         //return;
        //     }
        //     // Load the properties from the file
        //     Properties props = new Properties();
        //     props.load(input);

        //     dbUrl = props.getProperty("db.url");
        //     dbUsername = props.getProperty("db.username");
        //     dbPassword = props.getProperty("db.password");

            dbUrl = "jdbc:mysql://localhost:3306/parking_management_system";
            dbUsername = "root";
            dbPassword = "2235256$#21";

            // Validate that the properties were loaded correctly
            if (dbUrl == null || dbUsername == null || dbPassword == null) {
                System.err.println("Error: Missing database configuration values.");
            }

        // } catch (Exception e) {
        //     System.err.println("Error loading database configuration: " + e.getMessage());
        // }
    }

    // Connect method to establish the connection
    public static boolean connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                System.out.println("Connected to the database successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return false;
    }

    // Disconnect method to close the connection
    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Disconnected from the database successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing the database connection: " + e.getMessage());
        }
    }

    // Get connection method
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect(); // Ensure the connection is established before returning
        }
        return connection;
    }

}
