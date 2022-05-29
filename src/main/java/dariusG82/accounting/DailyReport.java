package dariusG82.accounting;

import dariusG82.accounting.finance.CashOperation;

import java.time.LocalDate;

public class DailyReport {

    private final String reportID;
    private final LocalDate date;
    private double dailyIncome;
    private double dailyExpenses;
    private double dailyBalance;

    public DailyReport(LocalDate date) {
        this.reportID = null;
        this.date = date;
        this.dailyIncome = 0.0;
        this.dailyExpenses = 0.0;
    }

    public DailyReport(LocalDate date, double dailyIncome, double dailyExpenses, double dailyBalance) {
        this.reportID = null;
        this.date = date;
        this.dailyIncome = dailyIncome;
        this.dailyExpenses = dailyExpenses;
        this.dailyBalance = dailyBalance;
    }

    public DailyReport(String reportID, LocalDate date, double dailyIncome, double dailyExpenses, double dailyBalance) {
        this.reportID = reportID;
        this.date = date;
        this.dailyIncome = dailyIncome;
        this.dailyExpenses = dailyExpenses;
        this.dailyBalance = dailyBalance;
    }

    public void updateDailyReport(CashOperation operation, double amount) {
        switch (operation) {
            case DAILY_INCOME -> {
                this.dailyIncome += amount;
                this.dailyExpenses += 0.0;
            }
            case DAILY_EXPENSE -> {
                this.dailyIncome += 0.0;
                this.dailyExpenses += amount;
            }
            default -> {
                this.dailyIncome += 0.0;
                this.dailyExpenses += 0.0;
            }
        }
    }

    public String getReportID() {
        return reportID;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getDailyIncome() {
        return dailyIncome;
    }

    public double getDailyExpenses() {
        return dailyExpenses;
    }

    public double getDailyBalance() {
        this.dailyBalance = dailyIncome - dailyExpenses;
        return dailyBalance;
    }
}
