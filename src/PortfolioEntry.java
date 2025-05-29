package src;


/**
 * 한 종목에 대한 보유 수량과 평균 가격을 관리하는 모델.
 */
public class PortfolioEntry {
    private String ticker;
    private int quantity;
    private double avgPrice;

    public PortfolioEntry(String ticker) {
        this.ticker   = ticker;
        this.quantity = 0;
        this.avgPrice = 0.0;
    }

    public String getTicker()   { return ticker; }
    public int getQuantity()    { return quantity; }
    public double getAvgPrice() { return avgPrice; }

    /**
     * 매수 체결 시 보유 수량과 평균 매수가를 갱신한다.
     */
    public void buy(int qty, double price) {
        double totalCost = this.avgPrice * this.quantity + price * qty;
        this.quantity += qty;
        this.avgPrice = totalCost / this.quantity;
    }

    /**
     * 매도 체결 시 보유 수량만 감소시킨다.
     */
    public void sell(int qty) {
        this.quantity = Math.max(0, this.quantity - qty);
        if (this.quantity == 0) {
            this.avgPrice = 0.0;
        }
    }
}
