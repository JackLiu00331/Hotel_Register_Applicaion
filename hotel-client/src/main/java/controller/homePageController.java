package controller;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.Util;


public class homePageController {
    @FXML
    private Button start; // Define a button for starting the application

    @FXML
    private Button login; // Define a button for logging into the application

    @FXML
    private Button exit; // Define a button for exiting the application

    Util util = new Util(); // Instantiate Util class for utility methods

    // Method to handle action when the "Start" button is clicked
    @FXML
    public void handleStartAction() {
        start.getScene().getWindow().hide(); // Hide the current window
        util.showWindow("custMenu", "Main Menu", Main.getToServer(), Main.getFromServer()); // Show the customer menu window
    }

    // Method to handle action when the "Login" button is clicked
    @FXML
    public void handleLoginAction() {
        login.getScene().getWindow().hide(); // Hide the current window
        util.showWindow("adminLogin", "Admin Login", Main.getToServer(), Main.getFromServer()); // Show the admin login window
    }

    // Method to handle action when the "Exit" button is clicked
    @FXML
    public void handleExitAction() {
        Stage stage = (Stage) exit.getScene().getWindow(); // Get the stage of the current window
        stage.close(); // Close the stage, effectively exiting the application
    }
}
