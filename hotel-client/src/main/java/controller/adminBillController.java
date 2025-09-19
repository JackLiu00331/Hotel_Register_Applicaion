/**********************************************
 Final Project
 Course:APD545 - WINTER2023
 Last Name:Liu
 First Name:Chao
 ID:160873170
 Section:ZAA
 This assignment represents my own work in accordance with Seneca Academic Policy.
 Chao Liu
 Date:2024/4/14
 **********************************************/
package controller;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import model.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Controller class for managing the admin bill UI.
 */
public class adminBillController {
    @FXML
    private GridPane billDetailGrid;
    @FXML
    private TextField searchBookIdField;
    @FXML
    private TextField discountField;
    @FXML
    private Label bookIdLabel;
    @FXML
    private Label guestNameLabel;
    @FXML
    private Label noOfRoomLabel;
    @FXML
    private Label roomTypeLabel;
    @FXML
    private Label rateLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Button back;
    private DataOutputStream toServer = null;
    private DataInputStream fromServer = null;
    Util util = new Util();

    /**
     * Sets the input/output streams for communication with the server.
     *
     * @param toServerStream   The output stream to the server.
     * @param fromServerStream The input stream from the server.
     */
    public void setStreams(DataOutputStream toServerStream, DataInputStream fromServerStream) {
        toServer = toServerStream;
        fromServer = fromServerStream;
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
        billDetailGrid.setVisible(false);
        discountField.setText("0");
        searchBills();
        giveDiscount();
    }

    /**
     * Sets up listener for search book ID field to search bills.
     */
    private void searchBills() {
        searchBookIdField.textProperty().addListener(((observableValue, oldValue, newValue) -> {
            getBills();
        }));
    }

    /**
     * Sets up listener for discount field to apply discount.
     */
    private void giveDiscount() {
        discountField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            try {
                if (Integer.parseInt(newValue) < 0 || Integer.parseInt(newValue) > 25) {
                    util.showAlertWarning("Warning", "Discount can not be less than 0 or bigger than 25%!");
                } else {
                    double total = Double.parseDouble(totalLabel.getText().isEmpty() ? "0" : totalLabel.getText());
                    double discount = newValue.isEmpty() ? 0 : Double.parseDouble(newValue);
                    total = total * (1 - discount / 100);
                    totalLabel.setText(String.format("%.2f", total));
                }
            } catch (NumberFormatException e) {
                totalLabel.setText(totalLabel.getText());
            }
        });
    }

    /**
     * Retrieves bill details from the server based on the entered book ID.
     */
    private void getBills() {
        try {
            Integer bookId = Integer.parseInt(searchBookIdField.getText());
            toServer.writeUTF("GET_BILL " + bookId);
            toServer.flush();

            String response = fromServer.readUTF();
            displayBillDetails(response);
        } catch (IOException e) {
            util.showAlertWarning("Warning", "Error communicating with the server.");
        }
    }

    /**
     * Displays bill details in the UI.
     *
     * @param data The data received from the server.
     */
    private void displayBillDetails(String data) {
        if (data.startsWith("SUCCESS")) {
            String[] details = data.split(" ");
            bookIdLabel.setText(details[1]);
            guestNameLabel.setText(details[2] + " " + details[3]);
            noOfRoomLabel.setText(details[4]);
            roomTypeLabel.setText(details[5]);
            rateLabel.setText(details[6]);
            totalLabel.setText(details[7]);
            double discount = Double.parseDouble(discountField.getText());
            double total = Double.parseDouble(totalLabel.getText());
            total = total * (1 - discount / 100);
            totalLabel.setText(String.format("%.2f", total));
            billDetailGrid.setVisible(true);
        } else {
            util.showAlertWarning("Warning", "No bill found with the given ID.");
            billDetailGrid.setVisible(false);
        }
    }

    /**
     * Handles the action when the back button is clicked.
     */
    @FXML
    public void handleBackAction() {
        back.getScene().getWindow().hide();
        util.showWindow("adminMenu", "Admin menu", Main.getToServer(), Main.getFromServer());
    }

    /**
     * Handles the action when the checkout button is clicked.
     */
    @FXML
    public void handleCheckoutAction() {
        try {
            String bookId = bookIdLabel.getText();
            String total = totalLabel.getText();
            toServer.writeUTF("CHECKOUT " + bookId + " " + total);
            toServer.flush();

            String response = fromServer.readUTF();
            handleCheckoutResponse(response);
        } catch (NumberFormatException e) {
            util.showAlertError("Error", "Invalid Booking ID format.");
        } catch (IOException e) {
            util.showAlertError("Error", "Communication error with the server.");
        }
    }

    /**
     * Handles the response after checkout.
     *
     * @param response The response from the server.
     */
    private void handleCheckoutResponse(String response) {
        if (response.startsWith("SUCCESS Updated")) {
            util.showAlertInformation("Checkout Updated", "The total amount has been updated.");
        } else if (response.startsWith("SUCCESS No update needed")) {
            util.showAlertInformation("Checkout Unchanged", "The total amount is the same, no update needed.");
        } else if (response.startsWith("SUCCESS")) {
            String[] responseParts = response.split(" ");
            int billId = Integer.parseInt(responseParts[1]);
            util.showAlertInformation("Checkout Successful", "New bill created with ID: " + billId);
        } else {
            util.showAlertError("Checkout Failed", "An error occurred during checkout.");
        }
    }
}

