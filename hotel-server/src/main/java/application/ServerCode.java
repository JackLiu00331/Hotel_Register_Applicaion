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

import controller.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.DbConnection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;

public class ServerCode extends Application {
    private Hashtable<Socket, DataOutputStream> outputStreams = new Hashtable<>();
    private TextArea ta = new TextArea();
    private static final int PORT = 6000;

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database
        DbConnection.initializeDatabase();

        // Start the server
        initializeServer();

        // Set up the UI
        ta.setWrapText(true);
        Scene scene = new Scene(new ScrollPane(ta), 400, 400);
        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to get output streams
    public Hashtable<Socket, DataOutputStream> getOutputStreams() {
        return outputStreams;
    }

    // Method to append messages to the text area
    public void appendToTextArea(String message) {
        Platform.runLater(() -> ta.appendText(message + "\n"));
    }

    // Method to initialize the server
    private void initializeServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                // Server started message
                appendToTextArea("MultiThreaded Server started at " + new Date());

                // Accept connections
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        // Connection established message
                        appendToTextArea("Connection from " + socket + " at " + new Date());

                        // Create output stream
                        DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                        outputStreams.put(socket, dout);

                        // Start a new thread for each client
                        new ServerController(this, socket);

                    } catch (IOException e) {
                        System.out.println("I/O error: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Server exception: " + e.getMessage());
            }
        }).start();
    }

    // Main method
    public static void main(String[] args) {
        launch(args);
    }
}
