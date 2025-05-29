package src;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * 사용자가 입력한 매수/매도 주문을 담는 모델.
 */
public class Order {
    private final SimpleStringProperty type;
    private final SimpleStringProperty ticker;
    private final SimpleDoubleProperty price;
    private final SimpleIntegerProperty quantity;

    public Order(String type, String ticker, double price, int quantity) {
        this.type     = new SimpleStringProperty(type);
        this.ticker   = new SimpleStringProperty(ticker);
        this.price    = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    public String getType()      { return type.get(); }
    public String getTicker()    { return ticker.get(); }
    public double getPrice()     { return price.get(); }
    public int getQuantity()     { return quantity.get(); }
}
