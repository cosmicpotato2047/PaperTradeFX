package app;

import model.DBManager;

public class App {
    public static void main(String[] args) throws Exception {
        
        DBManager.createTables();

        if (DBManager.isStocksTableEmpty()) {
            DBManager.insertStockCSV("TQQQ", "data/TQQQ.csv");
            DBManager.insertStockCSV("UPRO", "data/UPRO.csv");
            DBManager.insertStockCSV("SOXL", "data/SOXL.csv");
        } else {
            System.out.println("ℹ️ stocks 테이블에 데이터가 이미 존재합니다.");
        }
    }
}
