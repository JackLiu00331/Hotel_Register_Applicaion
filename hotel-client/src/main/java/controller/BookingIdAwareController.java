package controller;


import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Interface for controllers that need to be aware of the booking ID and input/output streams for communication with the server.
 */
public interface BookingIdAwareController {
    /**
     * Sets the booking ID.
     *
     * @param bookingId The booking ID to be set.
     */
    void setBookingId(int bookingId);

    /**
     * Sets the input/output streams for communication with the server.
     *
     * @param toServer   The output stream to the server.
     * @param fromServer The input stream from the server.
     */
    void setStreams(DataOutputStream toServer, DataInputStream fromServer);
}


