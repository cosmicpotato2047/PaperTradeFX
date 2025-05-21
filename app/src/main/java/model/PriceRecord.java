// File: com/papertradefx/model/PriceRecord.java
package model;

import java.time.LocalDate;

/**
 * PriceRecord represents a record in PriceHistory table.
 */
public class PriceRecord {
    private final String ticker;
    private final LocalDate date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;

    /**
     * Constructs a PriceRecord.
     * @param ticker stock ticker
     * @param date trading date
     * @param open opening price
     * @param high high price
     * @param low low price
     * @param close closing price
     * @param volume trading volume
     */
    public PriceRecord(String ticker, LocalDate date, double open, double high, double low, double close, long volume) {
        this.ticker = ticker;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    /** @return ticker */
    public String getTicker() { return ticker; }
    /** @return date */
    public LocalDate getDate() { return date; }
    /** @return open price */
    public double getOpen() { return open; }
    /** @return high price */
    public double getHigh() { return high; }
    /** @return low price */
    public double getLow() { return low; }
    /** @return close price */
    public double getClose() { return close; }
    /** @return volume */
    public long getVolume() { return volume; }
}
