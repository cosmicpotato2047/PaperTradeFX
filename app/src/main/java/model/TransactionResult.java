// File: com/papertradefx/model/TransactionResult.java
package model;

import java.time.LocalDate;

/**
 * TransactionResult represents execution result of an order.
 */
public class TransactionResult {
    private final LocalDate date;
    private final String ticker;
    private final OrderType type;
    private final double requestedPrice;
    private final int requestedQuantity;
    private final double executedPrice;
    private final int executedQuantity;

    /**
     * Constructs a TransactionResult.
     * @param date trade date
     * @param ticker stock ticker
     * @param type order type
     * @param requestedPrice requested price
     * @param requestedQuantity requested quantity
     * @param executedPrice execution price
     * @param executedQuantity executed quantity
     */
    public TransactionResult(LocalDate date, String ticker, OrderType type,
                             double requestedPrice, int requestedQuantity,
                             double executedPrice, int executedQuantity) {
        this.date = date;
        this.ticker = ticker;
        this.type = type;
        this.requestedPrice = requestedPrice;
        this.requestedQuantity = requestedQuantity;
        this.executedPrice = executedPrice;
        this.executedQuantity = executedQuantity;
    }

    /** @return date */
    public LocalDate getDate() { return date; }
    /** @return ticker */
    public String getTicker() { return ticker; }
    /** @return OrderType */
    public OrderType getType() { return type; }
    /** @return requested price */
    public double getRequestedPrice() { return requestedPrice; }
    /** @return requested quantity */
    public int getRequestedQuantity() { return requestedQuantity; }
    /** @return executed price */
    public double getExecutedPrice() { return executedPrice; }
    /** @return executed quantity */
    public int getExecutedQuantity() { return executedQuantity; }
}
