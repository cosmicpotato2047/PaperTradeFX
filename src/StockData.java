package src;

import java.time.LocalDate;

/**
 * CSV 한 줄의 주식 시세 데이터를 담는 모델.
 */
public class StockData {
    private LocalDate date;
    private double open, high, low, close;
    private long volume;

    public StockData(LocalDate date, double open, double high, double low, double close, long volume) {
        this.date   = date;
        this.open   = open;
        this.high   = high;
        this.low    = low;
        this.close  = close;
        this.volume = volume;
    }

    public LocalDate getDate() { return date; }
    public double getOpen()    { return open; }
    public double getHigh()    { return high; }
    public double getLow()     { return low; }
    public double getClose()   { return close; }
    public long getVolume()    { return volume; }
}
