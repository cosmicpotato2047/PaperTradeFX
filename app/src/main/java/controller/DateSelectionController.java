// File: com/papertradefx/controller/DateSelectionController.java
package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import repository.DBManager;
import service.SimulationContext;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for date selection screen.
 */
public class DateSelectionController {

    @FXML private ComboBox<LocalDate> dateComboBox;
    @FXML private Button okButton;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        try {
            List<LocalDate> dates = DBManager.getAvailableDates();
            dateComboBox.getItems().addAll(dates);
            if (!dates.isEmpty()) {
                dateComboBox.setValue(dates.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles OK button click, opens main dashboard.
     * @param event ActionEvent
     */
    @FXML
    private void handleOk(ActionEvent event) {
        try {
            LocalDate selected = dateComboBox.getValue();
            SimulationContext.setStartDate(selected);
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = loader.load();
            Stage mainStage = new Stage();
            mainStage.setTitle("Stock Simulator");
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
