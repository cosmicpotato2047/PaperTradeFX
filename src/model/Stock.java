package model;

import java.time.LocalDate;
public class Stock {
  private LocalDate date;
  private String ticker;
  private double open, high, low, close;
  private long volume;

  public Stock(LocalDate date, String ticker,
                double open, double high, double low,
                double close, long volume){
    this.date = date;
    this.ticker = ticker;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.volume = volume;
    }
    public LocalDate getDate() { return date; }
    public String getTicker() { return ticker; }
    public double getClose() { return close; }
}
