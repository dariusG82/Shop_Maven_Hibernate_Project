package dariusG82.services.sql_lite_services;

import dariusG82.accounting.DailyReport;
import dariusG82.accounting.finance.CashRecord;
import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.ItemIsAlreadyInDatabaseException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.*;
import dariusG82.partners.Client;
import dariusG82.services.file_services.DataPath;
import dariusG82.users.User;
import dariusG82.warehouse.Item;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static dariusG82.accounting.orders.OrderSeries.*;
import static dariusG82.services.sql_lite_services.SQL_Query.SELECT_CASH_JOURNAL;

public class DataFromSQLiteService extends SQLService implements DataManagement {


    private final AdminDatabaseService adminDatabaseService = new AdminDatabaseService(this);
    private final BusinessDatabaseService businessDatabaseService = new BusinessDatabaseService(this);
    private final WarehouseDatabaseService warehouseDatabaseService = new WarehouseDatabaseService(this);
    private final AccountingDatabaseService accountingDatabaseService = new AccountingDatabaseService(this);

    @Override
    public List<OrderLine> getAllOrderLines(DataPath orderDataType) throws WrongDataPathExeption {
        String series = switch (orderDataType){
            case PURCHASE_ORDERS_LINES_PATH -> PURCHASE.getSeries();
            case RETURN_ORDERS_LINES_PATH -> RETURN.getSeries();
            case SALES_ORDERS_LINES_PATH -> SALE.getSeries();
            default -> null;
        };

        if(series == null){
            throw new WrongDataPathExeption();
        }
        Session session = sessionFactory.openSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<OrderLine> criteriaQuery = cb.createQuery(OrderLine.class);
        Root<OrderLine> root = criteriaQuery.from(OrderLine.class);
        criteriaQuery.select(root).where(cb.equal(root.get("orderSeries"),series));

        Query<OrderLine> orderLineQuery = session.createQuery(criteriaQuery);

        List<OrderLine> orderLines = orderLineQuery.getResultList();

        session.close();

        return orderLines;
    }

    @Override
    public List<OrderLine> getOrderLinesForOrder(Order order) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<OrderLine> criteriaQuery = criteriaBuilder.createQuery(OrderLine.class);
        Root<OrderLine> root = criteriaQuery.from(OrderLine.class);

        criteriaQuery.select(root).where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("orderSeries"), order.getOrderSeries()),
                criteriaBuilder.equal(root.get("orderNumber"), order.getOrderNumber())
        ));

        Query<OrderLine> orderLineQuery = session.createQuery(criteriaQuery);
        List<OrderLine> orderLines = orderLineQuery.getResultList();

        session.close();

        return orderLines;
    }

    @Override
    public ArrayList<CashRecord> getAllCashRecords() {
        // TODO implement method
        return null;
    }

    @Override
    public ArrayList<DailyReport> getDailyReports() {
        ArrayList<DailyReport> reports = new ArrayList<>();
        String query = SELECT_CASH_JOURNAL.getQuery();

        try (Connection connection = this.connectToDB()) {

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String reportID = resultSet.getString("reportID");
                LocalDate reportDate = LocalDate.parse(resultSet.getString("reportDate"));
                double dailyIncome = resultSet.getDouble("dailyIncome");
                double dailyExpenses = resultSet.getDouble("dailyExpenses");
                double dailyBalance = resultSet.getDouble("dailyBalance");

                reports.add(new DailyReport(reportID, reportDate, dailyIncome, dailyExpenses, dailyBalance));
            }

            return reports;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        Session session = sessionFactory.openSession();

        Query<User> userQuery = session.createQuery("select data from User data", User.class);
        List<User> users = userQuery.getResultList();

        session.close();

        return users;
    }

    @Override
    public List<Client> getAllClients() {
        Session session = sessionFactory.openSession();

        Query<Client> clientQuery = session.createQuery("select data from Client data", Client.class);
        List<Client> clients = clientQuery.getResultList();

        session.close();

        return clients;
    }

    @Override
    public void addNewItemCard(Item newItem) throws ItemIsAlreadyInDatabaseException {
        List<Item> allItems = getAllItems();

        for(Item item : allItems){
            if(item.getItemName().equals(newItem.getItemName())){
                throw new ItemIsAlreadyInDatabaseException();
            }
        }

        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(newItem);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public List<Item> getAllItems() {
        Session session = sessionFactory.openSession();

        Query<Item> itemQuery = session.createQuery("select data from Item data", Item.class);
        List<Item> items = itemQuery.getResultList();

        session.close();

        return items;
    }

    @Override
    public void saveOrder(Order order, List<OrderLine> orderLines) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(order);
        session.getTransaction().commit();

        session.close();

        saveOrderLinesToDatabase(orderLines);
    }

    @Override
    public AdminInterface getAdminService() {
        return adminDatabaseService;
    }

    @Override
    public AccountingInterface getAccountingService() {
        return accountingDatabaseService;
    }

    @Override
    public BusinessInterface getBusinessService() {
        return businessDatabaseService;
    }

    @Override
    public WarehouseInterface getWarehouseService() {
        return warehouseDatabaseService;
    }

    private void saveOrderLinesToDatabase(List<OrderLine> orderLines){
        orderLines.forEach(orderLine -> {
            Session session = sessionFactory.openSession();

            session.beginTransaction();
            session.persist(orderLine);
            session.getTransaction().commit();

            session.close();
        });
    }
}
