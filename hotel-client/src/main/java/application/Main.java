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
package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Main class to launch the application.
 */
public class Main extends Application {
    private static DataOutputStream toServer = null;
    private static DataInputStream fromServer = null;

    /**
     * Main method to launch the application.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start method to initialize the primary stage and connect to the server.
     *
     * @param primaryStage The primary stage of the application.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/homePage.fxml"));
            Application.setUserAgentStylesheet("/style/primer-dark.css");
            primaryStage.setTitle("Inventory Management System");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            connectToServer();
        } catch (Exception e) {
            // Print the stack trace to the console if an exception occurs
            e.printStackTrace();
        }
    }

    /**
     * Connects to the server on localhost and initializes input and output streams.
     */
    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 6000);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Getter method to retrieve the output stream to the server.
     *
     * @return The output stream to the server.
     */
    public static DataOutputStream getToServer() {
        return toServer;
    }

    /**
     * Getter method to retrieve the input stream from the server.
     *
     * @return The input stream from the server.
     */
    public static DataInputStream getFromServer() {
        return fromServer;
    }
}