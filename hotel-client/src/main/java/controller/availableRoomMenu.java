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
 * Controller class for managing the available room menu UI.
 */
public class availableRoomMenu {
    @FXML
    private Label noOfRoomLabel;
    @FXML
    private TableView<List<String>> roomTable;
    @FXML
    private TableColumn<List<String>, String> roomNumColumn;
    @FXML
    private TableColumn<List<String>, String> roomTypeColumn;
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
        roomNumColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get(0)));
        roomTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get(1)));
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
        getRooms();
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
     * Retrieves available rooms from the server and displays them in the table.
     */
    private void getRooms() {
        // Before attempting to write to the server, check if the streams are null
        if (toServer == null || fromServer == null) {
            util.showAlertWarning("Warning", "Not connected to the server.");
            return;
        }

        try {
            toServer.writeUTF("CHECK_ROOMS");
            toServer.flush();

            String response = fromServer.readUTF();
            displayAvailableRooms(response);
        } catch (IOException e) {
            util.showAlertWarning("Warning", "Error communicating with the server.");
        }
    }

    /**
     * Displays available rooms in the table.
     *
     * @param data The data received from the server.
     */
    private void displayAvailableRooms(String data) {
        roomTable.getItems().clear();

        if (data.startsWith("SUCCESS")) {
            String[] roomEntries = data.substring("SUCCESS".length()).trim().split("; ");
            List<List<String>> tableData = new ArrayList<>();
            for (String entry : roomEntries) {
                String[] parts = entry.trim().split("\\s+", 2);
                // Create a list representing a row for the TableView.
                List<String> rowData = Arrays.asList(
                        parts[0], // Room #
                        parts[1] //  Room Type
                );

                tableData.add(rowData); // Add the row to the table data.
            }

            roomTable.setItems(FXCollections.observableArrayList(tableData)); // Set the table's items to the new data.
            noOfRoomLabel.setText(String.valueOf(tableData.size())); // Update the booking count label.
        } else {
            // If there's no "SUCCESS" in the response, it means no bookings were found.
            noOfRoomLabel.setText("0");
            util.showAlertWarning("Warning", "No bookings found.");
        }
    }
}

