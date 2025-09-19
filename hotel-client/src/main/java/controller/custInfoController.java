package controller;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import model.Util;
import javafx.scene.control.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class custInfoController implements BookingIdAwareController {
    private int bookingId; // Initialize variable to store booking ID
    private DataOutputStream toServer; // Initialize variable to write data to the server
    private DataInputStream fromServer; // Initialize variable to read data from the server
    Util util = new Util(); // Instantiate Util class for utility methods

    @FXML
    private ComboBox titleComboBox;
    // Define UI elements using FXML annotations
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField addressTextField;
    @FXML
    private TextField phoneTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private Button submit;


    // Method to set the booking ID
    @Override
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId; // Set the booking ID received from the previous window
        System.out.println("Received booking ID: " + bookingId); // Print the received booking ID
    }

    // Method to set input and output streams
    @Override
    public void setStreams(DataOutputStream toServer, DataInputStream fromServer) {
        this.toServer = toServer; // Set the output stream
        this.fromServer = fromServer; // Set the input stream
    }

    // Method to initialize the controller
    @FXML
    private void initialize() {
        // Initialize title ComboBox with options
        ObservableList<String> titles = FXCollections.observableArrayList("Mr.", "Mrs.", "Ms.", "Dr.");
        titleComboBox.setItems(titles); // Set items in the ComboBox
    }

    // Method to handle action when the submit button is clicked
    @FXML
    public void handleSubmitAction() {
        // Retrieve values from UI controls
        String title = (String) titleComboBox.getValue();
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String address = addressTextField.getText();
        String phoneNum = phoneTextField.getText();
        String email = emailTextField.getText();

        // Validate input fields
        if (title == null || title.isEmpty()) {
            util.showAlertWarning("Warning", "Please fill the Title field.");
        } else if (firstName == null || firstName.isEmpty()) {
            util.showAlertWarning("Warning", "Please fill the First Name field.");
        } else if (lastName == null || lastName.isEmpty()) {
            util.showAlertWarning("Warning", "Please fill the Last Name field.");
        } else if (address == null || address.isEmpty()) {
            util.showAlertWarning("Warning", "Please fill the Address field.");
        } else if (phoneNum == null || phoneNum.isEmpty()) {
            util.showAlertWarning("Warning", "Please fill the Phone field.");
        } else if (email == null || email.isEmpty()) {
            util.showAlertWarning("Warning", "Please fill the Email field.");
        } else if (!util.isValidEmail(email)) {
            util.showAlertWarning("Warning", "Please enter a valid email address.");
        } else {
            // If all fields are valid, send guest information to the server
            sendGuestInfoToServer(title, firstName, lastName, address, phoneNum, email);
        }
    }

    // Method to send guest information to the server
    private void sendGuestInfoToServer(String title, String firstName, String lastName, String address, String phoneNum, String email) {
        String guestDetails = "CREATE_GUEST|" + String.join("|", title, firstName, lastName, address, phoneNum, email);
        System.out.println("Sending guest details to server: " + guestDetails);
        try {
            // Send guest details to the server
            toServer.writeUTF(guestDetails);
            toServer.flush();

            // Receive response from the server
            String response = fromServer.readUTF();
            // Handle the response
            handleInformationResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to handle the response received from the server
    private void handleInformationResponse(String response) {
        System.out.println("Response from server: " + response);
        // Check if the response indicates success
        if (response.startsWith("SUCCESS")) {
            // Extract the guest ID from the response
            int guestId = Integer.parseInt(response.split(" ")[1]);
            // Link the guest with the booking
            linkGuestWithBooking(guestId);
        } else {
            // If response indicates failure, show an error alert
            util.showAlertError("Error", "Unable to create guest information.");
        }
    }

    // Method to link the guest with the booking
    private void linkGuestWithBooking(int guestId) {
        String linkDetails = String.format("LINK_GUEST_BOOKING %d %d", guestId, bookingId);
        try {
            // Send link details to the server
            toServer.writeUTF(linkDetails);
            toServer.flush();

            // Receive response from the server
            String response = fromServer.readUTF();
            // Check if the response indicates success
            if ("SUCCESS".equals(response)) {
                // If successful, hide the current window and show the main menu window
                submit.getScene().getWindow().hide();
                util.showWindow("custMenu", "Main Menu", Main.getToServer(), Main.getFromServer());
            } else {
                // If response indicates failure, show an error alert
                util.showAlertError("Error", "Unable to link guest with booking.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
