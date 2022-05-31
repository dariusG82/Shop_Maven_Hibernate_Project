package dariusG82.data.interfaces;

import dariusG82.accounting.finance.CashJournalEntry;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.NegativeBalanceException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.services.file_services.DataFileIndex;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface AccountingInterface {

    void addNewCashRecord(CashRecord cashRecord) throws IOException;
    List<CashJournalEntry> getDailyReports();

    double getDaysBalance(LocalDate date) throws SQLException;

    double getMonthBalance(LocalDate date);

    List<CashRecord> getDailySaleDocuments(LocalDate date, String series);

    double getTotalSalesByReport(List<CashRecord> records);

    List<CashRecord> getMonthlySalesReportBySeller(String username, LocalDate date);

    long getNewSalesDocumentNumber() throws IOException, WrongDataPathExeption;

    long getNewReturnDocumentNumber() throws IOException, WrongDataPathExeption;

    long getNewPurchaseOrderNumber() throws WrongDataPathExeption, IOException;

    void updateCashBalance(double amount, DataFileIndex dataId) throws WrongDataPathExeption, IOException, NegativeBalanceException;

    boolean isOrderReceivedPayment(Order order) throws SQLException;

    double getTotalOrderAmount(List<OrderLine> orderLines);

    default List<CashRecord> getCashRecords(LocalDate date, List<CashRecord> recordsBySeller) {
        List<CashRecord> monthlyRecords = new ArrayList<>();

        for(CashRecord record : recordsBySeller){
            LocalDate recordDate = LocalDate.parse(record.getRecordDate());
            if(recordDate.getYear() == date.getYear() && recordDate.getMonth() == date.getMonth()){
                monthlyRecords.add(record);
            }
        }
        return monthlyRecords;
    }
}
