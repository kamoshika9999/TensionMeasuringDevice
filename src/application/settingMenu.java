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
    private Label infoLB;
    @FXML
    private CheckBox demoChk;
    @FXML
    private CheckBox CH1SignInversion;
    @FXML
    private CheckBox CH2SignInversion;
    @FXML
    private TextField movingAverageTimeTX;
    @FXML
    private TextField graphXaxisTimeTX;


	//スレッドオブジェクト
	ScheduledExecutorService tr;
	Runnable tensionMesure;

    //設定値 CSVファイルに保存される値---------------------------------------------------------------------------------
    public static double[] ratioValue = new double[2];//警告の比率 例)ch1MaxErroValue-(ch1MaxErroValue*ch1RatioValue)=上限警告
    public static long[] maxErrorValue = new long[2];//テンションの上限閾値
    public static long[] minErrorValue = new long[2];//テンションの加減閾値
    public static double[] tareValue = new double[2];//風袋の値
    public static boolean[] signInversionFlg = new boolean[2];//テンションの符号
    public static boolean demoMode = false;
    public static double movingAverageTime;
    public static double graphXaxisTime;
    //-----------------------------------------------------------------------------------------------------------------
	double[][] result = new double[2][2];//計測値
    double[] tmpTareValue = new double[2];//風袋の確定前の値を一時保持用 SAVE実行時にtareValueに転送される
    boolean savingFlg = false;

    /**
     * 風袋リセット CH1
     * @param event
     */
    @FXML
    void onCh1BT(ActionEvent event) {
    	tmpTareValue[0]=result[0][2];
    }

    /**
     * 風袋リセット CH2
     * @param event
     */
    @FXML
    void onCh2BT(ActionEvent event) {
    	tmpTareValue[1]=result[1][2];
    }

    /**
     * キャリブレーション値キャンセル
     * @param event
     */
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

    /**
     * キャリブレーション値確定し保存
     * @param event
     */
    @FXML
    void onCalibSaveBT(ActionEvent event) {
    	if( savingFlg ) return;
    	savingFlg = true;

    	try {
    			tr.shutdown();
    			tr.awaitTermination(33, TimeUnit.MICROSECONDS);
    		} catch(Exception e) {
    			System.out.println(e);
    		}
    	//保存処理
       	try {
			ratioValue[0] = Double.valueOf( ch1Ratio.getText() );
			maxErrorValue[0] = Long.valueOf( ch1MaxError.getText());
			minErrorValue[0] = Long.valueOf( ch1MinError.getText());
			tareValue[0] = tmpTareValue[0];
			signInversionFlg[0] = CH1SignInversion.isSelected();

			ratioValue[1] = Double.valueOf( ch2Ratio.getText() );
			maxErrorValue[1] = Long.valueOf( ch2MaxError.getText());
			minErrorValue[1] = Long.valueOf( ch2MinError.getText());
			tareValue[1] = tmpTareValue[1];
			signInversionFlg[1] = CH2SignInversion.isSelected();

			movingAverageTime = Double.valueOf(movingAverageTimeTX.getText());
			graphXaxisTime = Double.valueOf(graphXaxisTimeTX.getText());
       	}catch(Exception e) {
       		//テキストの値が不正の場合
       		Platform.runLater(() ->infoLB.setText("入力された値が不正です"));
       		tr = Executors.newSingleThreadScheduledExecutor();
       		tr.scheduleAtFixedRate(tensionMesure, 0, 100, TimeUnit.MILLISECONDS);
       		savingFlg =false;
       		return;
       	}

    	if( !csvSaveLoad.settingValueSave() ) {
       		Platform.runLater(() ->infoLB.setText("保存に失敗しました"));
       		tr = Executors.newSingleThreadScheduledExecutor();
       		tr.scheduleAtFixedRate(tensionMesure, 0, 100, TimeUnit.MILLISECONDS);
       		savingFlg =false;
      		return;
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

    /**
     * 初期化
     */
    @FXML
    void initialize() {
    	//風袋の設定値を一時保存用変数にコピーする
    	tmpTareValue[0] = tareValue[0];
    	tmpTareValue[1] = tareValue[1];

    	Platform.runLater(() ->ch1MaxError.setText( String.valueOf(maxErrorValue[0])));
    	Platform.runLater(() ->ch1MinError.setText( String.valueOf(minErrorValue[0])));
    	Platform.runLater(() ->ch1Ratio.setText( String.valueOf(ratioValue[0])));
    	Platform.runLater(() ->CH1SignInversion.setSelected(signInversionFlg[0]));

    	Platform.runLater(() ->ch2MaxError.setText( String.valueOf(maxErrorValue[1])));
    	Platform.runLater(() ->ch2MinError.setText( String.valueOf(minErrorValue[1])));
    	Platform.runLater(() ->ch2Ratio.setText( String.valueOf(ratioValue[1])));
    	Platform.runLater(() ->CH2SignInversion.setSelected(signInversionFlg[1]));

    	Platform.runLater(() ->movingAverageTimeTX.setText(String.valueOf(movingAverageTime)));
    	Platform.runLater(() ->graphXaxisTimeTX.setText(String.valueOf(graphXaxisTime)));

    	//風袋測定用スレッド
    	tensionMesure = new Runnable() {
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
  					Platform.runLater(() ->ch1RawValueLB.setText(String.format("%.3f", result[0][2])));
  					Platform.runLater(() ->ch2RawValueLB.setText(String.format("%.3f", result[1][2])));

  					Platform.runLater(() ->ch1TareLB.setText(String.format("Tare=%.3f", result[0][2]-tmpTareValue[0])));
  					Platform.runLater(() ->ch2TareLB.setText(String.format("Tare=%.3f", result[1][2]-tmpTareValue[1])));
  				  }
			}
  	 	   };
  	 	   Platform.runLater(() ->infoLB.setText(""));
  	 	   tr = Executors.newSingleThreadScheduledExecutor();
  	 	   tr.scheduleAtFixedRate(tensionMesure, 0, 33, TimeUnit.MILLISECONDS);
    }
}
