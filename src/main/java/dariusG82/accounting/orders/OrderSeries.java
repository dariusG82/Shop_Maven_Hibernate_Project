package dariusG82.accounting.orders;

public enum OrderSeries {
    PURCHASE("PO"),
    SALE("SF"),
    RETURN("RE");

    final String series;

    OrderSeries(String series){
        this.series = series;
    }

    public String getSeries() {
        return series;
    }
}
