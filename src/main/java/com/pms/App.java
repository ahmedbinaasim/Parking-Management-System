package com.pms;

import com.pms.controllers.SceneManager;
import com.pms.utils.DatabaseManager;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.util.logging.Logger;
import java.util.logging.Level;

public class App extends Application {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Configure primary stage
            primaryStage.setTitle("Parking Management System");
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            
            // Set the stage in SceneManager
            SceneManager.setStage(primaryStage);

            // Initialize database connection
            initializeDatabase();

            // Load initial scene
            SceneManager.switchScene("Login.fxml");
            
            primaryStage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            showErrorAndExit();
        }
    }

    private void initializeDatabase() {
        if (!DatabaseManager.connect()) {
            LOGGER.severe("Failed to connect to database");
            showErrorAndExit();
        } else {
            LOGGER.info("Database connection successful");
        }
    }

    private void showErrorAndExit() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Application Error");
        alert.setHeaderText("Fatal Error");
        alert.setContentText("Failed to initialize application. Please check logs.");
        alert.showAndWait();
        System.exit(1);
    }

    @Override
    public void stop() {
        // Clean up resources
        DatabaseManager.disconnect();
        LOGGER.info("Application shutting down");
    }

    public static void main(String[] args) {
        launch(args);
    }
}