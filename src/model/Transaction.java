package model;

import java.time.LocalDate;

public class Transaction {
    private LocalDate date;
    private String ticker;
    private String type;      // "Buy", "Sell", "Skip"
    private double price;
    private int quantity;
    private double totalValue;
    private Double result;    // null 또는 손익

    // 생성자
    public Transaction(LocalDate date, String ticker,
                       String type, double price, int quantity, Double result) {
        this.date       = date;
        this.ticker     = ticker;
        this.type       = type;
        this.price      = price;
        this.quantity   = quantity;
        this.totalValue = price * quantity;
        this.result     = result;
    }

    // **여기에 반드시 추가해야 할 getter들**
    public LocalDate getDate()       { return date; }
    public String    getTicker()     { return ticker; }
    public String    getType()       { return type; }
    public double    getPrice()      { return price; }
    public int       getQuantity()   { return quantity; }
    public double    getTotalValue(){ return totalValue; }
    public Double    getResult()     { return result; }
}
