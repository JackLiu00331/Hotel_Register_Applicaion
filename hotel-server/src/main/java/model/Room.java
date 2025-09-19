package model;

public class Room {
    private int num;
    private String type;
    private double rate;

    public Room(int num, String type, double rate) {
        this.num = num;
        this.type = type;
        this.rate = rate;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
