package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;  
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.*;


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
          + "close REAL NOT NULL, "
          + "high REAL NOT NULL, "
          + "low REAL NOT NULL, "
          + "open REAL NOT NULL, "
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
        pstmt.setDouble(3, close);
        pstmt.setDouble(4, high);
        pstmt.setDouble(5, low);
        pstmt.setDouble(6, open);
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

  private Connection conn;
  public void connect() throws SQLException {
    conn = DriverManager.getConnection("jdbc:sqlite:data/stock.db");
  }
  public void initializeTransactions() throws SQLException{
    try (Statement stmt = conn.createStatement()){
      stmt.executeUpdate("DROP TABLE IF EXISTS transactions");
      stmt.executeUpdate(
        "CREATE TABLE transactions (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "date TEXT, ticker TEXT, type TEXT, " +
        "price REAL, quantity INTEGER, total_value REAL, result REAL)"
      );
    }
  }
  public List<LocalDate> getAvailableDates() throws SQLException {
    List<LocalDate> dates = new ArrayList<>();
    String sql = "SELECT DISTINCT date FROM stocks ORDER BY date";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)){
          while (rs.next()) {
            dates.add(LocalDate.parse(rs.getString("date")));
          }
        }
        return dates;
  }
  public Stock getStock(String ticker, LocalDate date) throws SQLException{
    String sql = "SELECT * FROM stocks WHERE ticker = ? AND date = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)){
      ps.setString(1, ticker);
      ps.setString(2, date.toString());
      try (ResultSet rs = ps.executeQuery()){
        if (rs.next()) {
          return new Stock(
            date,
            ticker,
            rs.getDouble("open"),
            rs.getDouble("high"),
            rs.getDouble("low"),
            rs.getDouble("close"),
            rs.getLong("volume")
          );
        }
      }
    }
    return null;
  }
  public void saveTransaction(Trade tx) throws SQLException {
        String sql = "INSERT INTO transactions (date,ticker,type,price,quantity,total_value,result) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tx.getDate().toString());
            ps.setString(2, tx.getTicker());
            ps.setString(3, tx.getType());
            ps.setDouble(4, tx.getPrice());
            ps.setInt(5, tx.getQuantity());
            ps.setDouble(6, tx.getTotalValue());
            if (tx.getResult() != null) {
                ps.setDouble(7, tx.getResult());
            } else {
                ps.setNull(7, Types.REAL);
            }
            ps.executeUpdate();
        }
    }
}
