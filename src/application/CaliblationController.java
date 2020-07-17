package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;

public class CaliblationController {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private TextField calibWeightTX_1;
    @FXML
    private TextField enptyValueTX_1;
    @FXML
    private TextField calibValueTX_1;
    @FXML
    private Label GetEmptyValueInfo_1;
    @FXML
    private Button GetEmptyValueBT_1;
    @FXML
    private Button GetCalibValueBT_1;
    @FXML
    private Button GetEmptyValueBT_2;
    @FXML
    private Button GetCalibValueBT_2;
    @FXML
    private Label GetCalibValueInfo_1;
    @FXML
    private Button calibSaveBT;
    @FXML
    private Button calibCancelBT;
    @FXML
    private TextField calibWeightTX_2;
    @FXML
    private TextField enptyValueTX_2;
    @FXML
    private TextField calibValueTX_2;
    @FXML
    private Label GetEmptyValueInfo_2;
    @FXML
    private Label GetCalibValueInfo_2;

    //クラス変数
    static long[] emptyValue = new long[2];
    static long[] calibValue = new long[2];
    static double[] calibWeight = new double[2];
    static HX711[] hx;//MainScreenControllerで作成されたオブジェクトの参照


    @FXML
    void onCalibCancelBT(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void onCalibSaveBT(ActionEvent event) {
    	emptyValue[0] = Long.valueOf( this.enptyValueTX_1.getText() );
    	calibValue[0] = Long.valueOf( this.calibValueTX_1.getText());
    	calibWeight[0] = Double.valueOf( this.calibWeightTX_1.getText());

    	emptyValue[1] = Long.valueOf( this.enptyValueTX_2.getText() );
    	calibValue[1] = Long.valueOf( this.calibValueTX_2.getText());
    	calibWeight[1] = Double.valueOf( this.calibWeightTX_2.getText());

		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }


    @FXML
    void onGetCalibValu_1(ActionEvent event) {
    	double reslut = getLoadCellValue(0,this.GetCalibValueInfo_1);
    	if( reslut > 0) {
    		Platform.runLater(() ->this.calibValueTX_1 .setText(
    				String.format("%d", reslut)));
    	}else {
    		Platform.runLater(() ->this.calibValueTX_1.setText("0"));
    	}

    }

    @FXML
    void onGetEmptyValueBT_1(ActionEvent event) {
    	double reslut = getLoadCellValue( 0, this.GetEmptyValueInfo_1);
    	if( reslut > 0) {
    		Platform.runLater(() ->this.enptyValueTX_1.setText(
    				String.format("%d", reslut)));
    	}else {
    		Platform.runLater(() ->this.enptyValueTX_1.setText("0"));
    	}
    }
    @FXML
    void onGetCalibValu_2(ActionEvent event) {
    	double reslut = getLoadCellValue(1,this.GetCalibValueInfo_2);
    	if( reslut > 0) {
    		Platform.runLater(() ->this.calibValueTX_2 .setText(
    				String.format("%d", reslut)));
    	}else {
    		Platform.runLater(() ->this.calibValueTX_2.setText("0"));
    	}
    }

    @FXML
    void onGetEmptyValueBT_2(ActionEvent event) {
    	double reslut = getLoadCellValue( 1, this.GetEmptyValueInfo_1);
    	if( reslut > 0 ) {
    		Platform.runLater(() ->this.enptyValueTX_1.setText(
    				String.format("%d", reslut)));
    	}else {
    		Platform.runLater(() ->this.enptyValueTX_1.setText("0"));
    	}
    }
    /**
    *
    * @return  double[chNo] hx.value
    */
   public double getLoadCellValue(int chNo,Label dstInfoLabel){
   	try {
   		double aveValue= 0.0;
   		final int rpeetCnt = (int)(30/0.1);

   		for(int j=0;j<rpeetCnt;j++) {
		        hx[chNo].read();
		        System.out.println("value="+hx[chNo].value);
		        aveValue += hx[chNo].value;
		        Platform.runLater(() ->dstInfoLabel.setText(String.valueOf(hx[chNo].value)));
   		}

		aveValue /= rpeetCnt;

        return aveValue;

   	}catch(Exception e) {
   		System.out.println(e);
   		return -1.0;

   	}
   }

   @FXML
   void onClose(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
   }

    @FXML
    void initialize() {
    	Platform.runLater(() ->this.enptyValueTX_1.setText(String.valueOf(this.emptyValue[0])));
    	Platform.runLater(() ->this.calibValueTX_1.setText(String.valueOf(this.calibValue[0])));
    	Platform.runLater(() ->this.calibWeightTX_1.setText(String.valueOf(this.calibWeight[0])));
    	Platform.runLater(() ->this.enptyValueTX_2.setText(String.valueOf(this.emptyValue[1])));
    	Platform.runLater(() ->this.calibValueTX_2.setText(String.valueOf(this.calibValue[1])));
    	Platform.runLater(() ->this.calibWeightTX_2.setText(String.valueOf(this.calibWeight[1])));
    }
}
