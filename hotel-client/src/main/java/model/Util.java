package model;

import controller.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class Util {
    // Method to show a window based on the provided FXML file name and title, with streams for communication
    public void showWindow(String formName, String title, DataOutputStream toServer, DataInputStream fromServer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/" + formName + ".fxml"));
            Parent form = loader.load(); // Load FXML file and obtain the root node

            // Depending on the form name, set streams for communication in the controller
            if (formName.equals("adminLogin")) {
                adminLoginController controller = loader.getController();
                controller.setStreams(toServer, fromServer);
            } else if (formName.equals("custBooking")) {
                custBookingController controller = loader.getController();
                controller.setStreams(toServer, fromServer);
            } else if (formName.equals("adminBill")) {
                adminBillController controller = loader.getController();
                controller.setStreams(toServer, fromServer);
            } else if (formName.equals("bookingMenu")) {
                bookingMenuController controller = loader.getController();
                controller.setStreams(toServer, fromServer);
            } else if (formName.equals("availableRoomMenu")) {
                availableRoomMenu controller = loader.getController();
                controller.setStreams(toServer, fromServer);
            }

            // Create a new stage, set its title, scene, and display it
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(form));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Print the stack trace if an IOException occurs
        }
    }

    // Method to show a window with a booking ID, passing streams for communication
    public void showWindowWithBookingId(String fxmlFile, String title, int bookingId, DataOutputStream toServer, DataInputStream fromServer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile + ".fxml"));
            Parent root = loader.load(); // Load FXML file and obtain the root node

            // Get the controller of the new window and set the bookingId and streams
            Object controller = loader.getController();
            if (controller instanceof BookingIdAwareController) {
                ((BookingIdAwareController) controller).setBookingId(bookingId);
                ((BookingIdAwareController) controller).setStreams(toServer, fromServer);
            }

            // Create a new stage, set its title, scene, and display it
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Print the stack trace if an IOException occurs
            // Handle exceptions here, perhaps alert the user that the UI failed to load
        }
    }

    // Method to show an information alert dialog
    public void showAlertInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to show a warning alert dialog
    public void showAlertWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to show an error alert dialog
    public void showAlertError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to validate an email address using regular expression
    public boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex); // Compile the regex pattern
        if (email == null) {
            return false; // If email is null, return false
        }
        return pattern.matcher(email).matches(); // Return true if email matches the pattern
    }
}
