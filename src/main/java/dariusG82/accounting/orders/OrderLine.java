package dariusG82.accounting.orders;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "order_lines")
public class OrderLine implements Serializable {

    @Id
    private String orderSeries;
    @Id
    private long orderNumber;
    @Id
    private int orderLineNumber;
    @Column(name = "itemId")
    private long itemNr;
    private int lineQuantity;

    private double lineAmount;

    public OrderLine(){}

    public OrderLine(String orderSeries, long orderNumber, int orderLineNumber, long itemNr, int lineQuantity, double lineAmount) {
        this.orderSeries = orderSeries;
        this.orderNumber = orderNumber;
        this.orderLineNumber = orderLineNumber;
        this.itemNr = itemNr;
        this.lineQuantity = lineQuantity;
        this.lineAmount = lineAmount;
    }

    public String getOrderSeries() {
        return orderSeries;
    }

    public void setOrderSeries(String orderSeries) {
        this.orderSeries = orderSeries;
    }

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNr) {
        this.orderNumber = orderNr;
    }

    public int getOrderLineNumber() {
        return orderLineNumber;
    }

    public void setOrderLineNumber(int orderLineNumber) {
        this.orderLineNumber = orderLineNumber;
    }

    public long getItemID() {
        return itemNr;
    }

    public void setItemID(long itemNr) {
        this.itemNr = itemNr;
    }

    public int getLineQuantity() {
        return lineQuantity;
    }

    public void setLineQuantity(int lineQuantity) {
        this.lineQuantity = lineQuantity;
    }

    public double getLineAmount() {
        return lineAmount;
    }

    public void setLineAmount(double lineAmount) {
        this.lineAmount = lineAmount;
    }
}

