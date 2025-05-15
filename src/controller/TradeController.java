package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Trade;
import model.TradeProcessor;
import model.PortfolioEntry;

public class TradeController {

    // UI component와 연결될 변수들
    @FXML private ComboBox<String> tickerComboBox;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> typeComboBox; // "Buy", "Sell"
    @FXML private Button addTradeButton;
    @FXML private Button executeButton;
    @FXML private TableView<Trade> tradeListTable;
    @FXML private TableView<PortfolioEntry> portfolioTable;
    @FXML private LineChart<String, Number> priceChart;

    private ObservableList<Trade> tradeList = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        // 초기 설정: 티커 목록, 거래 타입 세팅
        tickerComboBox.getItems().addAll("TQQQ", "UPRO", "SOXL");
        typeComboBox.getItems().addAll("Buy", "Sell");

        tradeListTable.setItems(tradeList);
        // 포트폴리오 테이블 설정은 이후에
    }

    @FXML
    public void handleAddTrade() {
        String ticker = tickerComboBox.getValue();
        String type = typeComboBox.getValue();
        double price = Double.parseDouble(priceField.getText());
        int quantity = Integer.parseInt(quantityField.getText());

        Trade trade = new Trade(ticker, type, price, quantity);
        tradeList.add(trade);
    }

    @FXML
    
    public void handleExecuteTrades() {
        // 모든 거래 실행
        TradeProcessor.executeTrades(tradeList);

        tradeList.clear(); // 오늘 거래 초기화

        // 포트폴리오, 차트 등 갱신 필요
        updatePortfolioTable();
        updatePriceChart();
    }

    private void updatePortfolioTable() {
        // 포트폴리오 상태를 UI에 반영
    }

    private void updatePriceChart() {
        // 차트 갱신
    }
}
