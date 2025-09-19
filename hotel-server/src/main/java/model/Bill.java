package model;

public class Bill {
    private int bill_id;
    private double bill_total;

    public Bill(int bill_id, double bill_total) {
        this.bill_id = bill_id;
        this.bill_total = bill_total;
    }

    public int getBill_id() {
        return bill_id;
    }

    public void setBill_id(int bill_id) {
        this.bill_id = bill_id;
    }

    public double getBill_total() {
        return bill_total;
    }

    public void setBill_total(double bill_total) {
        this.bill_total = bill_total;
    }
}
