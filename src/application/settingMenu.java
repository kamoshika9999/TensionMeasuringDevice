package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;

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

    //設定値
    public static double ch1RatioValue;
    public static long ch1MaxErrorValue;
    public static long ch1MinErrorValue;
    public static double ch2RatioValue;
    public static long ch2MaxErrorValue;
    public static long ch2MinErrorValue;

    @FXML
    void onCalibCancelBT(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void onCalibSaveBT(ActionEvent event) {
    	//保存処理
    	ch1RatioValue = Double.valueOf( this.ch1Ratio.getText() );
    	ch1MaxErrorValue = Long.valueOf( this.ch1MaxError.getText());
    	ch1MinErrorValue = Long.valueOf( this.ch1MinError.getText());
    	ch2RatioValue = Double.valueOf( this.ch2Ratio.getText() );
    	ch2MaxErrorValue = Long.valueOf( this.ch2MaxError.getText());
    	ch2MinErrorValue = Long.valueOf( this.ch2MinError.getText());


    	if( !csvSaveLoad.settingValueSave() ) {
    		System.out.println("settingSaveError");
    	}
    	//

		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void initialize() {

    }
}
