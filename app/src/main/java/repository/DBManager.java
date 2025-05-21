package repository;
// File: com/papertradefx/DBManager.java

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import model.PriceRecord;
import model.TransactionResult;
import model.OrderType;

/**
 * DBManager handles database connections and operations.
 */
public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:data/simulation.db";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            // ── WAL 설정을 “한 번만” 해 주고 닫는다
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = NORMAL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a database connection with WAL mode enabled.
     * @return Connection object
     * @throws SQLException if an error occurs
     */
    /** 트랜잭션 전용 커넥션 반환 (PRAGMA는 이미 static 초기화에서 처리됨) */
    private static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * Creates required tables if they do not exist.
     * @throws SQLException if an SQL error occurs
     */
    public static void createTables() throws SQLException {
        String priceHistorySQL = """
            CREATE TABLE IF NOT EXISTS PriceHistory (
              ticker TEXT NOT NULL,
              date TEXT NOT NULL,
              open REAL,
              high REAL,
              low REAL,
              close REAL,
              volume INTEGER,
              PRIMARY KEY (ticker, date)
            );
            """;
        String transactionsSQL = """
            CREATE TABLE IF NOT EXISTS Transactions (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              ticker TEXT NOT NULL,
              date TEXT NOT NULL,
              type TEXT NOT NULL,
              qty INTEGER NOT NULL,
              price REAL NOT NULL,
              executed_qty INTEGER NOT NULL,
              executed_price REAL NOT NULL
            );
            """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(priceHistorySQL);
            stmt.execute(transactionsSQL);
            conn.commit();
        }
    }

    /**
     * Checks if PriceHistory table is empty.
     * @return true if empty
     * @throws SQLException if an SQL error occurs
     */
    public static boolean isPriceHistoryEmpty() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PriceHistory";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    /**
     * Loads price history from a CSV file.
     * @param filePath path to CSV
     * @return list of PriceRecord
     * @throws IOException if IO error
     */
    public static List<PriceRecord> loadPriceHistoryFromCSV(String filePath) throws IOException {
        List<PriceRecord> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",", -1);
                if (tokens.length < 6) continue;
                LocalDate date = LocalDate.parse(tokens[0], DATE_FORMAT);
                double open = Double.parseDouble(tokens[1]);
                double high = Double.parseDouble(tokens[2]);
                double low = Double.parseDouble(tokens[3]);
                double close = Double.parseDouble(tokens[4]);
                long volume = Long.parseLong(tokens[5]);
                String ticker = new File(filePath).getName().replace(".csv", "");
                list.add(new PriceRecord(ticker, date, open, high, low, close, volume));
            }
        }
        return list;
    }

    /**
     * Inserts batch of PriceRecord into DB.
     * @param records list of PriceRecord
     * @throws SQLException if an SQL error occurs
     */
    public static void batchInsertPriceHistory(List<PriceRecord> records) throws SQLException {
        String sql = "INSERT OR IGNORE INTO PriceHistory(ticker,date,open,high,low,close,volume) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PriceRecord r : records) {
                ps.setString(1, r.getTicker());
                ps.setString(2, r.getDate().format(DATE_FORMAT));
                ps.setDouble(3, r.getOpen());
                ps.setDouble(4, r.getHigh());
                ps.setDouble(5, r.getLow());
                ps.setDouble(6, r.getClose());
                ps.setLong(7, r.getVolume());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
    }

    /**
     * Inserts a transaction record.
     * @param tr TransactionResult object
     * @throws SQLException if an SQL error occurs
     */
    public static void insertTransaction(TransactionResult tr) throws SQLException {
        String sql = "INSERT INTO Transactions(ticker,date,type,qty,price,executed_qty,executed_price) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tr.getTicker());
            ps.setString(2, tr.getDate().format(DATE_FORMAT));
            ps.setString(3, tr.getType().name());
            ps.setInt(4, tr.getRequestedQuantity());
            ps.setDouble(5, tr.getRequestedPrice());
            ps.setInt(6, tr.getExecutedQuantity());
            ps.setDouble(7, tr.getExecutedPrice());
            ps.executeUpdate();
            conn.commit();
        }
    }

    /**
     * Clears all transactions.
     * @throws SQLException if an SQL error occurs
     */
    public static void clearTransactions() throws SQLException {
        String sql = "DELETE FROM Transactions";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            conn.commit();
        }
    }

    /**
     * Retrieves all transaction records.
     * @return list of TransactionResult
     * @throws SQLException if an SQL error occurs
     */
    public static List<TransactionResult> getAllTransactions() throws SQLException {
        List<TransactionResult> list = new ArrayList<>();
        String sql = "SELECT ticker,date,type,qty,price,executed_qty,executed_price FROM Transactions ORDER BY id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TransactionResult(
                    LocalDate.parse(rs.getString("date"), DATE_FORMAT),
                    rs.getString("ticker"),
                    OrderType.valueOf(rs.getString("type")),
                    rs.getDouble("price"),
                    rs.getInt("qty"),
                    rs.getDouble("executed_price"),
                    rs.getInt("executed_qty")
                ));
            }
        }
        return list;
    }

    /**
     * Gets the close price for a ticker on a given date.
     * @param ticker stock ticker
     * @param date date of price
     * @return close price
     * @throws SQLException if an SQL error occurs
     */
    public static double getClosePrice(String ticker, LocalDate date) throws SQLException {
        String sql = "SELECT close FROM PriceHistory WHERE ticker = ? AND date = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticker);
            ps.setString(2, date.format(DATE_FORMAT));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("close");
                }
            }
        }
        throw new SQLException("No price found for " + ticker + " on " + date);
    }

    /**
     * Retrieves available dates from PriceHistory.
     * @return list of LocalDate
     * @throws SQLException if an SQL error occurs
     */
    public static List<LocalDate> getAvailableDates() throws SQLException {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT date FROM PriceHistory ORDER BY date";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dates.add(LocalDate.parse(rs.getString("date"), DATE_FORMAT));
            }
        }
        return dates;
    }
}
