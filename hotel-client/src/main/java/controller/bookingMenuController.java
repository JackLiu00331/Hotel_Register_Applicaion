package controller;

import application.Main;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller class for managing the booking menu UI.
 */
public class bookingMenuController {
    @FXML
    private Label noOfBookingLabel;
    @FXML
    private TableView<List<String>> bookTable;
    @FXML
    private TableColumn<List<String>, String> bookingNumberColumn;
    @FXML
    private TableColumn<List<String>, String> custnameColumn;
    @FXML
    private TableColumn<List<String>, String> roomtypeColumn;
    @FXML
    private TableColumn<List<String>, String> roomnumColumn;
    @FXML
    private TableColumn<List<String>, String> noofdayColumn;
    @FXML
    private Button back;
    private DataOutputStream toServer = null;
    private DataInputStream fromServer = null;
    Util util = new Util();

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
        bookingNumberColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get(0)));
        custnameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get(1)));
        roomtypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get(2)));
        roomnumColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get(3)));
        noofdayColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get(4)));
    }

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
     * Handles the action when the check button is clicked.
     */
    @FXML
    private void handleCheckAction() {
        getBookings();
    }

    /**
     * Handles the action when the delete button is clicked.
     */
    @FXML
    private void handleDeleteAction() {
        deleteSelected();
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
     * Deletes the selected booking from the table.
     */
    private void deleteSelected() {
        List<String> selectedBooking = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            util.showAlertWarning("Warning", "Please select a booking to delete.");
            return;
        }

        String bookingId = selectedBooking.get(0); // Assuming the first element is the booking ID.

        try {
            toServer.writeUTF("DELETE_BOOKING " + bookingId);
            toServer.flush();

            String response = fromServer.readUTF();
            if ("SUCCESS".equals(response)) {
                util.showAlertInformation("Information", "Booking deleted successfully.");
                getBookings(); // Refresh the bookings list.
            } else {
                util.showAlertError("Error", "Failed to delete booking.");
            }
        } catch (IOException e) {
            util.showAlertError("Error", "Error communicating with the server: " + e.getMessage());
        }
    }

    /**
     * Retrieves bookings from the server and displays them in the table.
     */
    private void getBookings() {
        // Before attempting to write to the server, check if the streams are null
        if (toServer == null || fromServer == null) {
            util.showAlertWarning("Warning", "Not connected to the server.");
            return;
        }

        try {
            toServer.writeUTF("CHECK_BOOKINGS");
            toServer.flush();

            String response = fromServer.readUTF();
            displayBookingDetails(response);
        } catch (IOException e) {
            util.showAlertWarning("Warning", "Error communicating with the server.");
        }
    }

    /**
     * Displays booking details in the table.
     *
     * @param data The data received from the server.
     */
    private void displayBookingDetails(String data) {
        bookTable.getItems().clear(); // Clear the table before adding new data.

        if (data.startsWith("SUCCESS")) {
            String[] bookingEntries = data.substring("SUCCESS".length()).trim().split("; ");
            List<List<String>> tableData = new ArrayList<>();
            for (String entry : bookingEntries) {
                String[] parts = entry.trim().split("\\s+", 6);
                String fullName = parts[1] + " " + parts[2];

                // Create a list representing a row for the TableView.
                List<String> rowData = Arrays.asList(
                        parts[0], // Booking #
                        fullName, // Customer Name
                        parts[3], // Room Type
                        parts[4], // Room Numbers
                        parts[5] // No of Days
                );

                tableData.add(rowData); // Add the row to the table data.
            }

            bookTable.setItems(FXCollections.observableArrayList(tableData)); // Set the table's items to the new data.
            noOfBookingLabel.setText(String.valueOf(tableData.size())); // Update the booking count label.
        } else {
            // If there's no "SUCCESS" in the response, it means no bookings were found.
            noOfBookingLabel.setText("0");
            util.showAlertWarning("Warning", "No bookings found.");
        }
    }
}
