package src;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 파일에서 StockData 리스트를 읽어오는 유틸리티.
 */
public class CSVUtil {
    /**
     * @param ticker "TQQQ", "UPRO", "SOXL"
     */
    public static List<StockData> readStockDataForTicker(String ticker) throws Exception {
        String path = "data/" + ticker + "_trunc.csv"; // 실행 디렉터리 기준 상대 경로
        return readStockData(path);
    }

    public static List<StockData> readStockData(String path) throws Exception {
        List<StockData> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String line = br.readLine(); // 헤더 건너뛰기
            while ((line = br.readLine()) != null) {
                String[] tok = line.split(",");
                LocalDate d = LocalDate.parse(tok[0]);
                double close = Double.parseDouble(tok[1]);
                double high = Double.parseDouble(tok[2]);
                double low = Double.parseDouble(tok[3]);
                double open = Double.parseDouble(tok[4]);
                // 1) 문자열에 소수점이 있을 수 있으므로 double로 먼저 파싱
                double volumeDouble = Double.parseDouble(tok[5].trim());
                // 2) 필요하다면 long으로 변환
                long volume = (long) volumeDouble;
                list.add(new StockData(d, open, high, low, close, volume));
            }
        }
        return list;
    }
}
