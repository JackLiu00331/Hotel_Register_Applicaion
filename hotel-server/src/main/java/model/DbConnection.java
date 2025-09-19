package model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbConnection {

    // Database URL
    private static final String URL = "jdbc:sqlite:src/main/resources/database.db";

    // Establishes a database connection
    private static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    // Checks login credentials
    public boolean checkLogin(String username, String password) {
        String sql = "SELECT Login_pswd FROM Login WHERE Login_ID = ?";

        return this.executeQuery(sql, rs -> {
            if (rs.next()) {
                String retrievedPassword = rs.getString("Login_pswd");
                return retrievedPassword.equals(password);
            }
            return false;
        }, username);
    }


    // Retrieves a list of available rooms based on the number of guests and room type
    public List<Integer> getAvailableRooms(int numOfGuests, String roomType) {
        String sql = "SELECT Room_ID FROM Room WHERE Room_type = ? AND Is_Available = 1";
        return executeQuery(sql, rs -> {
            List<Integer> roomIds = new ArrayList<>();
            while (rs.next()) {
                roomIds.add(rs.getInt("Room_ID"));
            }
            return roomIds;
        }, roomType);
    }

    // Retrieves bill details for a booking
    public String getBillDetails(int bookingId) {
        String sql = "SELECT r.Book_ID, g.First_name || ' ' || g.Last_name AS Name, " +
                "GROUP_CONCAT(rr.Room_ID) AS RoomIds, ro.Room_type, ro.Rate, " +
                "(julianday(r.Check_out_Date) - julianday(r.Check_in_Date)) AS NoOfDays " +
                "FROM Reservation r " +
                "JOIN Guest g ON r.Guest_ID = g.Guest_ID " +
                "JOIN Reservation_Room rr ON r.Book_ID = rr.Book_ID " +
                "JOIN Room ro ON rr.Room_ID = ro.Room_ID " +
                "WHERE r.Book_ID = ? " +
                "GROUP BY r.Book_ID, ro.Room_type;";

        return executeQuery(sql, rs -> {
            if (rs.next()) {
                String bookId = rs.getString("Book_ID");
                String guestName = rs.getString("Name");
                String roomIds = "[" + rs.getString("RoomIds") + "]";
                String roomType = rs.getString("Room_type");
                double rate = rs.getDouble("Rate");
                int noOfDays = rs.getInt("NoOfDays");

                int numOfRooms = roomIds.split(",").length;
                double total = rate * numOfRooms * noOfDays; // Total is rate multiplied by the number of rooms and number of days

                return String.format("%s %s %s %s %.2f %.2f", bookId, guestName, roomIds, roomType, rate, total);
            }
            return null;
        }, bookingId);
    }

    // Checks if a booking is checked out
    public boolean isBookedCheckedOut(int bookId) {
        String sql = "SELECT COUNT(*) FROM Bill WHERE Book_ID = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, bookId);
    }

    // Creates a bill and links it to a booking
    public int createBillAndLinkBooking(int bookId, double totalAmount) {
        String insertBillSql = "INSERT INTO Bill (Book_ID, Amount_toPay) VALUES (?, ?)";
        return executeUpdate(insertBillSql, bookId, totalAmount);
    }

    // Updates bill total amount if different
    public boolean updateBillTotalIfDifferent(int bookId, double newTotalAmount) {
        String checkSql = "SELECT Amount_toPay FROM Bill WHERE Book_ID = ?";
        Double existingTotal = executeQuery(checkSql, rs -> rs.next() ? rs.getDouble("Amount_toPay") : null, bookId);

        if (existingTotal != null && Double.compare(existingTotal, newTotalAmount) != 0) {
            String updateSql = "UPDATE Bill SET Amount_toPay = ? WHERE Book_ID = ?";
            int affectedRows = executeUpdateSame(updateSql, newTotalAmount, bookId);
            System.out.println("Affected rows: " + affectedRows);
            return affectedRows > 0;
        } else if (existingTotal != null) {
            return false;
        }
        return false;
    }

    // Retrieves room rate
    public double getRoomRate(String roomType) {
        String sql = "SELECT Rate FROM Room WHERE Room_type = ? AND Is_Available = 1 LIMIT 1";
        return executeQuery(sql, rs -> rs.next() ? rs.getDouble("Rate") : 0.0, roomType);
    }

    // Creates a booking
    public int createBooking(LocalDate bookDate, int numOfGuests, String checkInDate, String checkOutDate, String... selectedRooms) {
        String insertSql = "INSERT INTO Reservation (Book_date, Check_in_Date, Check_out_Date, NumOfPeople) VALUES (?, ?, ?, ?)";
        int bookingId = executeUpdate(insertSql, bookDate, checkInDate, checkOutDate, numOfGuests);

        if (bookingId != -1) {
            System.out.println(Arrays.toString(selectedRooms));
            for (String room : selectedRooms) {
                System.out.println(room);
                if (!room.equals("null") && !room.trim().isEmpty()) {
                    String updateSql = "UPDATE Room SET Is_Available = 0 WHERE Room_ID = ?";
                    executeUpdate(updateSql, room);

                    if (!isRoomLinked(bookingId, room)) {
                        linkRoomToBooking(bookingId, room);
                    }
                }
            }
        }

        return bookingId;
    }

    // Creates a guest
    public int createGuest(String title, String firstName, String lastName, String address, String phoneNum, String email) {
        String sql = "INSERT INTO Guest (Title, First_name, Last_name, Address, Phone, Email) VALUES (?, ?, ?, ?, ?, ?)";
        int guestID = executeUpdate(sql, title, firstName, lastName, address, phoneNum, email);
        return guestID;
    }

    // Links a room to a booking
    private void linkRoomToBooking(int bookingId, String roomNumber) {
        String linkSql = "INSERT INTO Reservation_Room (Book_ID, Room_ID) VALUES (?, ?)";
        executeUpdate(linkSql, bookingId, roomNumber);
    }

    // Checks if a room is linked to a booking
    private boolean isRoomLinked(int bookingId, String roomNumber) {
        if (roomNumber == null) {
            return false;
        }
        String checkSql = "SELECT COUNT(*) FROM Reservation_Room WHERE Book_ID = ? AND Room_ID = ?";
        return executeQuery(checkSql, rs -> rs.next() && rs.getInt(1) > 0, bookingId, roomNumber);
    }

    // Links a guest to a booking
    public boolean linkGuestToBooking(int guestId, int bookingId) {
        String sql = "UPDATE Reservation SET Guest_ID = ? WHERE Book_ID = ?";

        try (PreparedStatement pstmt = this.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, guestId);
            pstmt.setInt(2, bookingId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error linking guest to booking: " + e.getMessage());
        }
        return false;
    }

    // Retrieves booking details
    public String getBookingDetails() {
        String sql = "SELECT r.Book_ID, " +
                "g.First_name || ' ' || g.Last_name AS GuestName, " +
                "ro.Room_type, " +
                "GROUP_CONCAT(ro.Room_ID) AS RoomIds, " +
                "julianday(r.Check_out_Date) - julianday(r.Check_in_Date) AS NoOfDays " +
                "FROM Reservation r " +
                "JOIN Guest g ON r.Guest_ID = g.Guest_ID " +
                "JOIN Reservation_Room rr ON r.Book_ID = rr.Book_ID " +
                "JOIN Room ro ON rr.Room_ID = ro.Room_ID " +
                "GROUP BY r.Book_ID " +
                "ORDER BY r.Book_ID;";

        StringBuilder allBookings = new StringBuilder();
        boolean hasResults = executeQuery(sql, rs -> {
            while (rs.next()) {
                if (!allBookings.isEmpty()) {
                    allBookings.append("; "); // Separator between different bookings
                }
                int bookId = rs.getInt("Book_ID");
                String guestName = rs.getString("GuestName");
                String roomType = rs.getString("Room_type");
                String roomIds = rs.getString("RoomIds");
                int noOfDays = rs.getInt("NoOfDays");

                allBookings.append(String.format("%d %s %s %s %d", bookId, guestName, roomType, roomIds, noOfDays));
            }
            return !allBookings.isEmpty(); // Return true if we have results
        });

        return hasResults ? allBookings.toString() : "NO_BOOKINGS";
    }

    // Retrieves room details
    public String getRoomDetails() {
        String sql = "SELECT Room_ID, Room_type\n" +
                "FROM Room\n" +
                "WHERE Is_Available = 1;";
        StringBuilder allRooms = new StringBuilder();
        boolean hasResults = executeQuery(sql, rs -> {
            while (rs.next()) {
                if (!allRooms.isEmpty()) {
                    allRooms.append("; ");
                }
                int roomId = rs.getInt("Room_ID");
                String roomType = rs.getString("Room_type");

                allRooms.append(String.format("%d %s", roomId, roomType));
            }
            return !allRooms.isEmpty();
        });

        return hasResults ? allRooms.toString() : "NO_ROOMS";
    }

    // Deletes a booking and related data
    public boolean deleteBookingAndRelatedData(int bookingId) {
        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        PreparedStatement pstmt4 = null;
        boolean success = false;

        String sqlDeleteReservationRoom = "DELETE FROM Reservation_Room WHERE Book_ID = ?";
        String sqlDeleteBill = "DELETE FROM Bill WHERE Book_ID = ?";
        String sqlDeleteReservation = "DELETE FROM Reservation WHERE Book_ID = ?";
        String sqlUpdateAvailable = "UPDATE Room SET Is_Available = 1 WHERE Room_ID IN (SELECT Room_ID FROM Reservation_Room WHERE Book_ID = ?)";

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Update Room availability
            pstmt4 = conn.prepareStatement(sqlUpdateAvailable);
            pstmt4.setInt(1, bookingId);
            pstmt4.executeUpdate();

            // Delete from Reservation_Room
            pstmt1 = conn.prepareStatement(sqlDeleteReservationRoom);
            pstmt1.setInt(1, bookingId);
            pstmt1.executeUpdate();

            // Delete from Bill
            pstmt2 = conn.prepareStatement(sqlDeleteBill);
            pstmt2.setInt(1, bookingId);
            pstmt2.executeUpdate();

            // Delete from Reservation
            pstmt3 = conn.prepareStatement(sqlDeleteReservation);
            pstmt3.setInt(1, bookingId);
            int affectedRows = pstmt3.executeUpdate();

            conn.commit(); // Commit transaction
            success = affectedRows > 0;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            // Attempt to close all resources
            try {
                if (pstmt1 != null) pstmt1.close();
                if (pstmt2 != null) pstmt2.close();
                if (pstmt3 != null) pstmt3.close();
                if (pstmt4 != null) pstmt4.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit to true
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return success;
    }


    // Executes a query with a handler
    public <T> T executeQuery(String sql, ThrowableFunction<ResultSet, T> handler, Object... parameters) {
        try (PreparedStatement pstmt = this.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return handler.apply(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error during database query: " + e.getMessage());
        }
        return null;
    }

    // Executes an update query
    public int executeUpdate(String sql, Object... parameters) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error during database update: " + e.getMessage());
        }
        return -1;
    }

    // Executes an update query without returning generated keys
    public int executeUpdateSame(String sql, Object... parameters) {
        int affectedRows = 0;
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
            affectedRows = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error during database update: " + e.getMessage());
        }
        return affectedRows;
    }


    // Functional interface for a throwable function
    @FunctionalInterface
    public interface ThrowableFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    // Initializes the database
    public static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            String createLoginTable = "CREATE TABLE IF NOT EXISTS Login (" +
                    "Login_ID INTEGER PRIMARY KEY, " +
                    "Login_pswd TEXT NOT NULL)";
            statement.execute(createLoginTable);

            String createRoomTable = "CREATE TABLE IF NOT EXISTS Room (\n" +
                    "    Room_ID INTEGER PRIMARY KEY,\n" +
                    "    Room_type TEXT NOT NULL,\n" +
                    "    Rate DOUBLE NOT NULL,\n" +
                    "    Is_Available BOOLEAN NOT NULL DEFAULT 1);";
            statement.execute(createRoomTable);

            String createGuestTable = "CREATE TABLE IF NOT EXISTS Guest (\n" +
                    "    Guest_ID INTEGER PRIMARY KEY,\n" +
                    "    Title TEXT,\n" +
                    "    First_name TEXT NOT NULL,\n" +
                    "    Last_name TEXT NOT NULL,\n" +
                    "    Address TEXT,\n" +
                    "    Phone INTEGER,\n" +
                    "    Email TEXT);";
            statement.execute(createGuestTable);

            String createReservationTable = "CREATE TABLE IF NOT EXISTS Reservation (\n" +
                    "    Book_ID INTEGER PRIMARY KEY,\n" +
                    "    Guest_ID INTEGER,\n" +
                    "    Book_date DATE NOT NULL,\n" +
                    "    Check_in_Date DATE NOT NULL,\n" +
                    "    Check_out_Date DATE NOT NULL,\n" +
                    "    NumOfPeople INTEGER NOT NULL,\n" +
                    "    FOREIGN KEY (Guest_ID) REFERENCES Guest(Guest_ID)\n" +
                    ");";
            statement.execute(createReservationTable);
            String createReservation_RoomTable = "CREATE TABLE IF NOT EXISTS Reservation_Room (\n" +
                    "    Book_ID INTEGER NOT NULL,\n" +
                    "    Room_ID INTEGER NOT NULL,\n" +
                    "    PRIMARY KEY (Book_ID, Room_ID),\n" +
                    "    FOREIGN KEY (Book_ID) REFERENCES Reservation(Book_ID),\n" +
                    "    FOREIGN KEY (Room_ID) REFERENCES Room(Room_ID)\n" +
                    ");";
            statement.execute(createReservation_RoomTable);
            String createBillTable = "CREATE TABLE IF NOT EXISTS Bill(\n" +
                    "    Bill_ID INTEGER PRIMARY KEY,\n" +
                    "    Book_ID INTEGER NOT NULL,\n" +
                    "    Amount_toPay DOUBLE NOT NULL,\n" +
                    "    FOREIGN KEY (Book_ID) REFERENCES Reservation(Book_ID)\n" +
                    ");";
            statement.execute(createBillTable);


            String insertAdmins = "INSERT OR IGNORE INTO Login (Login_ID, Login_pswd) VALUES " +
                    "(155, 'cliu155'), " +
                    "(2, 'adminpassword2')";
            statement.execute(insertAdmins);
            String insertRooms = "INSERT OR IGNORE INTO Room (Room_ID, Room_type, Rate, Is_Available) VALUES\n" +
                    "(101, 'Single', 100.0, 1),\n" +
                    "(102, 'Single', 100.0, 1),\n" +
                    "(103, 'Single', 100.0, 1),\n" +
                    "(104, 'Single', 100.0, 1),\n" +
                    "(105, 'Single', 100.0, 1),\n" +
                    "(106, 'Single', 100.0, 1),\n" +
                    "(201, 'Double', 150.0, 0),\n" +
                    "(202, 'Double', 150.0, 1),\n" +
                    "(203, 'Double', 150.0, 1),\n" +
                    "(204, 'Double', 150.0, 1),\n" +
                    "(205, 'Double', 150.0, 1),\n" +
                    "(206, 'Double', 150.0, 1),\n" +
                    "(301, 'Deluxe', 200.0, 1),\n" +
                    "(302, 'Deluxe', 200.0, 0),\n" +
                    "(303, 'Deluxe', 200.0, 1),\n" +
                    "(304, 'Deluxe', 200.0, 1),\n" +
                    "(305, 'Deluxe', 200.0, 1),\n" +
                    "(306, 'Deluxe', 200.0, 1),\n" +
                    "(401, 'Penthouse', 500.0, 1),\n" +
                    "(402, 'Penthouse', 500.0, 1),\n" +
                    "(403, 'Penthouse', 500.0, 1),\n" +
                    "(404, 'Penthouse', 500.0, 1),\n" +
                    "(405, 'Penthouse', 500.0, 1),\n" +
                    "(406, 'Penthouse', 500.0, 1);";

            statement.execute(insertRooms);
            String insertGuests = "INSERT OR IGNORE INTO Guest (Guest_ID, Title, First_name, Last_name, Address, Phone, Email) VALUES\n" +
                    "(1, 'Mr.', 'John', 'Doe', '123 Main St', 5551234, 'johndoe@email.com'),\n" +
                    "(2, 'Ms.', 'Jane', 'Smith', '456 Elm St', 5555678, 'janesmith@email.com');";
            statement.execute(insertGuests);
            String insertReservation = "INSERT OR IGNORE INTO Reservation (Book_ID, Guest_ID, Book_date, Check_in_Date, Check_out_Date,NumOfPeople) VALUES\n" +
                    "(1, 1, '2023-10-01', '2023-10-10', '2023-10-15', 2),\n" +
                    "(2, 2, '2023-11-01', '2023-11-05', '2023-11-10', 1);";
            statement.execute(insertReservation);
            String insertBill = "INSERT OR IGNORE INTO Bill (Bill_ID, Book_ID, Amount_toPay) VALUES\n" +
                    "(1, 1, 150.0),\n" +
                    "(2, 2, 200.0);";
            statement.execute(insertBill);
            String insertReservation_Room = "INSERT OR IGNORE INTO Reservation_Room (Book_ID, Room_ID) VALUES\n" +
                    "(1, 201),\n" +
                    "(2, 302);";
            statement.execute(insertReservation_Room);


            System.out.println("Database initialized successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing the database.");
        }
    }
}
