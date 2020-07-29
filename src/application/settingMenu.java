package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class settingMenu {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Button settingSaveBT;
    @FXML
    private Button settingCancelBT;
    @FXML
    private TextField ch1Ratio;
    @FXML
    private TextField ch1MaxError;
    @FXML
    private TextField ch1MinError;
    @FXML
    private TextField ch2Ratio;
    @FXML
    private TextField ch2MaxError;
    @FXML
    private TextField ch2MinError;
    @FXML
    private Label ch1MaxWarning;
    @FXML
    private Label ch1MinWarning;
    @FXML
    private Label ch2MaxWarning;
    @FXML
    private Label ch2MinWarning;

    @FXML
    void onCalibCancelBT(ActionEvent event) {

    }

    @FXML
    void onCalibSaveBT(ActionEvent event) {

    }

    @FXML
    void initialize() {

    }
}
