package src;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;

import java.time.LocalDate;
import java.util.*;

/**
 * trade.fxml 에 선언된 UI를 제어하는 컨트롤러.
 */
public class MainController {

    @FXML
    private Label dateLabel, cashLabel;
    @FXML
    private ToggleButton tqqqToggleButton, uproToggleButton, soxlToggleButton;
    @FXML
    private LineChart<String, Number> priceLineChart;
    @FXML
    private ChoiceBox<String> tradeTypeChoiceBox, tickerChoiceBox;
    @FXML
    private TextField priceTextField, quantityTextField;
    @FXML
    private TableView<Order> orderTableView;
    @FXML
    private TableColumn<Order, String> tradeTypeColumn, tickerColumn;
    @FXML
    private TableColumn<Order, Double> priceColumn;
    @FXML
    private TableColumn<Order, Integer> quantityColumn;
    @FXML
    private TableColumn<Order, Void> actionColumn;
    @FXML
    private Button addOrderButton, executeTradesButton;
    @FXML
    private TableView<PortfolioEntry> accountStatusTableView;
    @FXML
    private PieChart cashVsInvestedPieChart, holdingProportionPieChart;
    @FXML
    private LineChart<String, Number> profitLossLineChart;
    @FXML
    private ListView<String> historyListView;

    private Map<String, List<StockData>> stockDataMap = new HashMap<>();
    private Map<String, PortfolioEntry> portfolioMap = new HashMap<>();
    private List<String> tickers = Arrays.asList("TQQQ", "UPRO", "SOXL");
    private int dayIndex = 0;
    private double cash = 5000.0;

    @FXML
    private void onHelp() {
    StringBuilder sb = new StringBuilder();
    sb.append("• 거래를 건너뛰려면 주문 리스트를 비워두고 Execute Trades를 누르세요.\n")
      .append("• 계좌는 단일 계좌로 가정합니다.\n")
      .append("• 수수료는 적용하지 않습니다.\n\n")
      .append("● LOC(Limit on Close) 거래 방식\n")
      .append("  매수(Buy)\n")
      .append("    - 조건: 종가 ≤ 지정가\n")
      .append("    - 체결 가격: 종가\n")
      .append("  매도(Sell)\n")
      .append("    - 조건: 종가 ≥ 지정가\n")
      .append("    - 체결 가격: 종가\n")
      .append("  ※ LOC 미충족 주문은 무시되며, 체결 수량은 0으로 기록됩니다.\n\n")
      .append("• 최초 예수금: $5,000\n")
      .append("• 언제든 “info” 버튼으로 이 안내를 확인할 수 있습니다.");

    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("시뮬레이터 가정 및 거래 방식");
    alert.setHeaderText(null);
    alert.setContentText(sb.toString());
    alert.showAndWait();
}

