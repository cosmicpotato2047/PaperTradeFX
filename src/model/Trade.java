package model;

import java.time.LocalDate;

/**
 * 도메인 모델: 한 건의 주식 거래를 나타냅니다.
 */
public class Trade {
    /**
     * 거래 유형
     */
    public enum Type {
        BUY, SELL, SKIP
    }

    private LocalDate date;       // 거래 일자 (시뮬레이션의 현재 날짜 주입)
    private String ticker;        // 종목명 (예: TQQQ)
    private Type type;            // 거래 유형 (BUY, SELL, SKIP)
    private double price;         // 지정가
    private int quantity;         // 수량
    private double totalValue;    // price * quantity
    private Double result;        // 매매 결과 손익 (매수 시 null 또는 0, 매도 시 계산된 손익)

    /**
     * 기본 생성자: 문자열 타입을 enum으로 변환하여 사용합니다.
     * date는 이후에 setDate()로 주입할 수 있습니다.
     * 
     * @param ticker   종목명
     * @param typeStr  "Buy", "Sell", "Skip"
     * @param price    거래 가격
     * @param quantity 거래 수량
     */
    public Trade(String ticker, String typeStr, double price, int quantity) {
        this.date       = null;
        this.ticker     = ticker;
        this.type       = Type.valueOf(typeStr.trim().toUpperCase());
        this.price      = price;
        this.quantity   = quantity;
        this.totalValue = price * quantity;
        this.result     = null;
    }

    /**
     * 전체 필드를 지정하는 생성자
     * 
     * @param date      거래 일자
     * @param ticker    종목명
     * @param type      거래 유형
     * @param price     거래 가격
     * @param quantity  거래 수량
     */
    public Trade(LocalDate date, String ticker, Type type, double price, int quantity) {
        this.date       = date;
        this.ticker     = ticker;
        this.type       = type;
        this.price      = price;
        this.quantity   = quantity;
        this.totalValue = price * quantity;
        this.result     = null;
    }

    // --- Getter / Setter ---

    /**
     * 거래 일자 설정
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * 거래 일자 반환
     */
    public LocalDate getDate() {
        return date;
    }

    public String getTicker() {
        return ticker;
    }

    public Type getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public Double getResult() {
        return result;
    }

    public void setResult(Double result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Trade{" +
            "date=" + date +
            ", ticker='" + ticker + '\'' +
            ", type=" + type +
            ", price=" + price +
            ", quantity=" + quantity +
            ", totalValue=" + totalValue +
            ", result=" + result +
            '}';
    }
}
