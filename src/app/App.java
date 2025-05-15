package app;
import model.DBManager;
public class App {
    public static void main(String[] args) throws Exception {
        DBManager.createTables();
    }
}
