package dariusG82.accounting.orders;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;

@Entity
@Table(name = "orders")
public class Order implements Serializable {

    @Id
    private String orderSeries;
    @Id
    private long orderNumber;
    private String orderDate;
    private String clientName;
    private double orderAmount;
    private String salesperson;
    private boolean payment_received;

    public Order(){

    }

    public Order(long orderNumber) {
        this.orderNumber = orderNumber;
        this.salesperson = "";
    }

    public Order(long orderNumber, String salesperson) {
        this.orderNumber = orderNumber;
        this.salesperson = salesperson;
    }

    public Order(String orderSeries, long orderNumber, String orderDate, String clientName, double orderAmount, String salesperson, boolean payment_received) {
        this.orderSeries = orderSeries;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.clientName = clientName;
        this.orderAmount = orderAmount;
        this.salesperson = salesperson;
        this.payment_received = payment_received;
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

    public void setOrderNumber(long orderID) {
        this.orderNumber = orderID;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getSalesperson() {
        return salesperson;
    }

    public void setSalesperson(String salesperson) {
        this.salesperson = salesperson;
    }

    public boolean isPayment_received() {
        return payment_received;
    }

    public void setPayment_received(boolean payment_status) {
        this.payment_received = payment_status;
    }

    public void addOrderLines(ArrayList<OrderLine> orderLines){

    }

    public void updateOrderAmount(double orderAmount){
        this.orderAmount += orderAmount;
    }
}
