// File: com/papertradefx/model/Order.java
package model;

/**
 * Order represents a pending order.
 */
public class Order {
    private final OrderType type;
    private final String ticker;
    private final double price;
    private final int quantity;

    /**
     * Constructs an Order.
     * @param type BUY or SELL
     * @param ticker stock ticker
     * @param price requested price
     * @param quantity requested quantity
     */
    public Order(OrderType type, String ticker, double price, int quantity) {
        this.type = type;
        this.ticker = ticker;
        this.price = price;
        this.quantity = quantity;
    }

    /** @return OrderType */
    public OrderType getType() { return type; }
    /** @return ticker */
    public String getTicker() { return ticker; }
    /** @return requested price */
    public double getPrice() { return price; }
    /** @return requested quantity */
    public int getQuantity() { return quantity; }
}
