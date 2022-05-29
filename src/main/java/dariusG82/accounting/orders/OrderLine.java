package dariusG82.accounting.orders;

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
    private int orderNumber;
    @Id
    private int orderLineNumber;

    private int itemID;
    private int lineQuantity;

    private double lineAmount;

    public OrderLine(){}

    public OrderLine(String orderSeries, int orderNumber, int orderLineNumber, int itemID, int lineQuantity, double lineAmount) {
        this.orderSeries = orderSeries;
        this.orderNumber = orderNumber;
        this.orderLineNumber = orderLineNumber;
        this.itemID = itemID;
        this.lineQuantity = lineQuantity;
        this.lineAmount = lineAmount;
    }

    public String getOrderSeries() {
        return orderSeries;
    }

    public void setOrderSeries(String orderSeries) {
        this.orderSeries = orderSeries;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNr) {
        this.orderNumber = orderNr;
    }

    public int getOrderLineNumber() {
        return orderLineNumber;
    }

    public void setOrderLineNumber(int orderLineNumber) {
        this.orderLineNumber = orderLineNumber;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
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

