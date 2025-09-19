package model;

public class Guest {
    private int g_id;
    private String g_title;
    private String g_firstname;
    private String g_lastName;
    private String g_address;
    private int g_phone;
    private String g_email;

    public Guest(int g_id, String g_title, String g_firstname, String g_lastName, String g_address, int g_phone, String g_email) {
        this.g_id = g_id;
        this.g_title = g_title;
        this.g_firstname = g_firstname;
        this.g_lastName = g_lastName;
        this.g_address = g_address;
        this.g_phone = g_phone;
        this.g_email = g_email;
    }

    public int getG_id() {
        return g_id;
    }

    public void setG_id(int g_id) {
        this.g_id = g_id;
    }

    public String getG_title() {
        return g_title;
    }

    public void setG_title(String g_title) {
        this.g_title = g_title;
    }

    public String getG_firstname() {
        return g_firstname;
    }

    public void setG_firstname(String g_firstname) {
        this.g_firstname = g_firstname;
    }

    public String getG_lastName() {
        return g_lastName;
    }

    public void setG_lastName(String g_lastName) {
        this.g_lastName = g_lastName;
    }

    public String getG_address() {
        return g_address;
    }

    public void setG_address(String g_address) {
        this.g_address = g_address;
    }

    public int getG_phone() {
        return g_phone;
    }

    public void setG_phone(int g_phone) {
        this.g_phone = g_phone;
    }

    public String getG_email() {
        return g_email;
    }

    public void setG_email(String g_email) {
        this.g_email = g_email;
    }
}
