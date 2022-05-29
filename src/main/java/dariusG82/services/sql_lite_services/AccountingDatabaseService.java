package dariusG82.services.sql_lite_services;

import dariusG82.accounting.finance.Balance;
import dariusG82.accounting.finance.CashJournalEntry;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.ItemIsNotInOrderException;
import dariusG82.custom_exeptions.NegativeBalanceException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.AccountingInterface;
import dariusG82.services.file_services.DataFileIndex;
import dariusG82.warehouse.Item;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static dariusG82.accounting.orders.OrderSeries.*;

public class AccountingDatabaseService extends SQLService implements AccountingInterface {

    private final DataFromSQLiteService dataService;

    public AccountingDatabaseService(DataFromSQLiteService dataService) {
        this.dataService = dataService;
        updateCashJournal(LocalDate.now());
    }

    private void updateCashJournal(LocalDate date){
        List<CashJournalEntry> entryList = getAllCashJournalEntries();

        LocalDate lastDate = LocalDate.parse(entryList.get(entryList.size()-1).getReportDate());

        if(lastDate.isBefore(date)){
            createAndSaveNewCashJournalEntries(lastDate);
        }
    }

    private void createAndSaveNewCashJournalEntries(LocalDate date){
        List<CashRecord> records = getAllCashRecords();
        List<CashRecord> newestRecords = new ArrayList<>();

        for (CashRecord record : records){
            if(LocalDate.parse(record.getRecordDate()).isAfter(date)){
                newestRecords.add(record);
            }
        }

        LocalDate startDate = LocalDate.parse(newestRecords.get(0).getRecordDate());
        CashJournalEntry entry = new CashJournalEntry();
        int entryId = getNewCashJournalEntryID();
        entry.setReportID(entryId);
        entry.setReportDate(startDate.toString());

        for (CashRecord record : newestRecords) {
            LocalDate currentDate = LocalDate.parse(record.getRecordDate());
            if (!currentDate.equals(startDate)) {
                saveEntryToDB(entry);
                startDate = currentDate;
                entry = new CashJournalEntry();
                entryId = getNewCashJournalEntryID();
                entry.setReportID(entryId);
                entry.setReportDate(startDate.toString());
            }
            updateEntry(entry, record);
        }
        saveEntryToDB(entry);
    }

    private void updateEntry(CashJournalEntry entry, CashRecord record){
        switch (record.getOrderSeries()){
            case "SF" -> {
                entry.updateIncome(record.getAmount());
                entry.updateBalance();
            }
            case "RE", "PO" -> {
                entry.updateExpense(record.getAmount());
                entry.updateBalance();
            }
        }
    }

