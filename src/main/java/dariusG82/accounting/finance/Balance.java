package dariusG82.accounting.finance;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "balance")
public class Balance {

    @Id
    private int typeId;
    private String dataType;
    private double balance;

    public Balance() {
    }

    public Balance(int typeId, String dataType, double balance) {
        this.typeId = typeId;
        this.dataType = dataType;
        this.balance = balance;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
