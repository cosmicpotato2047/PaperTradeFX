// File: controller/MainController.java
package controller;

import model.Order;
import model.OrderType;
import model.PortfolioEntry;
import model.TransactionResult;
import repository.DBManager;
import service.SimulationContext;
import service.TradeProcessor;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for main simulation dashboard.
 */
public class MainController {

    @FXML private Label dateLabel;
    @FXML private ToggleButton tqqqToggleButton, uproToggleButton, soxlToggleButton;
    @FXML private LineChart<String, Number> priceLineChart;
    @FXML private Label cashLabel;
    @FXML private ChoiceBox<String> tradeTypeChoiceBox, tickerChoiceBox;
    @FXML private TextField priceTextField, quantityTextField;
    @FXML private Button addOrderButton, executeTradesButton;
    @FXML private TableView<Order> orderTableView;
    @FXML private TableColumn<Order, String> tradeTypeColumn;
    @FXML private TableColumn<Order, String> tickerColumn;
    @FXML private TableColumn<Order, Double> priceColumn;
    @FXML private TableColumn<Order, Integer> quantityColumn;
    @FXML private TableColumn<Order, Void> actionColumn;
    @FXML private TableView<PortfolioEntry> accountStatusTableView;
    @FXML private PieChart cashVsInvestedPieChart, holdingProportionPieChart;
    @FXML private LineChart<String, Number> profitLossLineChart;
    @FXML private ListView<String> historyListView;

    private final ObservableList<Order> pendingOrders = FXCollections.observableArrayList();
    private final TradeProcessor tradeProcessor = new TradeProcessor();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        tradeTypeChoiceBox.setItems(FXCollections.observableArrayList("BUY","SELL"));
        tradeTypeChoiceBox.setValue("BUY");
        tickerChoiceBox.setItems(FXCollections.observableArrayList("TQQQ","UPRO","SOXL"));
        tickerChoiceBox.setValue("TQQQ");

        tradeTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        tickerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTicker()));
        priceColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()).asObject());
        quantityColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject());

        orderTableView.setItems(pendingOrders);
        setupActionColumn();

        addOrderButton.setOnAction(e -> handleAddOrder());
        executeTradesButton.setOnAction(e -> handleExecuteTrades());

        updateDateLabel();
        updateCashLabel();
        updatePriceChart("TQQQ");
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
    private void handleAddOrder() {
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
    private void handleExecuteTrades() {
        if (pendingOrders.isEmpty()) {
            boolean skip = showConfirmation("No Orders", "No pending orders. Skip trading today?");
            if (!skip) return;
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
                if (tr.getType() == OrderType.BUY) cash -= tr.getExecutedQuantity() * tr.getExecutedPrice();
                else cash += tr.getExecutedQuantity() * tr.getExecutedPrice();
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
