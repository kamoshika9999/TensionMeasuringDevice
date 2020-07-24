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
import javafx.scene.layout.BorderPane;
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
    private Label hxvalueLB4;
    @FXML
    private Label hxvalueLB5;
    @FXML
    private Button calibrationMenuBT;
    @FXML
    private BorderPane chartPane;
    @FXML
    private Label judgmentLB;

	//HX711のチャンネル数
	static final int ch_cnt =2;
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
    	double[][] result= {{-1,-1},{-1,-1}};
		double[] aveValue= {0,0};
		double[] aveWeight = {0,0};
		final int rpeetCnt = 3;//3回平均を取る
		double[] tmpValue = new double[rpeetCnt];
		double[] maxValue= new double[ch_cnt];
		double[] minValue= new double[ch_cnt];
		maxValue[0] = 0;maxValue[1]=0;
		minValue[0] = 99999999;minValue[1]=99999999;
    	try {
    		for(int j=0;j<rpeetCnt;j++) {
	    		for(int i=0;i<ch_cnt;i++) {
			        hx[i].read();
			        //System.out.println("value="+hx[i].value);
			        //System.out.println("weight="+hx[i].weight);
			        tmpValue[i] = hx[i].value;
			        if( maxValue[i] < hx[i].value) maxValue[i] = hx[i].value;
			        if( minValue[i] > hx[i].value) minValue[i] = hx[i].value;
			        aveValue[i] += hx[i].value;
			        aveWeight[i] += hx[i].weight;
		        }
    		}
    		//測定のレンジが1000(約0.3g)を超えていたら結果は-1になる
    		boolean flg=true;
    		for(int i=0;i<ch_cnt;i++) {
    			if( maxValue[i] - minValue[i] > 1000) {
    				flg=false;
    			}
    		}
    		if( flg ) {
	    		for(int i=0;i<ch_cnt;i++) {
			        aveValue[i] /= rpeetCnt;
			        aveWeight[i] /= rpeetCnt;
	    		}
	    		for(int i=0;i<ch_cnt;i++) {
	    			result[i][0] = aveValue[i];
	    			result[i][1] = aveWeight[i];
	    		}
    		}
    		System.out.println("CH1 MAX="+maxValue[0]+" CH1 MIN="+minValue);

    	}catch(Exception e) {
    		System.out.println(e);
    	}
    	return result;
    }

    @FXML
    void onGetValueBT(ActionEvent event) {

    	double[][] result = getLoadCellValue();

    	if( result[0][0] != -1 ) {
	    	Platform.runLater(() ->hxvalueLB1.setText(String.valueOf(result[0][0])));
	    	Platform.runLater(() ->hxvalueLB2.setText(String.valueOf(result[0][1])));

	    	Platform.runLater(() ->hxvalueLB4.setText(String.valueOf(result[1][0])));
	    	Platform.runLater(() ->hxvalueLB5.setText(String.valueOf(result[1][1])));
    	}

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
        //キャリブレーションデーターロード
        csvSaveLoad.calibDataLoad(
        		CaliblationController.emptyValue,
        		CaliblationController.calibValue,
        		CaliblationController.calibWeight,
        		CaliblationController.resolution);

        for(int i=0;i<ch_cnt;i++) {
	    	hx[i].emptyValue =  CaliblationController.emptyValue[i];
	    	hx[i].calibrationValue = CaliblationController.calibValue[i];
	    	hx[i].calibrationWeight =CaliblationController.calibWeight[i];
	    	hx[i].resolution = CaliblationController.resolution[i];
    	}
    }

}

