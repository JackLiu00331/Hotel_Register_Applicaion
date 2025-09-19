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
import model.Util;

/**
 * Controller class for managing the admin menu UI.
 */
public class adminMenuController {
    Util util = new Util();
    @FXML
    private Button book;

    /**
     * Handles the action when the book button is clicked.
     */
    @FXML
    public void handleBookAction() {
        book.getScene().getWindow().hide();
        util.showWindow("custBooking", "Guest Booking", Main.getToServer(), Main.getFromServer());
    }

    /**
     * Handles the action when the bill button is clicked.
     */
    @FXML
    public void handleBillAction() {
        book.getScene().getWindow().hide();
        util.showWindow("adminBill", "Bill Service", Main.getToServer(), Main.getFromServer());
    }

    /**
     * Handles the action when the exit button is clicked.
     */
    @FXML
    public void handleExitAction() {
        book.getScene().getWindow().hide();
        util.showWindow("homePage", "Home page", Main.getToServer(), Main.getFromServer());
    }

    /**
     * Handles the action when the check booking button is clicked.
     */
    @FXML
    public void handleCheckBookAction() {
        book.getScene().getWindow().hide();
        util.showWindow("bookingMenu", "Current Bookings", Main.getToServer(), Main.getFromServer());
    }

    /**
     * Handles the action when the check room button is clicked.
     */
    @FXML
    public void handleCheckRoomAction() {
        book.getScene().getWindow().hide();
        util.showWindow("availableRoomMenu", "Available Rooms", Main.getToServer(), Main.getFromServer());
    }

}

