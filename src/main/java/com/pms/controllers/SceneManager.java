package com.pms.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage stage;

    // Sets the primary stage
    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    // Switches scenes by loading the given FXML file
    public static void switchScene(String fxmlFile) {
        try {
            // Load the FXML file
            System.out.println(fxmlFile);
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/pms/" + fxmlFile));
            
            // Load the root element and controller
            Parent root = loader.load();
            
            // Set up the scene
            Scene scene = new Scene(root, 800, 600);
            
            // Add the stylesheet for consistent styling
            scene.getStylesheets().add(SceneManager.class.getResource("/com/pms/css/styles.css").toExternalForm());
            
            // Apply the scene to the stage and display it
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Gets the controller for a given FXML file
    public static <T> T getController(String fxmlFile) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/pms/" + fxmlFile));
            
            // Load the root element (we don't need it for getting the controller)
            loader.load();
            
            // Return the controller associated with the FXML
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
