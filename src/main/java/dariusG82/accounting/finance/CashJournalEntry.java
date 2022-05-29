package dariusG82.accounting.finance;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cash_journal")
public class CashJournalEntry {

    @Id
    private int reportID;
    private String reportDate;
    private double dailyIncome;
    private double dailyExpenses;
    private double dailyBalance;

    public CashJournalEntry() {
    }

    public CashJournalEntry(int reportID, String reportDate, double dailyIncome, double dailyExpenses, double dailyBalance) {
        this.reportID = reportID;
        this.reportDate = reportDate;
        this.dailyIncome = dailyIncome;
        this.dailyExpenses = dailyExpenses;
        this.dailyBalance = dailyBalance;
    }

    public int getReportID() {
        return reportID;
    }

    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public double getDailyIncome() {
        return dailyIncome;
    }

    public void setDailyIncome(double dailyIncome) {
        this.dailyIncome = dailyIncome;
    }

    public double getDailyExpenses() {
        return dailyExpenses;
    }

    public void setDailyExpenses(double dailyExpenses) {
        this.dailyExpenses = dailyExpenses;
    }

    public double getDailyBalance() {
        return dailyBalance;
    }

    public void setDailyBalance(double dailyBalance) {
        this.dailyBalance = dailyBalance;
    }

    public void updateIncome(double amount){
        this.dailyIncome += amount;
    }

    public void updateExpense(double amount){
        this.dailyExpenses += amount;
    }

    public void updateBalance(){
        this.dailyBalance = getDailyIncome() - getDailyBalance();
    }
}
