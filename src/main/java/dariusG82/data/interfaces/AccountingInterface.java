package dariusG82.data.interfaces;

import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.*;
import dariusG82.services.file_services.DataFileIndex;
import dariusG82.warehouse.Item;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface AccountingInterface {

    void updateSalesOrderLines(Order salesOrder, List<OrderLine> orderLines) throws WrongDataPathExeption, IOException;

    void addNewCashRecord(CashRecord cashRecord) throws IOException;

    Order getDocumentByID(String id) throws WrongDataPathExeption, ClientDoesNotExistExeption, SQLException, OrderDoesNotExistException;

    Item getSoldItemByName(Order salesOrder, String itemName) throws SQLException, WrongDataPathExeption, FileNotFoundException, ItemIsNotInOrderException;

    double getDaysBalance(LocalDate date) throws SQLException;

    double getMonthBalance(LocalDate date);

    List<CashRecord> getDailySaleDocuments(LocalDate date, String series);

    int getNewSalesDocumentNumber() throws IOException, WrongDataPathExeption;
    int getNewReturnDocumentNumber() throws IOException, WrongDataPathExeption;

    int getNewPurchaseOrderNumber() throws WrongDataPathExeption, IOException;

    List<CashRecord> getMonthlySalesReportBySeller(String username, LocalDate date);

    double getTotalSalesByReport(List<CashRecord> records);

    void updateCashBalance(double amount, DataFileIndex dataId) throws WrongDataPathExeption, IOException, NegativeBalanceException;

    boolean isOrderReceivedPayment(Order order) throws SQLException;

    void updateSalesOrderStatus(Order order) throws IOException, WrongDataPathExeption;

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
