package controller;

import application.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import model.Util;
import javafx.scene.control.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class custBookingController {
    @FXML
    private Label availableRoomsLabel;
    @FXML
    private Label rateLabel;
    @FXML
    private DatePicker bookingDate;
    @FXML
    private DatePicker checkIn;
    @FXML
    private DatePicker checkOut;
    @FXML
    private ChoiceBox numOfGuests;
    @FXML
    private ComboBox roomType;
    @FXML
    private ComboBox roomComboBox1;
    @FXML
    private ComboBox roomComboBox2;
    @FXML
    private ComboBox roomComboBox3;
    @FXML
    private Button book;
    // Data streams for communication with server
    private DataOutputStream toServer = null;
    private DataInputStream fromServer = null;

    // Utility instance
    Util util = new Util();

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Initialize number of guests and room type lists
        ObservableList<Integer> numberOfGuests =
                FXCollections.observableArrayList(
                        1, 2, 3, 4, 5, 6
                );
        ObservableList<String> roomTypeValue =
                FXCollections.observableArrayList(
                        "Single", "Double", "Deluxe", "Penthouse"
                );
        bookingDate.setValue(LocalDate.now());
        numOfGuests.setItems(numberOfGuests);
        roomType.setItems(roomTypeValue);
        // Populate rooms and limit date selection
        setRooms();
        limitDate();

    }

    /**
     * Sets the data streams for server communication.
     *
     * @param toServerStream   DataOutputStream for sending data to server
     * @param fromServerStream DataInputStream for receiving data from server
     */
    public void setStreams(DataOutputStream toServerStream, DataInputStream fromServerStream) {
        toServer = toServerStream;
        fromServer = fromServerStream;
    }

    private void updateSecondChoiceBox(String selectedRoom) {
        List<String> currentRooms = new ArrayList<>(roomComboBox1.getItems());
        currentRooms.remove(selectedRoom);
        roomComboBox2.setItems(FXCollections.observableArrayList(currentRooms));
    }

    /**
     * Updates the second choice box based on selected room.
     *
     * @param selectedRoom Selected room from first choice box
     */
    private void updateThirdChoiceBox(String selectedRoom) {
        List<String> currentRooms = new ArrayList<>(roomComboBox2.getItems());
        currentRooms.remove(selectedRoom);
        roomComboBox3.setItems(FXCollections.observableArrayList(currentRooms));
    }

    private void limitDate() {
        checkIn.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });
        checkIn.setValue(LocalDate.now());

        checkIn.valueProperty().addListener((obs, oldVal, newVal) -> {
            checkOut.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.compareTo(newVal) <= 0);
                }
            });
            if (checkOut.getValue() != null && !checkOut.getValue().isAfter(newVal)) {
                checkOut.setValue(null);
            }
        });

        checkOut.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = checkIn.getValue();
                setDisable(empty || (checkIn == null || date.compareTo(checkInDate) <= 0));
            }
        });

        checkOut.valueProperty().addListener(((observableValue, localDate, t1) -> {
            if (t1 != null) {
                getRoomRate();
            }
        }));
    }

    private void setRooms() {
        numOfGuests.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                getRooms();
            }
        });
        roomType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                getRooms();
            }
        });
        roomComboBox1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateSecondChoiceBox((String) newValue);
            }
        });
        roomComboBox2.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateThirdChoiceBox((String) newValue);
            }
        });
    }

    // Methods for updating choice boxes and limiting date selection...

    /**
     * Retrieves available rooms based on selected criteria.
     */
    private void getRooms() {
        roomComboBox1.setValue(null);
        roomComboBox2.setValue(null);
        roomComboBox3.setValue(null);
        if (numOfGuests.getValue() != null && roomType.getValue() != null) {
            try {
                int guests = (int) numOfGuests.getValue();
                String type = (String) roomType.getValue();
                toServer.writeUTF("GET_AVAILABLE_ROOMS " + guests + " " + type);
                toServer.flush();
                String response = fromServer.readUTF();
                Platform.runLater(() -> {

                    String[] responseParts = response.split(",");
                    availableRoomsLabel.setText(String.valueOf(responseParts.length));
                    List<String> roomIds = Arrays.asList(responseParts).subList(0, responseParts.length);
                    roomComboBox1.setItems(FXCollections.observableArrayList(roomIds));
                    if (guests == 3 && "Single".equals(type)) {
                        roomComboBox2.setDisable(false);
                        roomComboBox2.setItems(roomComboBox1.getItems());
                    } else if (guests >= 4 && "Single".equals(type)) {
                        roomComboBox2.setDisable(false);
                        roomComboBox2.setItems(roomComboBox1.getItems());
                        roomComboBox3.setDisable(false);
                        roomComboBox3.setItems(roomComboBox2.getItems());
                    } else if (guests >= 4 && "Double".equals(type)) {
                        roomComboBox2.setDisable(false);
                        roomComboBox2.setItems(roomComboBox1.getItems());
                    } else if (guests >= 4 && "Delux".equals(type)) {
                        roomComboBox2.setDisable(false);
                        roomComboBox2.setItems(roomComboBox1.getItems());
                    } else {
                        roomComboBox2.setDisable(true);
                        roomComboBox2.setValue(null);
                        roomComboBox3.setDisable(true);
                        roomComboBox3.setValue(null);
                    }

                });


            } catch (IOException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                });
            }
        }

    }

    /**
     * Retrieves room rate based on selected criteria and dates.
     */
    private void getRoomRate() {
        if (numOfGuests.getValue() != null && roomType.getValue() != null && checkIn.getValue() != null && checkOut.getValue() != null) {
            try {
                int guests = (int) numOfGuests.getValue();
                String type = (String) roomType.getValue();
                toServer.writeUTF("GET_ROOM_RATE " + guests + " " + type);
                toServer.flush();

                String response = fromServer.readUTF();
                Platform.runLater(() -> {
                    rateLabel.setText(response);
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    // 在这里处理错误，例如显示错误信息
                });
            }
        }

    }

    /**
     * Handles the booking action.
     */
    @FXML
    public void handleBookAction() {
        String bookDate = String.valueOf(this.bookingDate.getValue());
        Integer numOfGuests = (Integer) this.numOfGuests.getValue();
        String roomsType = (String) this.roomType.getValue();
        String selectedRoom = (String) this.roomComboBox1.getValue();
        String selectedRoom2 = (String) this.roomComboBox2.getValue();
        String selectedRoom3 = (String) this.roomComboBox3.getValue();

        LocalDate checkInDate = checkIn.getValue();
        LocalDate checkOutDate = checkOut.getValue();


        if (bookDate == null || bookDate.isEmpty()) {
            util.showAlertWarning("Warning", "Please pick the book date!");
        } else if (numOfGuests == null) {
            util.showAlertWarning("Warning", "Please pick the number of people!");
        } else if (roomsType == null || roomsType.isEmpty()) {
            util.showAlertWarning("Warning", "Please pick the type of room!");
        } else if (selectedRoom == null || selectedRoom.isEmpty()) {
            util.showAlertWarning("Warning", "Please pick the number of room!");
        } else if (!roomComboBox2.isDisable() && (selectedRoom2 == null || selectedRoom2.isEmpty())) {
            util.showAlertWarning("Warning", "Please pick the number of second room!");
        } else if (!roomComboBox3.isDisable() && (selectedRoom3 == null || selectedRoom3.isEmpty())) {
            util.showAlertWarning("Warning", "Please pick the number of third room!");
        } else if (checkInDate == null) {
            util.showAlertWarning("Warning", "Please pick the date of check-in!");
        } else if (checkOutDate == null) {
            util.showAlertWarning("Warning", "Please pick the date of check-out!");
        } else {
            String checkInDateS = String.valueOf(checkInDate);
            String checkOutDates = String.valueOf(checkOutDate);
            performBooking(bookDate, numOfGuests, roomsType, selectedRoom, selectedRoom2, selectedRoom3, checkInDateS, checkOutDates);
        }

    }

    /**
     * Performs the booking based on provided details.
     *
     * @param bookDate      Date of booking
     * @param numOfGuests   Number of guests
     * @param roomType      Type of room
     * @param selectedRoom  Selected room
     * @param selectedRoom2 Selected second room
     * @param selectedRoom3 Selected third room
     * @param checkInDate   Check-in date
     * @param checkOutDate  Check-out date
     */
    private void performBooking(String bookDate, Integer numOfGuests, String roomType, String selectedRoom, String selectedRoom2, String selectedRoom3, String checkInDate, String checkOutDate) {
        String bookingDetails = String.format("BOOK %s %d %s %s %s %s %s %s",
                bookDate, numOfGuests, roomType, selectedRoom, selectedRoom2, selectedRoom3, checkInDate, checkOutDate);
        try {
            toServer.writeUTF(bookingDetails);
            toServer.flush();

            String response = fromServer.readUTF();
            handleBookingResponse(response);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Handles the booking response from the server.
     *
     * @param response Response from server
     */
    private void handleBookingResponse(String response) {
        System.out.println("Response from server: " + response);
        if (response.startsWith("SUCCESS")) {
            String[] responseParts = response.split(" ");
            int bookingId = Integer.parseInt(responseParts[1]);
            book.getScene().getWindow().hide();
            util.showWindowWithBookingId("custInfo", "Customer Information", bookingId, Main.getToServer(), Main.getFromServer());
        } else {
            util.showAlertError("Booking Failed", "Could not complete the booking. Please try again.");
        }
    }


}
