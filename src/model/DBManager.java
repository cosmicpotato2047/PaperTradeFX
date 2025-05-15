package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;  
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.FileReader;

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

  public static void insertStockCSV(String ticker, String csvPath) {
    String url = "jdbc:sqlite:data/stock.db";
    String insertSQL = "INSERT INTO stocks(date, ticker, close, high, low, open, volume) VALUES (?,?,?,?,?,?,?)";

    try (Connection conn = DriverManager.getConnection(url);
        PreparedStatement pstmt = conn.prepareStatement(insertSQL);
        BufferedReader br = new BufferedReader(new FileReader(csvPath))) {

      String line = br.readLine();
      line = br.readLine();
      line = br.readLine();

      while ((line = br.readLine()) != null) {
        String[] tokens = line.split(",");
        if (tokens.length < 6)
          continue;

        String date = tokens[0];
        double close = Double.parseDouble(tokens[1]);
        double high = Double.parseDouble(tokens[2]);
        double low = Double.parseDouble(tokens[3]);
        double open = Double.parseDouble(tokens[4]);
        long volume = Long.parseLong(tokens[5]);

        pstmt.setString(1, date);
        pstmt.setString(2, ticker);
        pstmt.setDouble(3, open);
        pstmt.setDouble(4, high);
        pstmt.setDouble(5, low);
        pstmt.setDouble(6, close);
        pstmt.setLong(7, volume);
        pstmt.addBatch();
      }
      pstmt.executeBatch();
      System.out.println("✅ " + ticker + " 데이터 삽입 완료");

    } catch (Exception e) {
      System.out.println("❌ 주식 데이터 삽입 실패: " + ticker);
      e.printStackTrace();
    }
  }

  public static boolean isStocksTableEmpty() {
    String url = "jdbc:sqlite:data/stock.db";
    String sql = "SELECT COUNT(*) FROM stocks";

    try (Connection conn = DriverManager.getConnection(url);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      return rs.getInt(1) == 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return true; // 실패한 경우 일단 빈 것으로 간주
    }
  }

}
