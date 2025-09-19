package controller;

import application.ServerCode;
import model.*;
import model.DbConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class ServerController extends Thread {
    private final ServerCode server;
    private final Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private DbConnection dbConnection;

    public ServerController(ServerCode server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            dbConnection = new DbConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = input.readUTF();
                handleMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.getOutputStreams().remove(socket);
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Method to handle incoming messages and route them to appropriate handlers
    private void handleMessage(String message) {
        if (message.startsWith("LOGIN")) {
            handleLogin(message);
        } else if (message.startsWith("BOOK")) {
            handleBooking(message);
        } else if (message.startsWith("CREATE_GUEST")) {
            handleInformationRequest(message);
        } else if (message.startsWith("GET_BILL")) {
            handleBilling(message);
        } else if (message.startsWith("GET_AVAILABLE_ROOMS")) {
            handleAvailableRooms(message);
        } else if (message.startsWith("GET_ROOM_RATE")) {
            handleGetRoomRate(message);
        } else if (message.startsWith("LINK_GUEST_BOOKING")) {
            handleLinkGuestBooking(message);
        } else if (message.startsWith("CHECKOUT")) {
            handleCheckoutRequest(message);
        } else if (message.startsWith("CHECK_BOOKINGS")) {
            handleCheckBookingRequest(message);
        } else if (message.startsWith("CHECK_ROOMS")) {
            handleCheckRoomRequest(message);
        } else if (message.startsWith("DELETE_BOOKING")) {
            handleDeleteBooking(message);
        }
    }

    // Method to handle login requests
    private void handleLogin(String message) {
        // Parse the message to get credentials
        String[] credentials = parseMessage(message, 3, "LOGIN");
        if (credentials == null) return;

        // Extract username and password
        String id = credentials[1];
        String password = credentials[2];
        Login loginAttempt = new Login(id, password);

        // Check login credentials
        boolean loginSuccess = dbConnection.checkLogin(loginAttempt.getL_Id(), loginAttempt.getL_pwd());
        server.appendToTextArea("Login success: " + loginSuccess);

        // Send response based on login success
        if (loginSuccess) {
            sendResponse("SUCCESS");
        } else {
            sendResponse("FAILURE");
        }
    }

    // Method to handle booking requests
    private void handleBooking(String message) {
        // Parse the message to get booking details
        String[] parts = parseMessage(message, 9, "BOOK");
        if (parts == null) {
            sendResponse("FAILURE Invalid message format");
            return;
        }

        try {
            // Extract booking details
            LocalDate bookDate = LocalDate.parse(parts[1]);
            int numOfGuests = Integer.parseInt(parts[2]);
            String roomType = parts[3];
            String[] selectedRooms = {parts[4], parts[5], parts[6]};
            String checkInDate = parts[7];
            String checkOutDate = parts[8];

            // Create booking in the database
            int bookingId = dbConnection.createBooking(bookDate, numOfGuests, checkInDate, checkOutDate, selectedRooms);

            // Send response based on booking creation result
            if (bookingId != -1) {
                sendResponse("SUCCESS " + bookingId);
            } else {
                sendResponse("FAILURE Unable to create booking");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            sendResponse("FAILURE Invalid data format");
        }
    }

    // Method to handle guest information requests
    private void handleInformationRequest(String message) {
        // Parse the message to get guest information
        String[] parts = message.split("\\|");
        if (parts.length != 7 || !"CREATE_GUEST".equals(parts[0])) {
            sendResponse("FAILURE Invalid message format");
            return;
        }
        try {
            // Extract guest information
            String title = parts[1];
            String firstName = parts[2];
            String lastName = parts[3];
            String address = parts[4];
            String phone = parts[5];
            String email = parts[6];

            // Create guest in the database
            int guestId = dbConnection.createGuest(title, firstName, lastName, address, phone, email);

            // Send response based on guest creation result
            if (guestId != -1) {
                sendResponse("SUCCESS " + guestId);
            } else {
                sendResponse("FAILURE Unable to create guest");
            }
        } catch (Exception e) {
            sendResponse("FAILURE Invalid data format");
        }
    }

    // Method to handle linking guest to booking
    private void handleLinkGuestBooking(String message) {
        // Parse the message to get guest and booking IDs
        String[] parts = message.split(" ");
        if (parts.length == 3 && "LINK_GUEST_BOOKING".equals(parts[0])) {
            int guestId = Integer.parseInt(parts[1]);
            int bookingId = Integer.parseInt(parts[2]);

            // Link guest to booking in the database
            boolean success = dbConnection.linkGuestToBooking(guestId, bookingId);

            // Send response based on linking result
            if (success) {
                sendResponse("SUCCESS");
            } else {
                sendResponse("FAILURE");
            }
        }
    }

    // Method to handle billing requests
    private void handleBilling(String message) {
        // Parse the message to get booking ID
        String[] parts = parseMessage(message, 2, "GET_BILL");
        if (parts == null) {
            sendResponse("FAILURE Invalid message format");
            return;
        }
        try {
            // Extract booking ID
            int bookingId = Integer.parseInt(parts[1]);

            // Retrieve bill details from the database
            String result = dbConnection.getBillDetails(bookingId);

            // Send response based on billing result
            if (result != null) {
                sendResponse("SUCCESS " + result);
            } else {
                sendResponse("FAILURE No bill found with the given ID");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            sendResponse("FAILURE Invalid data format");
        }
    }

    // Method to handle checkout requests
    private void handleCheckoutRequest(String message) {
        // Parse the message to get booking ID and total amount
        String[] parts = parseMessage(message, 3, "CHECKOUT");
        if (parts == null) {
            sendResponse("FAILURE Invalid message format");
            return;
        }
        try {
            // Extract booking ID and total amount
            int bookId = Integer.parseInt(parts[1]);
            double total = Double.parseDouble(parts[2]);

            // Check if booking is already checked out
            if (dbConnection.isBookedCheckedOut(bookId)) {
                // Update bill total if different
                if (dbConnection.updateBillTotalIfDifferent(bookId, total)) {
                    sendResponse("SUCCESS Updated");
                } else {
                    sendResponse("SUCCESS No update needed");
                }
            } else {
                // Create bill and link it to booking in the database
                int billId = dbConnection.createBillAndLinkBooking(bookId, total);
                if (billId != -1) {
                    sendResponse("SUCCESS " + billId);
                } else {
                    sendResponse("FAILURE");
                }
            }
        } catch (NumberFormatException e) {
            sendResponse("FAILURE Invalid booking ID format");
        }
    }

    // Method to handle checking booking details requests
    private void handleCheckBookingRequest(String message) {
        // Parse the message to get required parts
        String[] parts = parseMessage(message, 1, "CHECK_BOOKINGS");
        if (parts == null) {
            sendResponse("FAILURE Invalid message format");
            return;
        }
        try {
            // Retrieve booking details from the database
            String result = dbConnection.getBookingDetails();
            if (result != null) {
                sendResponse("SUCCESS " + result);
            } else {
                sendResponse("FAILURE No bill found with the given ID");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            sendResponse("FAILURE Invalid data format");
        }
    }

    // Method to handle checking room details requests
    private void handleCheckRoomRequest(String message) {
        // Parse the message to get required parts
        String[] parts = parseMessage(message, 1, "CHECK_ROOMS");
        if (parts == null) {
            sendResponse("FAILURE Invalid message format");
            return;
        }
        try {
            // Retrieve room details from the database
            String result = dbConnection.getRoomDetails();
            if (result != null) {
                sendResponse("SUCCESS " + result);
            } else {
                sendResponse("FAILURE No room found");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            sendResponse("FAILURE Invalid data format");
        }
    }

    // Method to handle deleting booking requests
    private void handleDeleteBooking(String message) {
        // Parse the message to get booking ID
        String[] parts = message.split(" ");
        if (parts.length == 2 && "DELETE_BOOKING".equals(parts[0])) {
            int bookingId = Integer.parseInt(parts[1]);

            // Delete booking and related data from the database
            boolean success = dbConnection.deleteBookingAndRelatedData(bookingId);

            // Send response based on deletion result
            if (success) {
                sendResponse("SUCCESS");
            } else {
                sendResponse("FAILURE");
            }
        } else {
            sendResponse("FAILURE Invalid message format");
        }
    }

    // Method to handle available rooms requests
    private void handleAvailableRooms(String message) {
        // Parse the message to get required parts
        String[] parts = parseMessage(message, 3, "GET_AVAILABLE_ROOMS");
        if (parts == null) return;

        try {
            // Extract number of guests and room type
            int numOfGuests = Integer.parseInt(parts[1]);
            String roomType = parts[2];
            // Log the request
            logRequest("Received available rooms request for:", roomType + " room", "For " + String.valueOf(numOfGuests) + " People");
            // Retrieve available rooms from the database
            List<Integer> availableRoomIds = dbConnection.getAvailableRooms(numOfGuests, roomType);
            // Convert room IDs to a string and send response
            String roomIdsString = availableRoomIds.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            sendResponse(roomIdsString);
        } catch (NumberFormatException e) {
            sendResponse("FAILURE Invalid data format");
        }
    }

    // Method to handle room rate requests
    private void handleGetRoomRate(String message) {
        // Parse the message to get required parts
        String[] parts = parseMessage(message, 3, "GET_ROOM_RATE");
        if (parts == null) return;

        try {
            // Extract number of guests and room type
            int numOfGuests = Integer.parseInt(parts[1]);
            String roomType = parts[2];
            // Log the request
            logRequest("Received room rate request for:", roomType);
            // Retrieve room rate from the database
            double rate = dbConnection.getRoomRate(roomType);
            // Send response with room rate
            sendResponse(String.valueOf(rate));
        } catch (NumberFormatException e) {
            sendResponse("FAILURE Invalid data format");
        }
    }

    // Method to parse incoming messages
    private String[] parseMessage(String message, int expectedParts, String command) {
        String[] parts = message.split(" ");
        if (parts.length == expectedParts && command.equals(parts[0])) {
            return parts;
        } else {
            handleIncorrectMessageFormat();
            return null;
        }
    }

    // Method to log requests
    private void logRequest(String... parts) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String part : parts) {
            joiner.add(part);
        }
        String logMessage = joiner.toString();
        server.appendToTextArea(logMessage);
        System.out.println(logMessage);
    }

    // Method to send response to client
    private void sendResponse(String response) {
        try {
            output.writeUTF(response);
            output.flush();
        } catch (IOException e) {
            logError("Error sending response: ", e);
        }
    }

    // Method to handle incorrect message format
    private void handleIncorrectMessageFormat() {
        String errorMessage = "Error: Incorrect message format.";
        server.appendToTextArea(errorMessage);
        sendResponse(errorMessage);
    }

    // Method to log errors
    private void logError(String message, Exception e) {
        String fullMessage = message + e.getMessage();
        server.appendToTextArea(fullMessage);
        System.err.println(fullMessage);
    }

}
