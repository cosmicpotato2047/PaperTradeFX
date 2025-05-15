package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
  public static void createTables() {
    String url = "jdbc:sqlite:data/stock.db";
    try {
        // ✅ 드라이버 수동 등록 (Java 8 안정성 보장)
        Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
        System.out.println("❌ 드라이버 로딩 실패");
        e.printStackTrace();
        return;
    }

    try (Connection conn = DriverManager.getConnection(url);
        Statement stmt = conn.createStatement()) {
      // stocks
      String createStocks = "CREATE TABLE IF NOT EXISTS stocks ("
          + "date TEXT NOT NULL, "
          + "ticker TEXT NOT NULL, "
          + "open REAL NOT NULL, "
          + "high REAL NOT NULL, "
          + "low REAL NOT NULL, "
          + "close REAL NOT NULL, "
          + "adj_close REAL NOT NULL, "
          + "volume INTEGER NOT NULL, "
          + "PRIMARY KEY (date, ticker)"
          + ");";

      // transactions
      String createTransactions = "CREATE TABLE IF NOT EXISTS transactions ("
          + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
          + "date TEXT NOT NULL, "
          + "ticker TEXT NOT NULL, "
          + "type TEXT CHECK(type IN ('Buy', 'Sell', 'Skip')) NOT NULL, "
          + "price REAL NOT NULL, "
          + "quantity INTEGER NOT NULL, "
          + "total_value REAL, "
          + "result REAL"
          + ");";

      // portfolio
      String createPortfolio = "CREATE TABLE IF NOT EXISTS portfolio ("
          + "ticker TEXT NOT NULL, "
          + "quantity INTEGER NOT NULL, "
          + "avg_price REAL NOT NULL, "
          + "current_price REAL"
          + ");";

      stmt.execute(createStocks);
      stmt.execute(createTransactions);
      stmt.execute(createPortfolio);
      System.out.println("Tables created successfully.");
    } catch (SQLException e) {
      System.out.println("Error creating tables: " + e.getMessage());
    }
  }
}
