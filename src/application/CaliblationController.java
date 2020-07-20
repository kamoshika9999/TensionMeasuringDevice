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
    private Label infoLB;;


    //クラス変数
    static long[] emptyValue = new long[2];
    static long[] calibValue = new long[2];
    static double[] calibWeight = new double[2];
    static HX711[] hx;//MainScreenControllerで作成されたオブジェクトの参照
    private String infoText ="";
    private long aveValue= 0;
	private boolean trFlg = false;
	private TextField targetTX;//スケジューラーで書き換えを行うTextfield
	private boolean targetTX_comp = false;//スケジューラーで書き換え後にfalseに設定 測定開始時にTrueに設定

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
    	if(trFlg) return;

    	targetTX = calibValueTX_1;
    	getLoadCellValue(0);
    }

    @FXML
    void onGetEmptyValueBT_1(ActionEvent event) {
    	if(trFlg) return;

    	targetTX = enptyValueTX_1;
    	getLoadCellValue(0);
    }
    @FXML
    void onGetCalibValu_2(ActionEvent event) {
    	if(trFlg) return;

    	targetTX = calibValueTX_2;
    	getLoadCellValue(1);
    }

    @FXML
    void onGetEmptyValueBT_2(ActionEvent event) {
    	if(trFlg) return;

    	targetTX = enptyValueTX_2;
    	getLoadCellValue(1);
    }
    /**
    *
    * @return  double[chNo] hx.value
    */
   public void getLoadCellValue(int chNo){
	   ScheduledExecutorService tr = Executors.newSingleThreadScheduledExecutor();

	   /*
	    * 別スレッドで取得しないとUIが更新できない
	    */
	   Runnable frameGrabber = new Runnable() {
		   @Override
		   public void run() {
			   trFlg=true;
			   targetTX_comp = true;
	   			final int rpeetCnt = (int)(30/0.1);
	   		   aveValue=0;
	   			for(int i=0;i<rpeetCnt;i++) {
				   if( !hx[chNo].read() ) {
					   if( !hx[chNo].read() ) {
						   infoText ="Failed";
						   trFlg=false;
						   return;
					   }

				   }
	  				System.out.println("value="+hx[chNo].value);
					infoText = String.format("%d", hx[chNo].value);
					//infoText = String.valueOf(i);
			        aveValue += hx[chNo].value;
	   			}
	   			aveValue /= rpeetCnt;
	   			trFlg=false;
		   }
	   };
	   tr.execute(frameGrabber);
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

 	   ScheduledExecutorService tr = Executors.newSingleThreadScheduledExecutor();
	   Runnable frameGrabber2 = new Runnable() {
		   @Override
		   public void run() {
			   Platform.runLater(() ->infoLB.setText(infoText));

			   /*
			    * public void getLoadCellValue(int chNo)で得られた
			    * データーの平均値をテキストフィールドへ入力する
			    */
			   if(targetTX != null) {
				   if(!trFlg && targetTX_comp) {
					   Platform.runLater(() ->targetTX.setText(String.format("%d", aveValue)));
					   infoText = "データー取得完了";
					   targetTX_comp = false;//このフラグは、この場所でのみfalseにされる
				   }
			   }
		   }
	   };
	   tr.scheduleAtFixedRate(frameGrabber2, 0, 33, TimeUnit.MILLISECONDS);

    }
}
