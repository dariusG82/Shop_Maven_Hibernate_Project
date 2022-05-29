package dariusG82.accounting.finance;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cash_records")
public class CashRecord {

    @Id
    private int recordID;
    private double amount;
    private String recordDate;
    private int orderNumber;
    private String orderSeries;
    private String sellerId;

    public CashRecord() {
    }
    public CashRecord(int recordID, double amount, String recordDate, int orderNumber, String orderSeries, String sellerId) {
        this.recordID = recordID;
        this.amount = amount;
        this.recordDate = recordDate;
        this.orderNumber = orderNumber;
        this.orderSeries = orderSeries;
        this.sellerId = sellerId;
    }

    public CashRecord(double amount, String recordDate, int orderNumber, String orderSeries, String sellerId) {
        this.recordID = 0;
        this.amount = amount;
        this.recordDate = recordDate;
        this.orderNumber = orderNumber;
        this.orderSeries = orderSeries;
        this.sellerId = sellerId;
    }

    public int getRecordID() {
        return recordID;
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderSeries() {
        return orderSeries;
    }

    public void setOrderSeries(String orderSeries) {
        this.orderSeries = orderSeries;
    }

    public String getSellerId() {
        return sellerId;
    }

    @Override
    public String toString() {
        return "CashRecord{" +
                "recordID=" + recordID +
                ", amount=" + amount +
                ", recordDate='" + recordDate + '\'' +
                ", orderSeries='" + orderSeries + '\'' +
                ", orderNumber=" + orderNumber +
                ", sellerId='" + sellerId + '\'' +
                "}\n";
    }
}
