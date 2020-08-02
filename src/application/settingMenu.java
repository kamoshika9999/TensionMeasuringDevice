package application;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    @FXML
    private Label ch2TareLB;
    @FXML
    private Button ch2TareBT;
    @FXML
    private Label ch1TareLB;
    @FXML
    private Button ch1TareBT;
    @FXML
    private Label ch1RawValueLB;
    @FXML
    private Label ch2RawValueLB;
    @FXML
    private CheckBox demoChk;
    //クラス変数
    double tmpTareValueCh1,tmpTareValueCh2;

	//スレッドオブジェクト
	ScheduledExecutorService tr = Executors.newSingleThreadScheduledExecutor();
	Runnable tentionMesure;
	double[][] result = new double[2][2];

    //設定値
    public static double ch1RatioValue;//警告の比率 例)ch1MaxErroValue-(ch1MaxErroValue*ch1RatioValue)=上限警告
    public static long ch1MaxErrorValue;
    public static long ch1MinErrorValue;
    public static double ch2RatioValue;
    public static long ch2MaxErrorValue;
    public static long ch2MinErrorValue;
    public static double ch1TareValue;
    public static double ch2TareValue;
    public static boolean demoMode = false;

    /**
     * 風袋リセット CH1
     * @param event
     */
    @FXML
    void onCh1BT(ActionEvent event) {
    	tmpTareValueCh1=result[0][2];
    }

    /**
     * 風袋リセット CH2
     * @param event
     */
    @FXML
    void onCh2BT(ActionEvent event) {
    	tmpTareValueCh2=result[1][2];
    }

    @FXML
    void onCalibCancelBT(ActionEvent event) {
       	try {
    			tr.shutdown();
    			tr.awaitTermination(33, TimeUnit.MICROSECONDS);
    		} catch(Exception e) {
    			System.out.println(e);
    		}
       	Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void onCalibSaveBT(ActionEvent event) {
       	try {
    			tr.shutdown();
    			tr.awaitTermination(33, TimeUnit.MICROSECONDS);
    		} catch(Exception e) {
    			System.out.println(e);
    		}
    	//保存処理
    	ch1RatioValue = Double.valueOf( this.ch1Ratio.getText() );
    	ch1MaxErrorValue = Long.valueOf( this.ch1MaxError.getText());
    	ch1MinErrorValue = Long.valueOf( this.ch1MinError.getText());
    	ch2RatioValue = Double.valueOf( this.ch2Ratio.getText() );
    	ch2MaxErrorValue = Long.valueOf( this.ch2MaxError.getText());
    	ch2MinErrorValue = Long.valueOf( this.ch2MinError.getText());
    	ch1TareValue = tmpTareValueCh1;
    	ch2TareValue = tmpTareValueCh2;

    	if( !csvSaveLoad.settingValueSave() ) {
    		System.out.println("settingSaveError");
    	}

    	if( demoChk.isSelected() ) {
    		demoMode = true;
    	}else {
    		demoMode = false;
    	}
    		

		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void initialize() {
    	tmpTareValueCh1 = ch1TareValue;
    	tmpTareValueCh2 = ch2TareValue;

    	Platform.runLater(() ->ch1MaxError.setText( String.valueOf(ch1MaxErrorValue)));
    	Platform.runLater(() ->ch1MinError.setText( String.valueOf(ch1MinErrorValue)));
    	Platform.runLater(() ->ch1Ratio.setText( String.valueOf(ch1RatioValue)));
    	Platform.runLater(() ->ch2MaxError.setText( String.valueOf(ch2MaxErrorValue)));
    	Platform.runLater(() ->ch2MinError.setText( String.valueOf(ch2MinErrorValue)));
    	Platform.runLater(() ->ch2Ratio.setText( String.valueOf(ch2RatioValue)));

    	tentionMesure = new Runnable() {
			@Override
  	 		   public void run() {
  				  if( !MainScreenController.mesureFlg ) {
  					result = MainScreenController.getLoadCellValue();
  					//デバッグコード------------------------------------------
  					if(MainScreenController.debugFlg) {
  						result[0][2] = 0.000;
  						result[1][2] = 0.000;
  					}
  					//--------------------------------------------------------
  					Platform.runLater(() ->ch1RawValueLB.setText(String.format("%.1f", result[0][2])));
  					Platform.runLater(() ->ch2RawValueLB.setText(String.format("%.1f", result[1][2])));

  					Platform.runLater(() ->ch1TareLB.setText(String.format("Tare=%.1f", result[0][2]-tmpTareValueCh1)));
  					Platform.runLater(() ->ch2TareLB.setText(String.format("Tare=%.1f", result[1][2]-tmpTareValueCh2)));
  				  }
			}
  	 	   };
  	 	   tr.scheduleAtFixedRate(tentionMesure, 0, 100, TimeUnit.MILLISECONDS);
    }
}
