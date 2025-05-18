package model;

public class PortfolioEntry {
    private String ticker;
    private int quantity;
    private double avgPrice;
    private double currentPrice;

    public PortfolioEntry(String ticker, int quantity, double avgPrice){
        this.ticker = ticker;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.currentPrice = avgPrice;
    }
    public void updateOnBuy(int qty, double price) {
        double totalCost= this.avgPrice * this.quantity + price * qty;
        this.quantity +=qty;
        this.avgPrice = totalCost / this.quantity;
    }

    public void updateOnSell(int qty){
        this.quantity -=qty;
    }

    public String getTicker() {return ticker;}
    public int getQuantity() { return quantity;}
    public double getAvgPrice() { return avgPrice;}
    public double getCurrentPrice() { return currentPrice;}
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice;}
    public double getProfitLoss() { return (currentPrice - avgPrice)*quantity;}
    public double getProfitRate() { return (currentPrice - avgPrice)/avgPrice*100;}
    

}
