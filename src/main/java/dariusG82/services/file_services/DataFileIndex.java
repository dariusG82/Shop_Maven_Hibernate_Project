package dariusG82.services.file_services;

public enum DataFileIndex {
    CURRENT_DATE("0|"),
    PURCHASE_ORDER_NR_INFO("1|"),
    SALES_ORDER_NR_INFO("2|"),
    RETURN_ORDER_NR_INFO("3|"),
    CASH_REGISTER("4|"),
    BANK_ACCOUNT("5|");

    private final String index;

    DataFileIndex(String index) {
        this.index = index;
    }

    public java.lang.String getIndex() {
        return index;
    }
}
