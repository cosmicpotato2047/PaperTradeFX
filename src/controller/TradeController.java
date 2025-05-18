package controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
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
        try {
            // [1] DB & processor
            db = new DBManager();
            db.connect();
            proc = new TradeProcessor(db);

            // [2] 과거 거래가 있으면 "이어하기" 묻기
            if (db.hasTransactions()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Contine Simulation?");
                alert.setHeaderText("이전 시뮬레이션 기록이 발견되었습니다.");
                alert.setContentText("이어서 진행하시겠습니까?");
                ButtonType YES = new ButtonType("예", ButtonBar.ButtonData.YES);
                ButtonType NO = new ButtonType("아니오", ButtonBar.ButtonData.NO);
                alert.getButtonTypes().setAll(YES, NO);

                Optional<ButtonType> res = alert.showAndWait();
                if (res.isPresent() && res.get() == YES) {
                    // 이어하기
                    proc.continueSimulation();
                    datePicker.setValue(proc.getCurrentDate());
                    // 나머지 컨트롤 바인딩으로 넘어감
                } else {
                    // 새 시뮬레이션: 날짜 선택 버튼만 보이도록 종료
                    newSimButton.setVisible(true);
                    return;
                }
            } else {
                // 기록 없으면 바로 새 시뮬레이션 버튼 보이기
                newSimButton.setVisible(true);
                return;
            }

            // [3] 여기까지 왔으면 "이어하기"로 넘어온 상황
            newSimButton.setVisible(false);

            // DatePicker: restrict to valid dates
            List<LocalDate> validDates = db.loadAvailableDates();
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || !validDates.contains(date));
                }
            });

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

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Initialization Error", e.getMessage());
        }
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

    // View updates
    private void updateAllViews() {
        updatePriceChart();
        updatePortfolioTable();
        updatePendingTradesTable();
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

    // Utility
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}