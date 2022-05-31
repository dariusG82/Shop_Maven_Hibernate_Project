package dariusG82.services.sql_lite_services;

import dariusG82.accounting.orders.OrderLine;
import dariusG82.custom_exeptions.ItemIsAlreadyInDatabaseException;
import dariusG82.data.interfaces.WarehouseInterface;
import dariusG82.warehouse.Item;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.List;

public class WarehouseDatabaseService extends SQLService implements WarehouseInterface {

    @Override
    public void receiveGoods(long purchaseOrder) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<OrderLine> criteriaQuery = criteriaBuilder.createQuery(OrderLine.class);
        Root<OrderLine> root = criteriaQuery.from(OrderLine.class);

        criteriaQuery.select(root).where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("orderSeries"), "PO"),
                criteriaBuilder.equal(root.get("orderNumber"), purchaseOrder)
        ));

        Query<OrderLine> query = session.createQuery(criteriaQuery);
        List<OrderLine> orderLines = query.getResultList();

        session.close();

        orderLines.forEach(this::updateWarehouseStock);
    }

    @Override
    public void updateWarehouseStock(OrderLine orderLine) {
        long itemId = orderLine.getItemID();
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
    public List<Item> getAllItems() {
        Session session = sessionFactory.openSession();

        Query<Item> itemQuery = session.createQuery("select data from Item data", Item.class);
        List<Item> items = itemQuery.getResultList();

        session.close();

        return items;
    }

    @Override
    public Item getItemByName(String itemName) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = criteriaBuilder.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);

        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("itemName"), itemName));
        Item item = getItem(session, criteriaQuery);

        session.close();

        return item;

    }

    @Override
    public Item getItemById(long id) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = criteriaBuilder.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);

        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("itemId"), id));
        Item item = getItem(session, criteriaQuery);

        session.close();

        return item;
    }

    private Item getItem(Session session, CriteriaQuery<Item> criteriaQuery) {
        Query<Item> itemQuery = session.createQuery(criteriaQuery);

        try {
            return itemQuery.getSingleResult();
        } catch (NoResultException e){
            return null;
        }
    }

    @Override
    public long getNewItemID() {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Item> root = criteriaQuery.from(Item.class);

        criteriaQuery.select(criteriaBuilder.count(root));
        TypedQuery<Long> query = session.createQuery(criteriaQuery);
        Long count = query.getSingleResult();

        session.close();

        return count + 1;
    }

    @Override
    public void addNewItemCard(Item newItem) throws ItemIsAlreadyInDatabaseException {

        Item item = getItemByName(newItem.getItemName());

        if(item == null){
            Session session = sessionFactory.openSession();

            session.beginTransaction();
            session.persist(newItem);
            session.getTransaction().commit();

            session.close();
        } else {
            throw new ItemIsAlreadyInDatabaseException();
        }
    }

    @Override
    public void removeItemCard(Item item) {
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        session.delete(item);
        session.getTransaction().commit();

        session.close();
    }
}
