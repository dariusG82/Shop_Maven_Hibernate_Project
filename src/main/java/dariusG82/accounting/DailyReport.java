package dariusG82.accounting;

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
        this.dailyBalance = 0.0;
    }

    public void updateDailyReport(String series, double amount) {
        switch (series) {
            case "SF" -> {
                this.dailyIncome += amount;
                this.dailyExpenses += 0.0;
            }
            case "RE" -> {
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
