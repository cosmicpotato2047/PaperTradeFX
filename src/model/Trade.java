package model;

import java.time.LocalDate;

public class Trade {
    private LocalDate date;
    private String ticker;
    private String type; //Buy, Sell, Skip
    private double price;
    private int quantity;
    private double totalValue;
    private Double result;

    public Trade(String ticker, String type, double price, int quantity){
        this.ticker = ticker;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.totalValue = price * quantity;
    }

    public void setDate(LocalDate date) { this.date = date; }
    public LocalDate getDate() { return date; }
    public String getTicker() {return ticker;}
    public String getType() {return type;}
    public double getPrice() {return price;}
    public int getQuantity() { return quantity;}
    public double getTotalValue() {return totalValue;}
    public Double getResult() {return result;}
    public void setResult(Double result) {this.result = result;}
    
}
