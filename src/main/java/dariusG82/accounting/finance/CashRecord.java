package dariusG82.accounting.finance;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cash_records")
public class CashRecord {

    @Id
    private long recordID;
    private double amount;
    public String recordDate;
    private long orderNumber;
    private String orderSeries;
    private String sellerId;

    public CashRecord() {
    }
    public CashRecord(long recordID, double amount, String recordDate, long orderNumber, String orderSeries, String sellerId) {
        this.recordID = recordID;
        this.amount = amount;
        this.recordDate = recordDate;
        this.orderNumber = orderNumber;
        this.orderSeries = orderSeries;
        this.sellerId = sellerId;
    }

    public CashRecord(double amount, String recordDate, long orderNumber, String orderSeries, String sellerId) {
        this.recordID = 0;
        this.amount = amount;
        this.recordDate = recordDate;
        this.orderNumber = orderNumber;
        this.orderSeries = orderSeries;
        this.sellerId = sellerId;
    }

    public long getRecordID() {
        return recordID;
    }

    public void setRecordID(long recordID) {
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

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNumber) {
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

    public void updateAmount(double amount){
        this.amount += amount;
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
