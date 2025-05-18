package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class DBManager {
  private static final String URL = "jdbc:sqlite:data/stock.db";
  private Connection conn;

  // static 테이블 생성/CSV 삽입 메소드
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

  public void initializeTransactions() throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("DROP TABLE IF EXISTS transactions");
      stmt.executeUpdate(
          "CREATE TABLE transactions (" +
              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
              "date TEXT, ticker TEXT, type TEXT, " +
              "price REAL, quantity INTEGER, total_value REAL, result REAL)");
    }
  }

  /** 커넥션 열기 */
  public void connect() throws SQLException {
    conn = DriverManager.getConnection(URL);
  }

  /** 커넥션 닫기 (FR-19) */
  public void disconnect() {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException ignored) {
      }
    }
  }

  /** FR-01: 포트폴리오 테이블 초기화 */
  public void initializePortfolio() throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("DELETE FROM portfolio");
    }
  }

  /** FR-04: 거래 가능 날짜 조회 */
  public List<LocalDate> loadAvailableDates() throws SQLException {
    String sql = "SELECT DISTINCT date FROM stocks ORDER BY date";
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      List<LocalDate> dates = new ArrayList<>();
      while (rs.next()) {
        dates.add(LocalDate.parse(rs.getString("date")));
      }
      return dates;
    }
  }

  /** FR-05: 날짜 유효성 검증 */
  public boolean isDateValid(LocalDate date) throws SQLException {
    String sql = "SELECT 1 FROM stocks WHERE date = ? LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, date.toString());
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  /** FR-02: 시뮬레이션 상태 로드 */
  public SimulationState loadSimulationState() throws SQLException {
    // 1) 마지막 거래 날짜
    LocalDate date;
    String sqlDate = "SELECT MAX(date) FROM transactions";
    try (PreparedStatement ps = conn.prepareStatement(sqlDate);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next() && rs.getString(1) != null) {
        date = LocalDate.parse(rs.getString(1));
      } else {
        // 기록 없으면 첫 가능 날짜
        List<LocalDate> dates = loadAvailableDates();
        date = dates.isEmpty() ? LocalDate.now() : dates.get(0);
      }
    }
    // 2) 잔고 (예시: 5000 고정 혹은 별도 테이블에서 로드)
    double balance = 5000.0;
    // 3) 포트폴리오 보유
    List<PortfolioEntry> holdings = new ArrayList<>();
    String sqlPort = "SELECT ticker, quantity, avg_price FROM portfolio";
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sqlPort)) {
      while (rs.next()) {
        holdings.add(new PortfolioEntry(
            rs.getString("ticker"),
            rs.getInt("quantity"),
            rs.getDouble("avg_price")));
      }
    }
    return new SimulationState(date, balance, holdings);
  }

  /** FR-15: 특정 날짜 단일 종가 조회 (기존 getStock) */
  public Stock getStock(String ticker, LocalDate date) throws SQLException {
    String sql = "SELECT open, high, low, close, volume FROM stocks WHERE ticker = ? AND date = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, ticker);
      ps.setString(2, date.toString());
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Stock(
              date,
              ticker,
              rs.getDouble("open"),
              rs.getDouble("high"),
              rs.getDouble("low"),
              rs.getDouble("close"),
              rs.getLong("volume"));
        }
      }
    }
    return null;
  }

  /** FR-15 확장: 히스토리 차트용 가격 범위 조회 */
  public List<Stock> getHistoricalPrices(String ticker, LocalDate from, LocalDate to) throws SQLException {
    String sql = """
            SELECT date, open, high, low, close, volume
              FROM stocks
             WHERE ticker = ? AND date BETWEEN ? AND ?
             ORDER BY date
        """;
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, ticker);
      ps.setString(2, from.toString());
      ps.setString(3, to.toString());
      try (ResultSet rs = ps.executeQuery()) {
        List<Stock> list = new ArrayList<>();
        while (rs.next()) {
          list.add(new Stock(
              LocalDate.parse(rs.getString("date")),
              ticker,
              rs.getDouble("open"),
              rs.getDouble("high"),
              rs.getDouble("low"),
              rs.getDouble("close"),
              rs.getLong("volume")));
        }
        return list;
      }
    }
  }

  /** FR-12: 거래 기록 저장 */
  public void saveTransaction(Trade tx) throws SQLException {
    String sql = """
            INSERT INTO transactions
              (date, ticker, type, price, quantity, total_value, result)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tx.getDate().toString());
      ps.setString(2, tx.getTicker());
      ps.setString(3, tx.getType().toString());
      ps.setDouble(4, tx.getPrice());
      ps.setInt(5, tx.getQuantity());
      ps.setDouble(6, tx.getTotalValue());
      if (tx.getResult() != null)
        ps.setDouble(7, tx.getResult());
      else
        ps.setNull(7, Types.REAL);
      ps.executeUpdate();
    }
  }

  public LocalDate getNextDate(LocalDate currentDate) throws SQLException {
    String sql = """
            SELECT date
              FROM stocks
             WHERE date > ?
             ORDER BY date ASC
             LIMIT 1
        """;
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, currentDate.toString());
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return LocalDate.parse(rs.getString("date"));
        }
      }
    }
    return null;
  }

  /** 거래 기록이 하나라도 있는지 체크 */
  public boolean hasTransactions() throws SQLException {
    String sql = "SELECT COUNT(*) FROM transactions";
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      rs.next();
      return rs.getInt(1) > 0;
    }
  }

  /** 마지막(최신) 거래일을 반환, 없으면 null */
  public LocalDate getLastTransactionDate() throws SQLException {
    String sql = "SELECT MAX(date) FROM transactions";
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      if (rs.next()) {
        String d = rs.getString(1);
        return (d != null) ? LocalDate.parse(d) : null;
      }
    }
    return null;
  }

  /** 트랜잭션 테이블 초기화 */
  public void resetTransactions() throws SQLException {
    try (Statement st = conn.createStatement()) {
      st.execute("DELETE FROM transactions");
    }
  }
}