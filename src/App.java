package src;


import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 메인 애플리케이션 클래스.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(new File("trade.fxml").toURI().toURL());
        stage.setTitle("Stock Simulator");
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * 애플리케이션 진입점.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
