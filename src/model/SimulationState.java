package model;

import java.time.LocalDate;
import java.util.List;

// SimulationState는 시뮬레이션의 현재 날짜, 잔고, 보유 포지션을 캡슐화합니다.
public class SimulationState {
    private LocalDate date;
    private double balance;
    private List<PortfolioEntry> holdings;

    public SimulationState(LocalDate date, double balance, List<PortfolioEntry> holdings) {
        this.date     = date;
        this.balance  = balance;
        this.holdings = holdings;
    }

    // 시뮬레이션의 현재 거래 날짜를 반환합니다.
    public LocalDate getDate() {
        return date;
    }

    // 시뮬레이션의 계좌 잔고를 반환합니다.
    public double getBalance() {
        return balance;
    }

    // 시뮬레이션 재개 시 복원할 포트폴리오 보유 정보를 반환합니다.
    public List<PortfolioEntry> getHoldings() {
        return holdings;
    }
}
