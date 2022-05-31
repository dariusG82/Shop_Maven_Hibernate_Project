package dariusG82.services.sql_lite_services;

import dariusG82.accounting.orders.Order;
import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.ItemIsNotInOrderException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.OrdersManagementInterface;
import dariusG82.services.file_services.DataPath;
import dariusG82.warehouse.Item;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.List;

import static dariusG82.accounting.orders.OrderSeries.*;

public class OrderManagementSQLiteService extends SQLService implements OrdersManagementInterface {

    WarehouseDatabaseService warehouseDatabaseService = new WarehouseDatabaseService();

    @Override
    public Order getDocumentByID(String id) {

        String series = id.substring(0, 2);
        int number = Integer.parseInt(id.substring(2));

        return getOrderBySeriesAndNumber(series, number);
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
    public Item getSoldItemByName(Order salesOrder, String itemName) throws ItemIsNotInOrderException {
        Item item = warehouseDatabaseService.getItemByName(itemName);

        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<OrderLine> criteriaQuery = criteriaBuilder.createQuery(OrderLine.class);
        Root<OrderLine> root = criteriaQuery.from(OrderLine.class);

        criteriaQuery.select(root).where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("itemName"), itemName),
                criteriaBuilder.equal(root.get("orderSeries"),salesOrder.getOrderSeries()),
                criteriaBuilder.equal(root.get("orderNumber"),salesOrder.getOrderNumber())
        ));

        Query<OrderLine> itemQuery = session.createQuery(criteriaQuery);
        OrderLine orderLine = itemQuery.getSingleResult();

        session.close();

        if(item != null){
            item.setStockQuantity(orderLine.getLineQuantity());
            return item;
        } else {
            throw new ItemIsNotInOrderException();
        }
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
    public void updateSalesOrderLines(Order salesOrder, List<OrderLine> returnOrderLines) {
        for (OrderLine orderLine : returnOrderLines) {
            try {
                updateOrderLine(salesOrder, orderLine);
            } catch (WrongDataPathExeption e) {
                return;
            }
        }
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

    private Order getOrderBySeriesAndNumber(String series, int number) {
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

    private void saveOrderLinesToDatabase(List<OrderLine> orderLines){
        orderLines.forEach(orderLine -> {
            Session session = sessionFactory.openSession();

            session.beginTransaction();
            session.persist(orderLine);
            session.getTransaction().commit();

            session.close();
        });
    }

    private void updateOrderLine(Order order, OrderLine orderLine) throws WrongDataPathExeption {
        List<OrderLine> orderLineList = getOrderLinesForOrder(order);

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
                criteriaBuilder.equal(root.get("orderSeries"), orderLine.getOrderSeries()),
                criteriaBuilder.equal(root.get("orderNumber"), orderLine.getOrderNumber()),
                criteriaBuilder.equal(root.get("itemId"), orderLine.getItemID())
        ));

        session.createQuery(criteriaUpdate).executeUpdate();

        session.getTransaction().commit();

        session.close();
    }
}
