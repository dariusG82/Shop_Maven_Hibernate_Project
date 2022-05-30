package dariusG82.warehouse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Random;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @Column(name = "itemId")
    public long itemId;
    @Column(name = "itemName")
    private String itemName;
    @Column(name = "itemDescription")
    private String itemDescription;
    @Column(name = "purchasePrice")
    private double purchasePrice;
    @Column(name = "salePrice")
    private double salePrice;
    @Column(name = "stockQuantity")
    private int stockQuantity;

    public Item() {

    }

    public Item(String itemName) {
        this.itemName = itemName;
        this.itemDescription = null;
        this.purchasePrice = 0.0;
        this.salePrice = 0.0;
        this.stockQuantity = 0;
    }

    public Item(long itemId, String itemName, String itemDescription, double purchasePrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.purchasePrice = purchasePrice;
        this.salePrice = setNewSalePrice();
        this.stockQuantity = 0;
    }

    public Item(String itemName, String itemDescription, double purchasePrice, int stockQuantity) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.purchasePrice = purchasePrice;
        this.salePrice = setNewSalePrice();
        this.stockQuantity = stockQuantity;
    }

    public Item(String itemName, String itemDescription, double purchasePrice, double salePrice, int stockQuantity) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.stockQuantity = stockQuantity;
    }

    public Item(long itemId, String itemName, String itemDescription, double purchasePrice, double salePrice, int stockQuantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.stockQuantity = stockQuantity;
    }

    private double setNewSalePrice() {
        Random random = new Random();
        return Math.round((getPurchasePrice() * random.nextDouble(1.15, 1.6)) * 100.0) / 100.0;
    }

    public void updateQuantity(int quantity) {
        this.stockQuantity += quantity;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Double.compare(item.purchasePrice, purchasePrice) == 0 && Double.compare(item.salePrice, salePrice) == 0 && Objects.equals(itemName, item.itemName);
    }
}
