package dariusG82.services.sql_lite_services;

public enum SQL_Query {
    SELECT_ALL_USERS("SELECT * FROM users"),
    SELECT_USER_BY_USERNAME("SELECT * FROM users WHERE username = "),
    DELETE_USER_BY_USERNAME("DELETE FROM users WHERE username = "),

    SELECT_ALL_CLIENTS("SELECT * FROM clients"),
    SELECT_CLIENT_BY_NAME("SELECT * FROM clients WHERE clientName = "),
    DELETE_CLIENT_BY_NAME("DELETE FROM clients WHERE clientName = "),

    SELECT_ALL_ITEMS("SELECT * FROM items"),
    SELECT_ITEM_BY_NAME("SELECT * FROM items WHERE itemName = "),

    SELECT_ALL_WAREHOUSE("SELECT * FROM warehouse"),
    SELECT_FROM_WAREHOUSE("SELECT * FROM warehouse WHERE itemName = "),

    SELECT_CASH_JOURNAL("SELECT * FROM cash_journal"),

    SELECT_PURCHASE_ORDERS("SELECT * FROM order_lines WHERE orderType = 'PURCHASE'"),
    GET_PURCHASE_ORDER_LINES("SELECT * FROM order_lines WHERE orderType = 'PURCHASE' AND orderID = "),
    GET_LAST_PURCHASE_ORDER_NR("SELECT MAX(orderID) FROM order_lines WHERE orderType = 'PURCHASE'"),

    SELECT_SALES_ORDERS("SELECT * FROM order_lines WHERE orderType = 'SALE'"),
    GET_SALES_ORDER_LINES("SELECT * FROM order_lines WHERE orderType = 'SALE' AND orderID = "),
    GET_LAST_SALES_ORDER_NR("SELECT MAX(orderID) FROM order_lines WHERE orderType = 'PURCHASE'"),

    SELECT_RETURN_ORDERS("SELECT * FROM order_lines WHERE orderType = 'RETURN'"),
    GET_RETURN_ORDER_LINES("SELECT * FROM order_lines WHERE orderType = 'RETURN' AND orderID = "),
    GET_LAST_RETURN_ORDER_NR("SELECT MAX(orderID) FROM order_lines WHERE orderType = 'PURCHASE'");

    final String query;

    SQL_Query(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
