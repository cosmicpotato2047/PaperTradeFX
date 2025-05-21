package service;
// File: com/papertradefx/TradeProcessor.java

import model.Order;
import model.TransactionResult;
import repository.DBManager;
import model.OrderType;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TradeProcessor executes pending orders.
 */
public class TradeProcessor {

    /**
     * Executes all pending orders for given date.
     * @param orders list of orders
     * @param date trading date
     * @return list of TransactionResult
     * @throws SQLException if database error occurs
     */
    public List<TransactionResult> executeTrades(List<Order> orders, LocalDate date) throws SQLException {
        List<TransactionResult> results = new ArrayList<>();
        for (Order order : orders) {
            double closePrice = DBManager.getClosePrice(order.getTicker(), date);
            int executedQty = 0;
            double executedPrice = 0;
            if (order.getType() == OrderType.BUY) {
                if (closePrice <= order.getPrice()) {
                    executedQty = order.getQuantity();
                    executedPrice = closePrice;
                }
            } else if (order.getType() == OrderType.SELL) {
                if (closePrice >= order.getPrice()) {
                    executedQty = order.getQuantity();
                    executedPrice = closePrice;
                }
            }
            TransactionResult tr = new TransactionResult(
                date,
                order.getTicker(),
                order.getType(),
                order.getPrice(),
                order.getQuantity(),
                executedPrice,
                executedQty
            );
            DBManager.insertTransaction(tr);
            results.add(tr);
        }
        return results;
    }
}
