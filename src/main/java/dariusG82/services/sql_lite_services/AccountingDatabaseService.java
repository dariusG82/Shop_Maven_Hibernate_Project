package dariusG82.services.sql_lite_services;

import dariusG82.accounting.finance.Balance;
import dariusG82.accounting.finance.CashJournalEntry;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.NegativeBalanceException;
import dariusG82.data.interfaces.AccountingInterface;
import dariusG82.services.file_services.DataFileIndex;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static dariusG82.accounting.orders.OrderSeries.*;

public class AccountingDatabaseService extends SQLService implements AccountingInterface {

    public AccountingDatabaseService() {
        updateCashJournal(LocalDate.now());
    }

    @Override
    public void addNewCashRecord(CashRecord cashRecord) {
        long recordId = getNewCashRecordNumber();
        cashRecord.setRecordID(recordId);

        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(cashRecord);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public double getTotalSalesByReport(List<CashRecord> records) {
        return records.stream().mapToDouble(CashRecord::getAmount).sum();
    }

    @Override
    public double getDaysBalance(LocalDate date) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CashRecord> query = criteriaBuilder.createQuery(CashRecord.class);
        Root<CashRecord> root = query.from(CashRecord.class);
        query.select(root).where(criteriaBuilder.equal(root.get("recordDate"), date.toString()));

        Query<CashRecord> recordQuery = session.createQuery(query);
        List<CashRecord> records = recordQuery.getResultList();

        session.close();

        double totalIncome = 0.0;
        double totalExpense = 0.0;
        for (CashRecord record : records){
            switch (record.getOrderSeries()){
                case "RE", "PO" -> totalExpense += record.getAmount();
                case "SF" -> totalIncome += record.getAmount();
            }
        }

        return totalIncome - totalExpense;
    }

    @Override
    public double getMonthBalance(LocalDate date) {
        List<CashRecord> records = getAllCashRecords();

        double monthBalance = 0.0;

        for (CashRecord record : records) {
            LocalDate localDate = LocalDate.parse(record.getRecordDate());
            if (localDate.getYear() == date.getYear() && localDate.getMonth() == date.getMonth()) {
                monthBalance += record.getAmount();
            }
        }

        return monthBalance;
    }

    @Override
    public List<CashJournalEntry> getDailyReports() {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CashJournalEntry> criteriaQuery = criteriaBuilder.createQuery(CashJournalEntry.class);
        Root<CashJournalEntry> root = criteriaQuery.from(CashJournalEntry.class);

        criteriaQuery.select(root);

        Query<CashJournalEntry> query = session.createQuery(criteriaQuery);
        List<CashJournalEntry> cashJournalEntries = query.getResultList();

        session.close();

        return cashJournalEntries;
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
    public void updateCashBalance(double amount, DataFileIndex dataId) throws NegativeBalanceException {
        List<Balance> balanceList = getCurrentBalance();
        if (balanceList == null) {
            return;
        }

        double newAmount = 0.0;

        for (Balance balance : balanceList) {
            if (balance.getDataType().equals(dataId.toString())) {
                newAmount = balance.getBalance() + amount;
            }
        }

        if (newAmount >= 0.0) {
            setNewBalance(newAmount, dataId.toString());
        } else {
            throw new NegativeBalanceException();
        }
    }

    @Override
    public double getTotalOrderAmount(List<OrderLine> orderLines) {

        return orderLines.stream().mapToDouble(OrderLine::getLineAmount).sum();
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
    public List<CashRecord> getDailySaleDocuments(LocalDate date, String series) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CashRecord> query = criteriaBuilder.createQuery(CashRecord.class);
        Root<CashRecord> root = query.from(CashRecord.class);
        query.select(root).where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("recordDate"), date.toString()),
                criteriaBuilder.equal(root.get("orderSeries"), series)
        ));

        Query<CashRecord> recordQuery = session.createQuery(query);
        List<CashRecord> records = recordQuery.getResultList();

        session.close();

        return records;
    }

    @Override
    public long getNewSalesDocumentNumber() {
        return getNewDocumentNumber(SALE.getSeries());
    }

    @Override
    public long getNewReturnDocumentNumber() {
        return getNewDocumentNumber(RETURN.getSeries());
    }

    @Override
    public long getNewPurchaseOrderNumber() {
        return getNewDocumentNumber(PURCHASE.getSeries());
    }

    private void updateCashJournal(LocalDate date) {
        List<CashJournalEntry> entryList = getAllCashJournalEntries();

        if(entryList.size() == 0){
            createAndSaveNewCashJournalEntries(LocalDate.of(2000,1,1));
            return;
        }

        LocalDate lastDate = LocalDate.parse(entryList.get(entryList.size() - 1).getReportDate());

        if (lastDate.isBefore(date)) {
            createAndSaveNewCashJournalEntries(lastDate);
        }
    }

    private void createAndSaveNewCashJournalEntries(LocalDate date) {
        List<CashRecord> records = getAllCashRecords();
        List<CashRecord> newestRecords = new ArrayList<>();

        for (CashRecord record : records) {
            if (LocalDate.parse(record.getRecordDate()).isAfter(date)) {
                newestRecords.add(record);
            }
        }

        if (newestRecords.size() == 0) {
            return;
        }

        LocalDate startDate = LocalDate.parse(newestRecords.get(0).getRecordDate());
        CashJournalEntry entry = new CashJournalEntry();
        long entryId = getNewCashJournalEntryID();
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

    private void updateEntry(CashJournalEntry entry, CashRecord record) {
        switch (record.getOrderSeries()) {
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

    private void saveEntryToDB(CashJournalEntry entry) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(entry);
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

    private List<CashRecord> getAllCashRecords() {
        Session session = sessionFactory.openSession();

        Query<CashRecord> recordQuery = session.createQuery("select data from CashRecord data", CashRecord.class);
        List<CashRecord> records = recordQuery.getResultList();

        session.close();

        return records;
    }

    private List<CashJournalEntry> getAllCashJournalEntries() {
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

    private int getNewDocumentNumber(String series) {
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

    private long getNewCashRecordNumber() {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CashRecord> root = criteriaQuery.from(CashRecord.class);

        criteriaQuery.select(criteriaBuilder.count(root));
        TypedQuery<Long> query = session.createQuery(criteriaQuery);
        Long count = query.getSingleResult();

        session.close();

        return count + 1;
    }

    private long getNewCashJournalEntryID() {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CashJournalEntry> root = criteriaQuery.from(CashJournalEntry.class);

        criteriaQuery.select(criteriaBuilder.count(root));
        TypedQuery<Long> query = session.createQuery(criteriaQuery);
        Long count = query.getSingleResult();

        session.close();

        return count + 1;
    }


}
