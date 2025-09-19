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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Controller class for managing the admin login UI.
 */
public class adminLoginController {
    @FXML
    private PasswordField password;
    @FXML
    private TextField username;
    @FXML
    private Button login;
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
     * Handles the action when the admin login button is clicked.
     */
    @FXML
    public void handleAdminLoginAction() {
        String username = this.username.getText();
        String password = this.password.getText();

        performLogin(username, password);
    }

    /**
     * Sends the login credentials to the server and handles the response.
     *
     * @param username The admin username.
     * @param password The admin password.
     */
    private void performLogin(String username, String password) {
        try {
            toServer.writeUTF("LOGIN " + username + " " + password);
            toServer.flush();

            String response = fromServer.readUTF();
            handleLoginResponse(response);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handles the response from the server after login attempt.
     *
     * @param response The response from the server.
     */
    private void handleLoginResponse(String response) {
        System.out.println("Response from server: " + response);
        if ("SUCCESS".equals(response)) {
            util.showWindow("adminMenu", "Admin Menu", Main.getToServer(), Main.getFromServer());
            login.getScene().getWindow().hide();
        } else {
            util.showAlertError("Login Failed", "Wrong Username or Password!");
        }
    }
}

