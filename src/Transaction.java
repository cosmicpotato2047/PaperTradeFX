package src;


import java.time.LocalDate;

/**
 * 체결된 거래 내역을 담는 모델.
 */
public class Transaction {
    private LocalDate date;
    private String ticker, type;
    private double requestedPrice, executedPrice;
    private int requestedQty, executedQty;

    public Transaction(LocalDate date, String ticker, String type,
                       double requestedPrice, int requestedQty,
                       double executedPrice, int executedQty) {
        this.date           = date;
        this.ticker         = ticker;
        this.type           = type;
        this.requestedPrice = requestedPrice;
        this.requestedQty   = requestedQty;
        this.executedPrice  = executedPrice;
        this.executedQty    = executedQty;
    }

    @Override
    public String toString() {
        return String.format("%s | %s %s %d@%.2f → %d@%.2f",
            date, ticker, type, requestedQty, requestedPrice, executedQty, executedPrice);
    }
}
