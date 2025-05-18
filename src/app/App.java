package app;

import model.DBManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/view/trade.fxml"));
        stage.setTitle("Stock Simulator");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) throws Exception {
        
        DBManager.createTables();

        if (DBManager.isStocksTableEmpty()) {
            DBManager.insertStockCSV("TQQQ", "data/TQQQ.csv");
            DBManager.insertStockCSV("UPRO", "data/UPRO.csv");
            DBManager.insertStockCSV("SOXL", "data/SOXL.csv");
        } else {
            System.out.println("ℹ️ stocks 테이블에 데이터가 이미 존재합니다.");
        }

        launch(args);
    }
}
