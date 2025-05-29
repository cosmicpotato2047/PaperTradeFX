package src;

import java.time.LocalDate;
import java.util.*;

/**
 * 매일의 주문을 처리하고 계좌/포트폴리오를 갱신하는 시뮬레이션 엔진.
 */
public class SimulationService {

    /**
     * 한 거래일의 주문을 실행하고, 체결된 거래 내역을 반환한다.
     * @param date 오늘 날짜
     * @param orders 오늘 입력된 주문 목록
     * @param stockDataMap 티커별 전일 시세 리스트
     * @param dayIndex 오늘에 해당하는 인덱스
     * @param cash 현금 잔고 (참조로 전달되지 않으므로 리턴값 처리)
     * @param portfolioMap 티커별 보유 현황
     * @return [새 잔고, 체결내역 리스트]
     */
    public static Object[] executeDay(
        LocalDate date,
        List<Order> orders,
        Map<String,List<StockData>> stockDataMap,
        int dayIndex,
        double cash,
        Map<String,PortfolioEntry> portfolioMap
    ) {
        List<Transaction> txs = new ArrayList<>();
        // 주문별 체결 로직
        for (Order o : orders) {
            StockData sd = stockDataMap.get(o.getTicker()).get(dayIndex);
            int executedQty = 0;
            double executedPrice = 0;
            if ("Buy".equals(o.getType()) && sd.getClose() <= o.getPrice()) {
                executedQty   = o.getQuantity();
                executedPrice = sd.getClose();
                cash -= executedQty * executedPrice;
                portfolioMap.get(o.getTicker()).buy(executedQty, executedPrice);
            }
            if ("Sell".equals(o.getType()) && sd.getClose() >= o.getPrice()) {
                executedQty   = o.getQuantity();
                executedPrice = sd.getClose();
                cash += executedQty * executedPrice;
                portfolioMap.get(o.getTicker()).sell(executedQty);
            }
            txs.add(new Transaction(date, o.getTicker(), o.getType(),
                                     o.getPrice(), o.getQuantity(),
                                     executedPrice, executedQty));
        }
        return new Object[]{cash, txs};
    }
}
