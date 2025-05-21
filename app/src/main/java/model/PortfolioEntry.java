// File: com/papertradefx/model/PortfolioEntry.java
package model;

/**
 * PortfolioEntry represents current portfolio status for a ticker.
 */
public class PortfolioEntry {
    private final String ticker;
    private final int quantity;
    private final double averagePrice;
    private final double currentPrice;

    /**
     * Constructs a PortfolioEntry.
     * @param ticker stock ticker
     * @param quantity held quantity
     * @param averagePrice average buy price
     * @param currentPrice current closing price
     */
    public PortfolioEntry(String ticker, int quantity, double averagePrice, double currentPrice) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.currentPrice = currentPrice;
    }

    /** @return ticker */
    public String getTicker() { return ticker; }
    /** @return held quantity */
    public int getQuantity() { return quantity; }
    /** @return average buy price */
    public double getAveragePrice() { return averagePrice; }
    /** @return current price */
    public double getCurrentPrice() { return currentPrice; }

    /** @return market value (quantity * currentPrice) */
    public double calculateMarketValue() { return quantity * currentPrice; }

    /** @return profit (marketValue - cost) */
    public double calculateProfit() { return calculateMarketValue() - (quantity * averagePrice); }

    /** @return profit percentage */
    public double calculateProfitPercentage() {
        double cost = quantity * averagePrice;
        return cost > 0 ? calculateProfit() / cost * 100.0 : 0.0;
    }
}