    private void saveEntryToDB(CashJournalEntry entry){
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(entry);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public void updateSalesOrderLines(Order salesOrder, List<OrderLine> returnOrderLines) {
        for (OrderLine orderLine : returnOrderLines){
            try {
                updateOrderLine(salesOrder, orderLine);
            } catch (WrongDataPathExeption e) {
                return;
            }
        }
    }

    private void updateOrderLine(Order order, OrderLine orderLine) throws WrongDataPathExeption {
        List<OrderLine> orderLineList = dataService.getOrderLinesForOrder(order);

        int currentQuantity = 0;
        double itemPrice = 0.0;

        for (OrderLine line : orderLineList) {
            if (line.getItemID() == orderLine.getItemID()) {
                currentQuantity = line.getLineQuantity();
                itemPrice = line.getLineAmount() / currentQuantity;
            }
        }

        int newQuantity = currentQuantity + orderLine.getLineQuantity();
        double newLineAmount = newQuantity * itemPrice;

        Session session = sessionFactory.openSession();

        session.beginTransaction();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<OrderLine> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(OrderLine.class);
        Root<OrderLine> root = criteriaUpdate.from(OrderLine.class);

        criteriaUpdate.set("lineQuantity", newQuantity);
        criteriaUpdate.set("lineAmount", newLineAmount);
        criteriaUpdate.where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("orderSeries"), order.getOrderSeries()),
                criteriaBuilder.equal(root.get("orderNumber"), order.getOrderNumber()),
                criteriaBuilder.equal(root.get("itemID"), orderLine.getItemID())
        ));

        session.createQuery(criteriaUpdate).executeUpdate();

        session.getTransaction().commit();

        session.close();
    }

    @Override
    public void addNewCashRecord(CashRecord cashRecord) {
        int recordId = getNewCashRecordNumber();
        cashRecord.setRecordID(recordId);

        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(cashRecord);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public Order getDocumentByID(String id) {

        String series = id.substring(0, 2);
        int number = Integer.parseInt(id.substring(2));

        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        criteriaQuery.select(root).where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("orderSeries"), series),
                criteriaBuilder.equal(root.get("orderNumber"), number)
        ));

        Query<Order> orderQuery = session.createQuery(criteriaQuery);

        Order order = orderQuery.getSingleResult();

        session.close();

        return order;
    }

    @Override
    public Item getSoldItemByName(Order salesOrder, String itemName) throws ItemIsNotInOrderException {
        List<OrderLine> orderLines = dataService.getOrderLinesForOrder(salesOrder);
        Item item = dataService.getWarehouseService().getItemByName(itemName);

        for (OrderLine orderLine : orderLines) {
            if (orderLine.getItemID() == item.getItemId()) {
                item.setStockQuantity(orderLine.getLineQuantity());
                return item;
            }
        }

        throw new ItemIsNotInOrderException();
    }

    @Override
    public double getDaysBalance(LocalDate date) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CashRecord> query = criteriaBuilder.createQuery(CashRecord.class);
        Root<CashRecord> root = query.from(CashRecord.class);
        query.select(root).where(criteriaBuilder.equal(root.get("recordDate"),date.toString()));

        Query<CashRecord> recordQuery = session.createQuery(query);
        List<CashRecord> records = recordQuery.getResultList();

        session.close();

        return records.stream().mapToDouble(CashRecord::getAmount).sum();
    }

    @Override
    public double getMonthBalance(LocalDate date) {
        List<CashRecord> records = getAllCashRecords();

        double monthBalance = 0.0;

        for (CashRecord record : records){
            LocalDate localDate = LocalDate.parse(record.getRecordDate());
            if(localDate.getYear() == date.getYear() && localDate.getMonth() == date.getMonth()){
                monthBalance += record.getAmount();
            }
        }

        return monthBalance;
    }

    @Override
    public List<CashRecord> getDailySaleDocuments(LocalDate date, String series) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CashRecord> query = criteriaBuilder.createQuery(CashRecord.class);
        Root<CashRecord> root = query.from(CashRecord.class);
        query.select(root).where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("recordDate"),date.toString()),
                criteriaBuilder.equal(root.get("orderSeries"),series)
        ));

        Query<CashRecord> recordQuery = session.createQuery(query);
        List<CashRecord> records = recordQuery.getResultList();

        session.close();

        return records;
    }

    @Override
    public int getNewSalesDocumentNumber() {
        return getNewDocumentNumber(SALE.getSeries());
    }

    @Override
    public int getNewReturnDocumentNumber() {
        return getNewDocumentNumber(RETURN.getSeries());
    }

    @Override
    public int getNewPurchaseOrderNumber(){
        return getNewDocumentNumber(PURCHASE.getSeries());
    }

    private int getNewCashRecordNumber(){
        List<CashRecord> records = getAllCashRecords();

        return records.size() + 1;
    }

    private int getNewCashJournalEntryID(){
        List<CashJournalEntry> entries = getAllCashJournalEntries();

        return entries.size() + 1;
    }

    private List<CashJournalEntry> getAllCashJournalEntries(){
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CashJournalEntry> criteriaQuery = criteriaBuilder.createQuery(CashJournalEntry.class);
        Root<CashJournalEntry> root = criteriaQuery.from(CashJournalEntry.class);


        criteriaQuery.select(root);
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("reportDate")));

        Query<CashJournalEntry> query = session.createQuery(criteriaQuery);

        List<CashJournalEntry> entryList = query.getResultList();

        session.close();

        return entryList;
    }

    private int getNewDocumentNumber(String series){
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = criteriaQuery.from(Order.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("orderSeries"), series));

        Query<Order> orderQuery = session.createQuery(criteriaQuery);

        int maxNumber = orderQuery.getResultList().size();

        session.close();

        return maxNumber + 1;
    }

    @Override
    public List<CashRecord> getMonthlySalesReportBySeller(String username, LocalDate date) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CashRecord> criteriaQuery = criteriaBuilder.createQuery(CashRecord.class);
        Root<CashRecord> root = criteriaQuery.from(CashRecord.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("sellerId"), username));

        Query<CashRecord> recordQuery = session.createQuery(criteriaQuery);
        List<CashRecord> records = recordQuery.getResultList();

        session.close();

        return records == null ? null : getCashRecords(date, records);
    }

    @Override
    public double getTotalSalesByReport(List<CashRecord> records) {
        return records.stream().mapToDouble(CashRecord::getAmount).sum();
    }

    @Override
    public void updateCashBalance(double amount, DataFileIndex dataId) throws NegativeBalanceException {
        List<Balance> balanceList = getCurrentBalance();
        if (balanceList == null) {
            return;
        }

        double newAmount = 0.0;

        for(Balance balance : balanceList){
            if(balance.getDataType().equals(dataId.toString())){
                newAmount = balance.getBalance() + amount;
            }
        }

        if(newAmount >= 0.0){
            setNewBalance(newAmount, dataId.toString());
        } else {
            throw new NegativeBalanceException();
        }
    }

    @Override
    public boolean isOrderReceivedPayment(Order order) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        criteriaQuery.select(root).where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("orderSeries"), order.getOrderSeries()),
                criteriaBuilder.equal(root.get("orderNumber"), order.getOrderNumber())
        ));

        Query<Order> orderQuery = session.createQuery(criteriaQuery);

        Order newOrder = orderQuery.getSingleResult();

        session.close();

        return newOrder.isPayment_received();
    }

    @Override
    public void updateSalesOrderStatus(Order order) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<Order> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Order.class);
        Root<Order> root = criteriaUpdate.from(Order.class);

        criteriaUpdate.set("payment_received", true);
        criteriaUpdate.where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("orderSeries"), order.getOrderSeries()),
                criteriaBuilder.equal(root.get("orderNumber"), order.getOrderNumber())
        ));

        session.createQuery(criteriaUpdate).executeUpdate();

        session.getTransaction().commit();

        session.close();
    }

    private List<Balance> getCurrentBalance() {
        Session session = sessionFactory.openSession();

        Query<Balance> balanceQuery = session.createQuery("select data from Balance data", Balance.class);
        List<Balance> balanceList = balanceQuery.getResultList();

        session.close();

        return balanceList;
    }

    private void setNewBalance(double newBalance, String dataType) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<Balance> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Balance.class);
        Root<Balance> root = criteriaUpdate.from(Balance.class);

        criteriaUpdate.set("balance", newBalance);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("dataType"), dataType));

        session.createQuery(criteriaUpdate).executeUpdate();

        session.getTransaction().commit();

        session.close();
    }

    @Override
    public double getTotalOrderAmount(List<OrderLine> orderLines) {

        return orderLines.stream().mapToDouble(OrderLine::getLineAmount).sum();
    }

    private List<Order> getAllOrders(){
        Session session = sessionFactory.openSession();

        Query<Order> orderQuery = session.createQuery("select data from Order data", Order.class);
        List<Order> orders = orderQuery.getResultList();

        session.close();

        return orders;
    }

    private Order getOrderFromDB(Order orderToRetrieve){
        String orderSeries = orderToRetrieve.getOrderSeries();
        int orderNumber = orderToRetrieve.getOrderNumber();

        return getOrderBySeriesAndNumber(orderSeries, orderNumber);
    }

    private Order getOrderBySeriesAndNumber(String series, int number){
        List<Order> orders = getAllOrders();

        return orders.stream()
                .filter(order -> order.getOrderSeries().equals(series) && order.getOrderNumber() == number)
                .findFirst()
                .orElse(null);
    }

    private List<CashRecord> getAllCashRecords(){
        Session session = sessionFactory.openSession();

        Query<CashRecord> recordQuery = session.createQuery("select data from CashRecord data", CashRecord.class);
        List<CashRecord> records = recordQuery.getResultList();

        session.close();

        return records;
    }

    private List<CashRecord> getSalesRecords(){
        return getRecordsByOrderSeries(RETURN.getSeries());
    }

    private List<CashRecord> getReturnRecords(){
        return getRecordsByOrderSeries(RETURN.getSeries());
    }

    private List<CashRecord> getPurchaseRecords(){
        return getRecordsByOrderSeries(PURCHASE.getSeries());
    }

    private List<CashRecord> getRecordsByOrderSeries(String orderSeries){
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CashRecord> query = criteriaBuilder.createQuery(CashRecord.class);
        Root<CashRecord> root = query.from(CashRecord.class);
        query.select(root).where(criteriaBuilder.equal(root.get("orderSeries"),orderSeries));

        Query<CashRecord> recordQuery = session.createQuery(query);
        List<CashRecord> records = recordQuery.getResultList();

        session.close();

        return records;
    }
}
