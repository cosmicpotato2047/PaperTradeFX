// model/TradeProcessor.java
package model;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class TradeProcessor {
    private final DBManager db;
    private final Map<String, PortfolioEntry> portfolio = new HashMap<>();
    private double balance;
    private LocalDate currentDate;

    public TradeProcessor(DBManager db) {
        this.db = db;
    }

    /** FR-01: 새 거래 시작 */
    public void startNewSimulation(LocalDate date) throws SQLException {
        db.initializeTransactions();
        db.initializePortfolio();
        this.currentDate = date;
        this.balance = 5000.0;
        portfolio.clear();
    }

    /** FR-02: 상태 복원 */
    public void restoreState(SimulationState state) throws SQLException {
        this.currentDate = state.getDate();
        this.balance = state.getBalance();
        portfolio.clear();
        for (PortfolioEntry e : state.getHoldings()) {
            portfolio.put(e.getTicker(), e);
        }
    }

    /** FR-06: 날짜만 변경 */
    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    /** FR-11~14: 주문 처리 및 다음 날짜 이동 */
    public void processTrades(List<Trade> trades) throws SQLException {
        for (Trade t : trades) {
            t.setDate(currentDate);
            Stock s = db.getStock(t.getTicker(), currentDate);
            double close = (s != null) ? s.getClose() : 0.0;

            if ("Buy".equals(t.getType())
                && close <= t.getPrice()
                && balance >= t.getTotalValue()) {

                balance -= t.getTotalValue();
                portfolio.compute(t.getTicker(), (k, v) -> {
                    if (v == null) return new PortfolioEntry(k, t.getQuantity(), t.getPrice());
                    v.updateOnBuy(t.getQuantity(), t.getPrice());
                    return v;
                });
                t.setResult(null);

            } else if ("Sell".equals(t.getType())
                       && close >= t.getPrice()) {

                PortfolioEntry entry = portfolio.get(t.getTicker());
                if (entry != null && entry.getQuantity() >= t.getQuantity()) {
                    balance += t.getTotalValue();
                    entry.updateOnSell(t.getQuantity());
                    t.setResult((t.getPrice() - entry.getAvgPrice()) * t.getQuantity());
                }
            }

            db.saveTransaction(t);
        }

        // 다음 거래일로 포인터 이동 (FR-06)
        advanceToNextDate();
    }

    private void advanceToNextDate() throws SQLException {
        List<LocalDate> dates = db.loadAvailableDates();
        int idx = dates.indexOf(currentDate);
        if (idx >= 0 && idx + 1 < dates.size()) {
            currentDate = dates.get(idx + 1);
        } else {
            throw new SQLException("No more dates available after " + currentDate);
        }
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public double getBalance() {
        return balance;
    }

    /** FR-17: 포트폴리오 조회 */
    public Collection<PortfolioEntry> getPortfolio() {
        return portfolio.values();
    }
}
