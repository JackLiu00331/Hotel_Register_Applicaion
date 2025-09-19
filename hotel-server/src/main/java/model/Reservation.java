package model;

public class Reservation {
    private int r_id;
    private String r_bookDate;
    private String r_checkInDate;
    private String r_checkOutDate;

    public Reservation(int r_id, String r_bookDate, String r_checkInDate, String r_checkOutDate) {
        this.r_id = r_id;
        this.r_bookDate = r_bookDate;
        this.r_checkInDate = r_checkInDate;
        this.r_checkOutDate = r_checkOutDate;
    }

    public int getR_id() {
        return r_id;
    }

    public void setR_id(int r_id) {
        this.r_id = r_id;
    }

    public String getR_bookDate() {
        return r_bookDate;
    }

    public void setR_bookDate(String r_bookDate) {
        this.r_bookDate = r_bookDate;
    }

    public String getR_checkInDate() {
        return r_checkInDate;
    }

    public void setR_checkInDate(String r_checkInDate) {
        this.r_checkInDate = r_checkInDate;
    }

    public String getR_checkOutDate() {
        return r_checkOutDate;
    }

    public void setR_checkOutDate(String r_checkOutDate) {
        this.r_checkOutDate = r_checkOutDate;
    }
}
