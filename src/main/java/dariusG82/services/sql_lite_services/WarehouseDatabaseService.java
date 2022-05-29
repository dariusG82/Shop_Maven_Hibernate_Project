package dariusG82.services.sql_lite_services;

import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.ItemIsNotInWarehouseExeption;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.WarehouseInterface;
import dariusG82.services.file_services.DataPath;
import dariusG82.warehouse.Item;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.List;

public class WarehouseDatabaseService extends SQLService implements WarehouseInterface {

    private final DataFromSQLiteService dataService;

    public WarehouseDatabaseService(DataFromSQLiteService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void receiveGoods(int purchaseOrder) throws WrongDataPathExeption {
        List<OrderLine> purchaseOrdersLines = dataService.getAllOrderLines(DataPath.PURCHASE_ORDERS_LINES_PATH);
        List<OrderLine> currentOrderLines = purchaseOrdersLines.stream()
                .filter(orderLine -> orderLine.getOrderNumber() == purchaseOrder)
                .toList();

        currentOrderLines.forEach(this::updateWarehouseStock);
    }

    @Override
    public Item getItemFromWarehouse(String itemName) throws ItemIsNotInWarehouseExeption {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = criteriaBuilder.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("itemName"), itemName));

        Query<Item> itemQuery = session.createQuery(criteriaQuery);

        List<Item> items = itemQuery.getResultList();

        session.close();

        if(items.size() == 0){
            throw new ItemIsNotInWarehouseExeption();
        }

        return items.get(0);
    }

    @Override
    public void updateWarehouseStock(OrderLine orderLine) {
        int itemId = orderLine.getItemID();
        Item item = getItemById(itemId);

        int currentQuantity = item.getStockQuantity() + orderLine.getLineQuantity();

        Session session = sessionFactory.openSession();

        session.beginTransaction();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<Item> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Item.class);
        Root<Item> root = criteriaUpdate.from(Item.class);

        criteriaUpdate.set("stockQuantity", currentQuantity);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("itemId"), itemId));

        session.createQuery(criteriaUpdate).executeUpdate();

        session.getTransaction().commit();

        session.close();
    }

    @Override
    public List<Item> getAllWarehouseItems() {
        Session session = sessionFactory.openSession();

        Query<Item> itemQuery = session.createQuery("select data from Item data", Item.class);
        List<Item> items = itemQuery.getResultList();

        session.close();

        return items;
    }

    @Override
    public Item getItemByName(String itemName) {
        List<Item> items = getAllWarehouseItems();

        return items.stream()
                .filter(item -> item.getItemName().equals(itemName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Item getItemById(int id) {
        List<Item> items = getAllWarehouseItems();

        return items.stream()
                .filter(item -> item.getItemId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addNewItem(Item item) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.persist(item);
        session.getTransaction().commit();

        session.close();
    }

    @Override
    public int getNewItemID() {
        List<Item> items = dataService.getAllItems();

        if(items == null){
            return 1;
        }

        return items.size() + 1;
    }
}
