package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*; // Label, Button, TableView, TableColumn, ListView, ChoiceBox, TextField, Alert, ToggleButton 등
import javafx.scene.control.cell.PropertyValueFactory;

import model.Order;
import model.OrderType;
import model.PortfolioEntry;
import model.TransactionResult;
import repository.DBManager;
import service.SimulationContext;
import service.TradeProcessor;

import java.time.LocalDate;
import java.util.List;


/**
 * Controller for main simulation dashboard.
 */
public class MainController {

    @FXML
    private Label dateLabel;
    @FXML
    private ToggleButton tqqqToggleButton, uproToggleButton, soxlToggleButton;
    @FXML
    private LineChart<String, Number> priceLineChart;
    @FXML
    private Label cashLabel;
    @FXML
    private ChoiceBox<String> tradeTypeChoiceBox, tickerChoiceBox;
    @FXML
    private TextField priceTextField, quantityTextField;
    @FXML
    private Button addOrderButton, executeTradesButton;
    @FXML
    private TableView<Order> orderTableView;
    @FXML
    private TableColumn<Order, String> tradeTypeColumn;
    @FXML
    private TableColumn<Order, String> tickerColumn;
    @FXML
    private TableColumn<Order, Double> priceColumn;
    @FXML
    private TableColumn<Order, Integer> quantityColumn;
    @FXML
    private TableColumn<Order, Void> actionColumn;
    @FXML
    private TableView<PortfolioEntry> accountStatusTableView;
    @FXML
    private PieChart cashVsInvestedPieChart, holdingProportionPieChart;
    @FXML
    private LineChart<String, Number> profitLossLineChart;
    @FXML
    private ListView<String> historyListView;

    private final ObservableList<Order> pendingOrders = FXCollections.observableArrayList();
    private final TradeProcessor tradeProcessor = new TradeProcessor();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        tradeTypeChoiceBox.setItems(FXCollections.observableArrayList("BUY", "SELL"));
        tradeTypeChoiceBox.setValue("BUY");
        tickerChoiceBox.setItems(FXCollections.observableArrayList("TQQQ", "UPRO", "SOXL"));
        tickerChoiceBox.setValue("TQQQ");

        tradeTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        tickerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTicker()));
        priceColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()).asObject());
        quantityColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject());

        orderTableView.setItems(pendingOrders);
        setupActionColumn();

        addOrderButton.setOnAction(e -> handleAddOrder(e));
        executeTradesButton.setOnAction(e -> handleExecuteTrades(e));

        updateDateLabel();
        updateCashLabel();
        updatePriceChart("TQQQ");
    }

    @FXML
    private void handleHelpDialog(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("도움말");
        alert.setHeaderText(null);
        alert.setContentText("- “중요” 그날 거래를 건너뛰고 싶다면 거래 예정 리스트를 빈 리스트로 두고 Execute Trades를 누르면 됩니다.\r\n" + //
                "- 계좌는 한 개로 가정하였습니다.\r\n" + //
                "- 수수료는 가정하지 않았습니다.\r\n" + //
                "- 거래 방식은 LOC(Limit on Close) 체결 방식을 채택하였습니다.\r\n" + //
                "    \r\n" + //
                "    매수 (Buy)\r\n" + //
                "    \r\n" + //
                "    - 조건: **종가 ≤ 설정한 가격**\r\n" + //
                "    - 체결 가격: 종가\r\n" + //
                "    \r\n" + //
                "    매도 (Sell)\r\n" + //
                "    \r\n" + //
                "    - 조건: **종가 ≥ 설정한 가격**\r\n" + //
                "    - 체결 가격: 종가\r\n" + //
                "    \r\n" + //
                "    LOC 체결 조건을 만족하지 못한 거래는 무시됩니다. \r\n" + //
                "    (거래 내역에 체결 수량이 0으로 기록)\r\n" + //
                "    \r\n" + //
                "- 최초 예수금은 5000$입니다.\r\n" + //
                "- 이 정보는 상단 툴바에서 “Help” 버튼을 누르면 다시 확인할 수 있습니다.");
        alert.showAndWait();
    }

    /**
     * Sets up delete action button in order table.
     */
    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    pendingOrders.remove(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
    }

    /** Handles adding an order to pending list. */
    @FXML
    private void handleAddOrder(ActionEvent event) {
        try {
            OrderType type = OrderType.valueOf(tradeTypeChoiceBox.getValue());
            String ticker = tickerChoiceBox.getValue();
            double price = Double.parseDouble(priceTextField.getText());
            int qty = Integer.parseInt(quantityTextField.getText());
            pendingOrders.add(new Order(type, ticker, price, qty));
        } catch (Exception e) {
            showAlert("Invalid input", "Please enter valid price and quantity.");
        }
    }

    /** Handles executing all pending trades. */
    @FXML
    private void handleExecuteTrades(ActionEvent event) {
        if (pendingOrders.isEmpty()) {
            boolean skip = showConfirmation("No Orders", "No pending orders. Skip trading today?");
            if (!skip)
                return;
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                LocalDate date = SimulationContext.getStartDate();
                tradeProcessor.executeTrades(pendingOrders, date);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            pendingOrders.clear();
            try {
                updateCashLabel();
                SimulationContext.advanceDate();
                updateDateLabel(); // Date: YYYY-MM-DD (Day N) 갱신
                updateCashLabel(); // 예수금 갱신
                updatePriceChart(tickerChoiceBox.getValue()); // 차트도 새 날짜 종가로 리프레시

                // TODO: implement refreshPortfolioView(), updateHistory(), advanceDate()
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        task.setOnFailed(e -> showAlert("Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    /** Updates the date label. */
    private void updateDateLabel() {
        LocalDate start = SimulationContext.getStartDate();
        dateLabel.setText("Date: " + start + " (Day 1)");
    }

    /** Updates the cash label. */
    private void updateCashLabel() {
        double cash = 5000.0;
        try {
            List<TransactionResult> transactions = DBManager.getAllTransactions();
            for (TransactionResult tr : transactions) {
                if (tr.getType() == OrderType.BUY)
                    cash -= tr.getExecutedQuantity() * tr.getExecutedPrice();
                else
                    cash += tr.getExecutedQuantity() * tr.getExecutedPrice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cashLabel.setText(String.format("Cash: $%.2f", cash));
    }

    /** Placeholder: updates price chart. */
    private void updatePriceChart(String ticker) {
        // TODO: implement
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
}
