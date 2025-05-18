package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
public class TradeController {
  @FXML private Button newSimButton, contSimButton, dateConfirmButton;
  @FXML private DatePicker datePicker;
  @FXML private ComboBox<String> tickerCombo, typeCombo;
  @FXML private TextField priceField, quantityField;
  @FXML private Button addTradeButton, executeButton;
  @FXML private LineChart<String, Number> priceChart;
  @FXML private TableView<PortfolioEntry> portfolioTable;
  @FXML private TableColumn<PortfolioEntry, String> colTicker;
  @FXML private TableColumn<PortfolioEntry, Integer> colQty;
  @FXML private TableColumn<PortfolioEntry, Double> colAvg, colCurr, colPL, colPLpct;

  private DBManager db;
  private TradeProcessor proc;
  private List<Trade> pending = new ArrayList<>();
  public void initialize() {
    try {
        db = new DBManager(); db.connect();
        proc = new TradeProcessor(db);

        tickerCombo.getItems().addAll("TQQQ", "UPRO", "SOXL");
        typeCombo.getItems().addAll("Buy", "Sell", "Skip");

        colTicker.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTicker()));
        colQty.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        colAvg.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getAvgPrice()));
        colCurr.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCurrentPrice()));
        colPL.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getProfitLoss()));
        colPLpct.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getProfitRate()));

        portfolioTable.setItems(FXCollections.observableArrayList());
    } catch (Exception e) { e.printStackTrace(); }
  }
  @FXML private void handleNewSimulation(){
    LocalDate date = datePicker.getValue();
    try { proc.startNewSimulation(date); updateViews(); } catch (Exception e) { e.printStackTrace();}
  }
  @FXML private void handleAddTrade() {
    String tkr = tickerCombo.getValue();
    String type = typeCombo.getValue();
    double price = Double.parseDouble(priceField.getText());
    int qty = Integer.parseInt(quantityField.getText());
    pending.add(new Trade(tkr, type, price, qty));
  }
  @FXML private void handleExecuteTrades() {
    try {
        proc.processTrades(pending);
        pending.clear();
        updateViews();
    } catch (Exception e) { e.printStackTrace();}
  }
  private void updateViews() {
        priceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try {
            Stock s = db.getStock(tickerCombo.getValue(), proc.getCurrentDate());
            series.getData().add(new XYChart.Data<>(proc.getCurrentDate().toString(), s.getClose()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        priceChart.getData().add(series);

        ObservableList<PortfolioEntry> list = FXCollections.observableArrayList(proc.getPortfolio());
        portfolioTable.setItems(list);
    }
}
