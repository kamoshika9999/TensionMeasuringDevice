package application;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.GpioUtil;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;




public class MainScreenController {
	@FXML
	private ResourceBundle resources;
	@FXML
	private URL location;
    @FXML
    private Button getValueBT;
    @FXML
    private Label hxvalueLB1;
    @FXML
    private Label hxvalueLB2;
    @FXML
    private Label hxvalueLB3;
    @FXML
    private Label hxvalueLB4;
    @FXML
    private Label hxvalueLB5;
    @FXML
    private Label hxvalueLB6;
    @FXML
    private Button calibrationMenuBT;

	//HX711のチャンネル数
	final int ch_cnt =2;
    //HX711 接続ピンリスト BCM番号で指定 「gpio readall」 で物理ピンと確認すること
	final Pin[] pinNoDAT = {RaspiPin.GPIO_21,RaspiPin.GPIO_23};
	final Pin[] pinNoCLK = {RaspiPin.GPIO_22,RaspiPin.GPIO_24};
	//HX711 オブジェクト
	GpioPinDigitalInput[] pinHXDAT = new GpioPinDigitalInput[ch_cnt];
    GpioPinDigitalOutput[] pinHXCLK = new GpioPinDigitalOutput[ch_cnt];
    HX711[] hx = new HX711[ch_cnt];


    /**
     *
     * @return  double[ch][0] hx.value   double[ch][1] hx.weight
     */
    public double[][] getLoadCellValue(){
    	try {
    		double[] aveValue= {0,0};
    		double[] aveWeight = {0,0};
    		final int rpeetCnt = 2;

    		for(int j=0;j<rpeetCnt;j++) {
	    		for(int i=0;i<ch_cnt;i++) {
			        hx[i].read();
			        System.out.println("value="+hx[i].value);
			        System.out.println("weight="+hx[i].weight);
			        aveValue[i] += hx[i].value;
			        aveWeight[i] += hx[i].weight;
		        }
    		}
    		for(int i=0;i<ch_cnt;i++) {
		        aveValue[i] /= rpeetCnt;
		        aveWeight[i] /= rpeetCnt;
    		}

	        double[][] result= {{aveValue[0],aveWeight[0]},{aveValue[1],aveWeight[1]}};
	        return result;

    	}catch(Exception e) {
    		System.out.println(e);
    		double[][] result= {{-1,-1},{-1,-1}};
    		return result;

    	}
    }

    @FXML
    void onGetValueBT(ActionEvent event) {
    	for(int i=0;i<ch_cnt;i++) {
	    	hx[i].emptyValue =  CaliblationController.emptyValue[i];
	    	hx[i].calibrationValue = CaliblationController.calibValue[i];
	    	hx[i].calibrationWeight =CaliblationController.calibWeight[i];
    	}

    	double[][] result = getLoadCellValue();
    	Platform.runLater(() ->hxvalueLB1.setText(String.valueOf(result[0][0])));
    	Platform.runLater(() ->hxvalueLB2.setText(String.valueOf(result[0][1])));

    	Platform.runLater(() ->hxvalueLB4.setText(String.valueOf(result[1][0])));
    	Platform.runLater(() ->hxvalueLB5.setText(String.valueOf(result[1][1])));

    }

    @FXML
    void onCaliblationController(ActionEvent event) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("caliblation.fxml"));
		AnchorPane root = null;
		try {
			root = (AnchorPane) loader.load();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setResizable(false);

		CaliblationController.hx = this.hx;//参照を渡す
		//設定ウィンドウを開く
		stage.showAndWait();

    }
    @FXML
    void initialize() {
        GpioUtil.enableNonPrivilegedAccess();

        GpioController gpio = GpioFactory.getInstance();

        for(int i=0;i<ch_cnt;i++) {
	        pinHXDAT[i] = gpio.provisionDigitalInputPin(pinNoDAT[i],
	        		"HX_DAT"+String.valueOf(i), PinPullResistance.OFF);
	        pinHXCLK[i] = gpio.provisionDigitalOutputPin(pinNoCLK[i],
	        		"HX_CLK"+String.valueOf(i), PinState.LOW);
	        hx[i] = new HX711(pinHXDAT[i], pinHXCLK[i], 128);
        }

    }
}

