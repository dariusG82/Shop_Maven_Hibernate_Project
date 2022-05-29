package dariusG82.data.interfaces;

import dariusG82.accounting.DailyReport;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.*;
import dariusG82.custom_exeptions.ItemIsAlreadyInDatabaseException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.partners.Client;
import dariusG82.services.file_services.DataPath;
import dariusG82.users.User;
import dariusG82.warehouse.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface DataManagement {

    List<OrderLine> getAllOrderLines(DataPath orderDataType) throws WrongDataPathExeption;

    List<OrderLine> getOrderLinesForOrder(Order order) throws WrongDataPathExeption;

    ArrayList<CashRecord> getAllCashRecords();

    ArrayList<DailyReport> getDailyReports();

    List<User> getAllUsers();

    List<Client> getAllClients();

    void addNewItemCard(Item item) throws ItemIsAlreadyInDatabaseException, IOException;

    List<Item> getAllItems();

    void saveOrder(Order order, List<OrderLine> orderLines) throws WrongDataPathExeption, IOException;

    AdminInterface getAdminService();

    AccountingInterface getAccountingService();

    BusinessInterface getBusinessService();

    WarehouseInterface getWarehouseService();
}


