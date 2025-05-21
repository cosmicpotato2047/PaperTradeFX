// File: com/papertradefx/controller/HelpController.java
package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Controller for help dialog.
 */
public class HelpController {

    @FXML private TextArea helpTextArea;
    @FXML private Button okButton;

    @FXML
    public void initialize() {
        String helpText = """
            중요: 거래 리스트를 빈 상태로 두고 Execute Trades를 누르면 당일 거래를 건너뛸 수 있습니다.
            계좌는 한 개로 가정합니다.
            수수료는 없음.
            거래 방식은 LOC (Limit on Close) 입니다.
             • 매수: 종가 ≤ 설정 가격 → 체결 (종가)
             • 매도: 종가 ≥ 설정 가격 → 체결 (종가)
            미체결은 executed_qty = 0 으로 기록
            최초 예수금: $5000.00
            """;
        helpTextArea.setText(helpText);
    }

    @FXML
    private void handleOk() {
        ((Stage) okButton.getScene().getWindow()).close();
    }
}

