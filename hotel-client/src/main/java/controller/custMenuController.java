package controller;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import model.Util;

public class custMenuController {
    Util util = new Util(); // Instantiate Util class for utility methods

    @FXML
    private Button book; // Define a button for booking

    // Method to handle action when the "Book" button is clicked
    @FXML
    public void handleBookAction() {
        book.getScene().getWindow().hide(); // Hide the current window
        // Show the customer booking window
        util.showWindow("custBooking", "Guest Booking", Main.getToServer(), Main.getFromServer());
    }

    // Method to handle action when the "Exit" button is clicked
    @FXML
    public void handleExitAction() {
        book.getScene().getWindow().hide(); // Hide the current window
        // Show the home page window
        util.showWindow("homePage", "Home page", Main.getToServer(), Main.getFromServer());
    }
}