    @FXML
    private void initialize() {
        try {
            // CSV 로딩
            for (String t : tickers) {
                stockDataMap.put(t, CSVUtil.readStockDataForTicker(t));
                portfolioMap.put(t, new PortfolioEntry(t));
            }
        } catch (Exception e) {
            showError("CSV 로드 오류", e.getMessage());
        }

        // UI 초기화
        dateLabel.setText(todayLabel());
        cashLabel.setText(String.format("$%.2f", cash));

        // 토글 버튼 체인
        ToggleGroup tg = new ToggleGroup();
        tqqqToggleButton.setToggleGroup(tg);
        uproToggleButton.setToggleGroup(tg);
        soxlToggleButton.setToggleGroup(tg);
        tqqqToggleButton.setSelected(true);
        tg.selectedToggleProperty().addListener((obs, o, n) -> updateChart(selectedTicker()));

        // priceLineChart
        priceLineChart.getData().clear();
        updateChart("TQQQ");

        // ChoiceBox
        tradeTypeChoiceBox.getItems().addAll("Buy", "Sell");
        tradeTypeChoiceBox.setValue("Buy");
        tickerChoiceBox.getItems().addAll(tickers);
        tickerChoiceBox.setValue("TQQQ");

        // 주문 테이블
        tradeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        tickerColumn.setCellValueFactory(new PropertyValueFactory<>("ticker"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        addActionColumn();

        // 체결차트 초기화
        profitLossLineChart.getData().add(new XYChart.Series<>());

        // Details 버튼 컬럼 팩토리
        TableColumn<PortfolioEntry, Void> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Details");
            {
                btn.setOnAction(e -> {
                    // 해당 행의 PortfolioEntry를 꺼내 팝업 띄우기
                    PortfolioEntry pe = getTableView().getItems().get(getIndex());
                    showDetails(pe);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        accountStatusTableView.getColumns().add(detailsCol);

    }

    /** @return 현재 선택된 토글의 티커 */
    private String selectedTicker() {
        if (uproToggleButton.isSelected())
            return "UPRO";
        if (soxlToggleButton.isSelected())
            return "SOXL";
        return "TQQQ";
    }

    /** 차트에 가격 시계열을 갱신한다. */
    private void updateChart(String ticker) {
        List<StockData> data = stockDataMap.get(ticker);
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (int i = 0; i <= dayIndex && i < data.size(); i++) {
            s.getData().add(new XYChart.Data<>(data.get(i).getDate().toString(), data.get(i).getClose()));
        }
        priceLineChart.getData().setAll(s);
    }

    /** 주문 삭제 버튼 컬럼을 추가한다. */
    private void addActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.setOnAction(e -> {
                    getTableView().getItems().remove(getIndex());
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    /** 주문 추가 버튼 핸들러 */
    @FXML
    private void onAddOrder() {
        try {
            String type = tradeTypeChoiceBox.getValue();
            String t = tickerChoiceBox.getValue();
            double price = Double.parseDouble(priceTextField.getText());
            int qty = Integer.parseInt(quantityTextField.getText());
            if (price <= 0 || qty <= 0)
                throw new NumberFormatException();
            orderTableView.getItems().add(new Order(type, t, price, qty));
            
            priceTextField.clear();
            quantityTextField.clear();

        } catch (Exception e) {
            showError("입력 오류", "가격과 수량은 양수여야 합니다.");
        }
    }

    /** Execute Trades 버튼 핸들러 */
    @FXML
    private void onExecuteTrades() {
        List<Order> orders = new ArrayList<>(orderTableView.getItems());
        if (orders.isEmpty()) {
            Alert a = new Alert(AlertType.CONFIRMATION,
                    "거래 예정 리스트가 비어 있습니다.\n거래 없이 오늘 건너뛰시겠습니까?",
                    ButtonType.YES, ButtonType.NO);
            a.showAndWait().filter(b -> b == ButtonType.NO).ifPresent(b -> {
            });
            if (a.getResult() == ButtonType.YES) {
                nextDay();
            }
            return;
        }

        // 검증: 매수 총액 ≤ 현금, 매도 수량 ≤ 보유
        double buySum = orders.stream()
                .filter(o -> "Buy".equals(o.getType()))
                .mapToDouble(o -> o.getQuantity() * o.getPrice()).sum();
        if (buySum > cash) {
            showError("잔고 부족", "예수금: $" + cash + " 필요: $" + buySum);
            return;
        }
        for (Order o : orders) {
            if ("Sell".equals(o.getType())
                    && o.getQuantity() > portfolioMap.get(o.getTicker()).getQuantity()) {
                showError("보유 부족",
                        o.getTicker() + " 보유: " + portfolioMap.get(o.getTicker()).getQuantity()
                                + " 요청: " + o.getQuantity());
                return;
            }
        }

        // 체결
        LocalDate today = stockDataMap.get("TQQQ").get(dayIndex).getDate();
        Object[] res = SimulationService.executeDay(
                today, orders, stockDataMap, dayIndex, cash, portfolioMap);
        cash = (double) res[0];
        @SuppressWarnings("unchecked")
        List<Transaction> txs = (List<Transaction>) res[1];

        // UI 업데이트
        txs.forEach(t -> historyListView.getItems().add(0, t.toString()));
        orderTableView.getItems().clear();
        cashLabel.setText(String.format("$%.2f", cash));
        updateAccountTable();
        updatePieCharts();
        updatePLChart();

        nextDay();
    }

    /**
     * Details 버튼 클릭 시, 해당 종목의 전체 지표를 Alert로 표시합니다.
     */
    private void showDetails(PortfolioEntry pe) {
        String ticker = pe.getTicker();
        int qty = pe.getQuantity();
        double avg = pe.getAvgPrice();
        double current = stockDataMap.get(ticker).get(dayIndex).getClose();
        double invested = avg * qty;
        double evaluated = current * qty;
        double profit = evaluated - invested;
        double profitPct = invested == 0 ? 0 : profit / invested * 100;

        Alert dlg = new Alert(AlertType.INFORMATION);
        dlg.setTitle(ticker + " Details");
        dlg.setHeaderText(null);
        dlg.setContentText(
                String.format(
                        "Ticker: %s%n" +
                                "Quantity: %d%n" +
                                "Current Price: $%.2f%n" +
                                "Avg Price: $%.2f%n" +
                                "Invested Amount: $%.2f%n" +
                                "Evaluated Amount: $%.2f%n" +
                                "Profit: $%.2f%n" +
                                "Profit %%: %.2f%%",
                        ticker, qty, current, avg,
                        invested, evaluated, profit, profitPct));
        dlg.showAndWait();
    }

    /** 다음 거래일로 이동하고 날짜/차트 갱신 */
    private void nextDay() {
        dayIndex++;
        if (dayIndex >= stockDataMap.get("TQQQ").size()) {
            Alert a = new Alert(AlertType.CONFIRMATION,
                    "데이터 마지막에 도달했습니다.\n새 게임을 시작하시겠습니까?",
                    ButtonType.YES, ButtonType.NO);
            a.showAndWait().filter(b -> b == ButtonType.NO).ifPresent(b -> {
            });
            if (a.getResult() == ButtonType.YES) {
                dayIndex = 0;
                cash = 5000.0;
                portfolioMap.values().forEach(pe -> {
                    pe.sell(pe.getQuantity());
                });
                historyListView.getItems().clear();
            } else {
                dayIndex--;
                return;
            }
        }
        dateLabel.setText(todayLabel());
        updateChart(selectedTicker());
    }

    /** 계좌 상태 테이블 갱신 */
    private void updateAccountTable() {
        accountStatusTableView.getItems().setAll(portfolioMap.values());
    }

    /** 파이 차트 갱신 */
    private void updatePieCharts() {
        double invested = portfolioMap.values().stream()
                .mapToDouble(pe -> pe.getAvgPrice() * pe.getQuantity()).sum();
        cashVsInvestedPieChart.getData().setAll(
                new PieChart.Data("Cash", cash),
                new PieChart.Data("Invested", invested));
        List<PieChart.Data> hd = new ArrayList<>();
        portfolioMap.values().forEach(pe -> {
            if (pe.getQuantity() > 0)
                hd.add(new PieChart.Data(pe.getTicker(), pe.getQuantity()));
        });
        holdingProportionPieChart.getData().setAll(hd);
    }

    /** P/L 라인 차트 갱신 */
    private void updatePLChart() {
        double totalInvest = portfolioMap.values().stream()
                .mapToDouble(pe -> pe.getAvgPrice() * pe.getQuantity()).sum();
        double totalMV = portfolioMap.values().stream()
                .mapToDouble(pe -> {
                    StockData sd = stockDataMap.get(pe.getTicker()).get(dayIndex);
                    return sd.getClose() * pe.getQuantity();
                }).sum();
        double plPercent = totalInvest == 0 ? 0
                : (totalMV - totalInvest) / totalInvest * 100;
        XYChart.Series<String, Number> s = profitLossLineChart.getData().get(0);
        s.getData().add(new XYChart.Data<>(stockDataMap.get("TQQQ").get(dayIndex)
                .getDate().toString(), plPercent));
    }

    /** 오늘 날짜+경과일 문자열 */
    private String todayLabel() {
        LocalDate d = stockDataMap.get("TQQQ").get(dayIndex).getDate();
        return d + "  (+" + dayIndex + " trading days)";
    }

    /** 오류 메시지 다이얼로그 띄우기 */
    private void showError(String title, String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.showAndWait(); // 이제 정상 호출됩니다
    }
}
