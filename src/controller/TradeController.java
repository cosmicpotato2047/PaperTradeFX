package controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TradeController {
    // FXML UI components
    @FXML
    private Button newSimButton;
    @FXML
    private Button contSimButton;
    @FXML
    private Button dateConfirmButton;
    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> tickerCombo;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private Button addTradeButton;
    @FXML
    private Button removeTradeButton;
    @FXML
    private Button executeButton;

    @FXML
    private TableView<Trade> pendingTradesTable;
    @FXML
    private TableColumn<Trade, String> colPendingTicker;
    @FXML
    private TableColumn<Trade, String> colPendingType;
    @FXML
    private TableColumn<Trade, Double> colPendingPrice;
    @FXML
    private TableColumn<Trade, Integer> colPendingQty;

    @FXML
    private LineChart<String, Number> priceChart;

    @FXML
    private TableView<PortfolioEntry> portfolioTable;
    @FXML
    private TableColumn<PortfolioEntry, String> colTicker;
    @FXML
    private TableColumn<PortfolioEntry, Integer> colQty;
    @FXML
    private TableColumn<PortfolioEntry, Double> colAvg;
    @FXML
    private TableColumn<PortfolioEntry, Double> colCurr;
    @FXML
    private TableColumn<PortfolioEntry, Double> colPL;
    @FXML
    private TableColumn<PortfolioEntry, Double> colPLpct;

    // Internal state
    private DBManager db;
    private TradeProcessor proc;
    private ObservableList<Trade> pendingTrades = FXCollections.observableArrayList();

    public void initialize() {

        setupUIBindings();

        try {
            db = new DBManager();
            db.connect();
            proc = new TradeProcessor(db);
        Platform.runLater(() -> {
            try{
            if (db.hasTransactions()) {
                // 이전 기록이 있으면 이어하기 vs 새 시작 경고창
                Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                        "이전 거래내역이 있습니다.\n이어 진행하시겠습니까?", ButtonType.YES, ButtonType.NO);
                a.setHeaderText("시뮬레이션 계속");
                Optional<ButtonType> choice = a.showAndWait();
                if (choice.orElse(ButtonType.NO) == ButtonType.YES) {
                    proc.continueSimulation();
                    applyDate(proc.getCurrentDate());
                } else {
                    // 새 사이클 팝업
                    showDateDialog();
                }
            } else {
                // 완전 처음 실행
                showDateDialog();
            }}catch (SQLException ex){
                ex.printStackTrace();
                showAlert("Database Error", ex.getMessage());
            }
        });

            // newSimButton 은 팝업 대신 다시 새 사이클 시작할 때만 사용
            newSimButton.setOnAction(e -> showDateDialog());

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Initialization Error", ex.getMessage());
            Platform.exit();
        }
    }

    /** 팝업 다이얼로그로 날짜를 고르게 하고, 선택된 날짜로 시뮬레이션 시작 */
    private void showDateDialog() {
        final List<LocalDate> valid;
        try {
            valid = db.loadAvailableDates();
        } catch (SQLException e) {
            showAlert("Database Error", "날짜 목록을 불러오는 중 오류가 발생했습니다:\n" + e.getMessage());
            return;
        }
        Dialog<LocalDate> dlg = new Dialog<>();
        dlg.setTitle("시뮬레이션 시작 날짜 선택");
        dlg.setHeaderText("거래를 시작할 날짜를 선택하세요");

        DatePicker dp = new DatePicker(LocalDate.now());
        // 유효 날짜만 선택 가능하도록 제한
        dp.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || !valid.contains(date));
            }
        });

        dlg.getDialogPane().setContent(new VBox(dp));
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(btn -> btn == ButtonType.OK ? dp.getValue() : null);

        Optional<LocalDate> res = dlg.showAndWait();
        if (res.isPresent()) {
            LocalDate chosen = res.get();
            try {
                proc.startNewSimulation(chosen);
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
                return;
            }
            applyDate(chosen);
        } else {
            // 취소하면 앱 종료
            Platform.exit();
        }
    }

    /** datePicker에 날짜 설정하고, 읽기 전용(disabled)로 막기 */
    private void applyDate(LocalDate date) {
        datePicker.setValue(date);
        datePicker.setDisable(true);
        newSimButton.setDisable(false); // 새 시뮬 버튼은 활성
        updateAllViews();
    }

    private void setupUIBindings() {
        // ComboBoxes
        tickerCombo.getItems().setAll("TQQQ", "UPRO", "SOXL");
        tickerCombo.getSelectionModel().selectFirst();
        typeCombo.getItems().setAll("Buy", "Sell", "Skip");
        typeCombo.getSelectionModel().selectFirst();

        // Pending trades table
        colPendingTicker.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTicker()));
        colPendingType.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getType().toString()));
        colPendingPrice.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPrice()));
        colPendingQty.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        pendingTradesTable.setItems(pendingTrades);

        // Portfolio table
        colTicker.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTicker()));
        colQty.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        colAvg.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getAvgPrice()));
        colCurr.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCurrentPrice()));
        colPL.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getProfitLoss()));
        colPLpct.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getProfitRate()));
        portfolioTable.setItems(FXCollections.observableArrayList());
    }

    private void updateAllViews() {
        updatePendingTradesTable();
        // updatePortfolioTable(proc.getHoldingsList());
        // updatePriceChart(db.loadStocks(datePicker.getValue(), tickerCombo.getValue()));
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }

    @FXML
    private void handleNewSimulation() {
        LocalDate chosen = datePicker.getValue();
        // 1) 날짜 유효성 검사
        try {
            if (!db.isDateValid(chosen)) {
                showAlert("Invalid Date", "선택된 날짜에 데이터가 없습니다.");
                return;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "날짜 검증 중 오류가 발생했습니다.");
            return;
        }

        // 2) 시뮬레이션 초기화
        try {
            proc.startNewSimulation(chosen);
        } catch (Exception e) {
            showAlert("Initialization Error", e.getMessage());
            return;
        }

        // 3) 화면 요소 초기화 및 바인딩
        pendingTrades.clear();
        newSimButton.setVisible(false);
        datePicker.setValue(chosen);
        updateAllViews(); // 모든 뷰(테이블, 차트 등) 갱신

        // (필요하면) 콤보박스 초기화, 테이블 컬럼 바인딩 등 추가 호출
    }

    @FXML
    private void handleContinueSimulation() {
        try {
            SimulationState state = db.loadSimulationState();
            proc.restoreState(state);
            updateAllViews();
        } catch (SQLException e) {
            showAlert("Error loading simulation state", e.getMessage());
        }
    }

    @FXML
    private void handleDateConfirm() {
        handleDateConfirm(datePicker.getValue(), false);
    }

    private void handleDateConfirm(LocalDate date, boolean forceNew) {
        try {
            if (!db.isDateValid(date)) {
                showAlert("Invalid Date", "Selected date has no stock data. Please choose another.");
                return;
            }
            if (forceNew)
                proc.startNewSimulation(date);
            else
                proc.setCurrentDate(date);
            pendingTrades.clear();
            updateAllViews();
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleAddTrade() {
        try {
            LocalDate tradeDate = datePicker.getValue();
            if (tradeDate == null) {
                showAlert("Input Error", "Please select a date.");
                return;
            }

            String tkr = tickerCombo.getValue();
            String type = typeCombo.getValue();
            double price = Double.parseDouble(priceField.getText());
            int qty = Integer.parseInt(quantityField.getText());

            // LocalDate 버전 생성자 사용
            Trade t = new Trade(tradeDate, tkr, Trade.Type.valueOf(type.toUpperCase()), price, qty);
            pendingTrades.add(t);
            updatePendingTradesTable();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Price and Quantity must be numeric.");
        }
    }

    @FXML
    private void handleRemoveTrade() {
        int idx = pendingTradesTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            pendingTrades.remove(idx);
            updatePendingTradesTable();
        }
    }

    @FXML
    private void handleExecuteTrades() {
        // ① 현재 시뮬레이션 날짜 가져오기
        LocalDate currentDate = datePicker.getValue();
        if (currentDate == null) {
            showAlert("Execution Error", "Please select a simulation date.");
            return;
        }

        // ② pendingTrades 각각에 현재 날짜 주입 후 처리
        pendingTrades.forEach(t -> t.setDate(currentDate));

        // ③ 처리 끝난 리스트 초기화
        pendingTrades.clear();
        updatePendingTradesTable();

        // ④ 포트폴리오·가격 차트 갱신
        updatePortfolioTable();
        updatePriceChart();

        // ⑤ 다음 거래일로 날짜 이동
        LocalDate nextDate = proc.advanceToNextDate(currentDate);
        System.out.println("[DEBUG] currentDate=" + currentDate + " nextDate=" + nextDate);
        if (nextDate != null) {
            datePicker.setValue(nextDate);
        } else {
            showAlert("Data Exhausted", "No more trading dates. Please refresh data.");
        }
    }

    private void updatePriceChart() {
        priceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try {
            List<Stock> history = db.getHistoricalPrices(
                    tickerCombo.getValue(), proc.getCurrentDate().minusDays(30), proc.getCurrentDate());
            for (Stock s : history) {
                series.getData().add(
                        new XYChart.Data<>(s.getDate().toString(), s.getClose()));
            }
        } catch (SQLException e) {
            showAlert("Chart Error", e.getMessage());
        }
        priceChart.getData().add(series);
    }

    private void updatePortfolioTable() {
        ObservableList<PortfolioEntry> list = FXCollections.observableArrayList(proc.getPortfolio());
        portfolioTable.setItems(list);
    }

    private void updatePendingTradesTable() {
        pendingTradesTable.setItems(FXCollections.observableArrayList(pendingTrades));
    }

}