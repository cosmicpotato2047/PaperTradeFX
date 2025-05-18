package model;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
public class TradeProcessor {
    private DBManager db;
    private Map<String, PortfolioEntry> portfolio = new HashMap<>();
    private double balance;
    private LocalDate currentDate;

    public TradeProcessor(DBManager db){
        this.db = db;
    }
    public void startNewSimulation (LocalDate date) throws SQLException {
        db.initializeTransactions();
        this.currentDate = date;
        this.balance = 5000.0;
        this.portfolio.clear();
    }

    public void processTrades(List<Trade> trades) throws SQLException {
        for (Trade t : trades){
            t.setDate(currentDate);
            Stock s=db.getStock(t.getTicker(), currentDate);
            double close = (s !=null) ? s.getClose() : 0;
            if ("Buy".equals(t.getType()) && close <=t.getPrice() && balance >= t.getTotalValue()) {
                balance -= t.getTotalValue();
                portfolio.compute(t.getTicker(), (k,v) -> {
                    if (v ==null) return new PortfolioEntry(k, t.getQuantity(), t.getPrice());
                    v.updateOnBuy(t.getQuantity(), t.getPrice());
                    return v;
                });
                t.setResult(null);
            }else if (" Sell".equals(t.getType()) && close >= t.getPrice()) {
                PortfolioEntry e = portfolio.get(t.getTicker());
                if (e != null && e.getQuantity() >= t.getQuantity()){
                    balance += t.getTotalValue();
                    e.updateOnSell(t.getQuantity());
                    t.setResult((t.getPrice() -e.getAvgPrice()) * t.getQuantity());
                }
            }
            db.saveTransaction(t);
        }
        currentDate = currentDate.plusDays(1);
    }

    public double getBalance() { return balance; }
    public LocalDate getCurrentDate() { return currentDate; }
    public Collection<PortfolioEntry> getPortfolio() { return portfolio.values(); }
}
