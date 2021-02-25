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
    private TextField enptyValueTX_1;//CH1の風袋(ロードセルの換算前の値)
    @FXML
    private TextField calibWeightTX_1;//CH1のキャリブレーションに使用した重り
    @FXML
    private TextField calibValueTX_1;//CH1 重りを付けた時のロードセルの換算前の値
    @FXML
    private TextField enptyValueTX_2;//CH2の風袋(ロードセルの換算前の値)
    @FXML
    private TextField calibWeightTX_2;//CH2のキャリブレーションに使用した重り
    @FXML
    private TextField calibValueTX_2;//CH2 重りを付けた時のロードセルの換算前の値
    @FXML
    private Button GetEmptyValueBT_1;//CH1 風袋測定開始ボタン
    @FXML
    private Button GetCalibValueBT_1;//CH1 キャリブレーション測定開始ボタン
    @FXML
    private Button GetEmptyValueBT_2;//CH2 風袋測定開始ボタン
    @FXML
    private Button GetCalibValueBT_2;//CH2 キャリブレーション測定開始ボタン
    @FXML
    private Button calibSaveBT;//キャリブレーションの結果を保存
    @FXML
    private Button calibCancelBT;//キャリブレーションの結果を破棄
    @FXML
    private Label infoLB;//各種情報ラベル
    @FXML
    private Label ch1_resultLB;//Ch1 1(g)あたりのロードセル換算値  static double[] resolution に入力される
    @FXML
    private Label ch2_resultLB;//Ch2 1(g)あたりのロードセル換算値  static double[] resolution に入力される

    //init.csvに保存される変数--------------------------------------
    static long[] emptyValue = new long[2];//風袋
    static long[] calibValue = new long[2];//重りあり時のロードセル生値
    static double[] calibWeight = new double[2];//重りの重量
    static double[] resolution = new double[2];//1(g)あたりのロードセル換算値
    //--------------------------------------------------------------
    private String infoText ="";//各種メッセージはスケジューラーで書き込まれる為、クラス変数にしておく
    private long aveValue= 0;//スケジューラーで書き込まれる為、クラス変数にしておく
	private boolean trFlg = false;//計測メソッドの２重呼び出し回避用
	private TextField targetTX;//スケジューラーで書き換えを行うTextfield
	private boolean targetTX_comp = false;//スケジューラーで書き換え後にfalseに設定 測定開始時にTrueに設定

	/**
	 * 保存せず戻る
	 * @param event
	 */
    @FXML
    void onCalibCancelBT(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    /**
     * キャリブレーションの結果を保存
     * @param event
     */
    @FXML
    void onCalibSaveBT(ActionEvent event) {
    	emptyValue[0] = Long.valueOf( this.enptyValueTX_1.getText() );
    	calibValue[0] = Long.valueOf( this.calibValueTX_1.getText());
    	calibWeight[0] = Double.valueOf( this.calibWeightTX_1.getText());
    	resolution[0] =(calibValue[0]-emptyValue[0])/calibWeight[0];

    	emptyValue[1] = Long.valueOf( this.enptyValueTX_2.getText() );
    	calibValue[1] = Long.valueOf( this.calibValueTX_2.getText());
    	calibWeight[1] = Double.valueOf( this.calibWeightTX_2.getText());
    	resolution[1] =(calibValue[1]-emptyValue[1])/calibWeight[1];



    	//キャリブレーションデーターの保存
    	csvSaveLoad.calibDataCsvWrite(emptyValue, calibValue, calibWeight,resolution);

    	//ロードセルオブジェクトのパラメータを更新
    	for(int i=0;i<2;i++) {
	    	MainScreenController.hx[i].emptyValue =  CaliblationController.emptyValue[i];
	    	MainScreenController.hx[i].calibrationValue = CaliblationController.calibValue[i];
	    	MainScreenController.hx[i].calibrationWeight =CaliblationController.calibWeight[i];
	    	MainScreenController.hx[i].resolution = CaliblationController.resolution[i];
    	}

		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    /**
     * CH1 キャリブレーション実行
     * @param event
     */
    @FXML
    void onGetCalibValu_1(ActionEvent event) {
    	if(trFlg) return;

    	targetTX = calibValueTX_1;
    	getLoadCellValue(0);
    }

    /**
     * CH1 風袋取得
     * @param event
     */
    @FXML
    void onGetEmptyValueBT_1(ActionEvent event) {
    	if(trFlg) return;

    	targetTX = enptyValueTX_1;
    	getLoadCellValue(0);
    }

    /**
     * CH2 キャリブレーション実行
     * @param event
     */
    @FXML
    void onGetCalibValu_2(ActionEvent event) {
    	if(trFlg) return;

    	targetTX = calibValueTX_2;
    	getLoadCellValue(1);
    }

    /**
     * CH2 風袋取得
     * @param event
     */
    @FXML
    void onGetEmptyValueBT_2(ActionEvent event) {
    	if(trFlg) return;

    	targetTX = enptyValueTX_2;
    	getLoadCellValue(1);
    }

    /**
    *ロードセルからデーターを取得(風袋/キャリブレーション共通で使用)
    * @return  double[chNo] hx.value
    */
    public void getLoadCellValue(int chNo){
	   ScheduledExecutorService tr = Executors.newSingleThreadScheduledExecutor();

	   /*
	    * 別スレッドで取得しないとUIが更新できない(計測中に値が更新されず固まったように見えてしまうことを回避
	    */
	   Runnable frameGrabber = new Runnable() {
		   @Override
		   public void run() {
			   trFlg=true;//実行開始フラグ
			   targetTX_comp = true;//データー取得が行われたことを示すフラグ
			   final int rpeetCnt = (int)(10/0.1);//HX711はfps=10である為100回測定の内、有効な値の平均を計算する
	   		   aveValue=0;

	   		   System.out.println("----初期偏差 確認開始-----");
	   		   Platform.runLater(() ->infoLB.setText("----初期偏差 確認開始-----"));
	   		   //最初の20回の平均を取得
	   		   final int tryCnt = 20;
	   		   long[] tmpValue = new long[tryCnt];
	   		   double tmpAve = 0;
	   		   boolean okFlg = false;//偏差のMAXが500以下ならばTrue;

	   		   while( !okFlg ) {
	   			   tmpAve = 0;
		   		   for(int i=0;i<tryCnt;i++) {
		   			   System.out.print(".");
					   if( !MainScreenController.hx[chNo].read() ) {
						   System.out.println("fail 1st=" + i);
						   if( !MainScreenController.hx[chNo].read() ) {
							   infoText ="Failed";
							   trFlg=false;
							   System.out.println("最初の20回測定内でFailed発生");
							   Platform.runLater(() ->infoLB.setText("初期偏差確認中に計測機器のエラーを検出しました"
							   		+ "断線が疑われます"));
							   return;
						   }
					   }
					   tmpValue[i] = MainScreenController.hx[chNo].value;
					   tmpAve += tmpValue[i];
		   		   }
		   		   tmpAve /= tryCnt;

		   		   System.out.println();
		   		   System.out.println("平均tmpAve="+tmpAve);

		   		   //最初の(tryCnt)回の偏差の平均を計算
		   		   final double MaxDeviation = 500;
		   		   double tmpMaxDeviation = 0;
		   		   boolean okDeviation = true;
		   		   double[] tmpDeviation = new double[tryCnt];
		   		   for(int i=0;i<tryCnt;i++) {
		   			   tmpDeviation[i] = Math.abs(tmpValue[i] - tmpAve);
		   			   //System.out.println("|tmpValue[" + i + "] - tmpAve|" + Math.abs(tmpValue[i] - tmpAve));
		   			   if( tmpDeviation[i] > MaxDeviation ) {

		   				   if( tmpMaxDeviation < tmpDeviation[i] ) {
		   					tmpMaxDeviation = tmpDeviation[i];
		   				   }
		   				   okDeviation = false;
		   			   }
		   		   }
		   		   if( okDeviation ) {
		   			   okFlg = true;
		   		   }else {
		   			   System.out.println("初期偏差大　再評価");
		   			   final String tmpStr = String.valueOf(tmpMaxDeviation);
		   			   Platform.runLater(() ->infoLB.setText(tmpStr + "__初期偏差大 再評価"));
		   		   }
	   		   }
	   		   System.out.println("----初期偏差 確認終了");

	   		   System.out.println();

	   		   System.out.println("----平均 確認開始");
	   		   int overCnt = 0;
	   			for(int i=0;i<rpeetCnt;i++) {
		   			   System.out.print(".");
				   if( !MainScreenController.hx[chNo].read() ) {
					   if( !MainScreenController.hx[chNo].read() ) {
						   infoText ="Failed";
						   trFlg=false;
						   return;
					   }
				   }
				   //得た値が最初の20回の平均との偏差が2000を超えている場合(異常値の破棄)
				   if( Math.abs( tmpAve - MainScreenController.hx[chNo].value) > 2000) {//> MainScreenController.hx[chNo].resolution*5) {
					  System.out.println( "tmpAve="+tmpAve + "差" + (tmpAve - MainScreenController.hx[chNo].value) );
					  System.out.println( "|tmpAve - hx[chNo].value| = " + Math.abs( tmpAve- MainScreenController.hx[chNo].value));
					  overCnt++;
					  System.out.println("overCnt=" + overCnt);
				   }else {
						infoText = String.format("%d",MainScreenController.hx[chNo].value);
				        aveValue += MainScreenController.hx[chNo].value;
				   }
	   			}
	   			if( rpeetCnt == overCnt ) {
	   				Platform.runLater(() ->infoLB.setText("計測値が揺らぎすぎています"));
	   				trFlg = false;
	   				return;
	   			}
	   			aveValue /= rpeetCnt-overCnt;
	   			System.out.println("----平均 確認終了:" + aveValue);
	   			//Platform.runLater(() ->targetTX.setText("----平均 確認終了:" + aveValue));
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
    	for(int i=0;i<2;i++) {
	    	if( emptyValue[i] >= calibValue[1] ) {
	    		calibValue[i] = emptyValue[i] +500000;
	    	}
    	}
    	Platform.runLater(() ->this.enptyValueTX_1.setText(String.valueOf(emptyValue[0])));
    	Platform.runLater(() ->this.calibValueTX_1.setText(String.valueOf(calibValue[0])));
    	Platform.runLater(() ->this.calibWeightTX_1.setText(String.valueOf(calibWeight[0])));
    	Platform.runLater(() ->this.enptyValueTX_2.setText(String.valueOf(emptyValue[1])));
    	Platform.runLater(() ->this.calibValueTX_2.setText(String.valueOf(calibValue[1])));
    	Platform.runLater(() ->this.calibWeightTX_2.setText(String.valueOf(calibWeight[1])));

 	   ScheduledExecutorService tr = Executors.newSingleThreadScheduledExecutor();
	   Runnable frameGrabber2 = new Runnable() {
		   @Override
		   public void run() {
			   Platform.runLater(() ->infoLB.setText(infoText));
			   try{
				   long[] tmp_emptyValue = new long[2];
				   long[] tmp_calibValue = new long[2];
				   double[] tmp_calibWeight = new double[2];
				   double[] tmp_resolution = new double[2];
			    	tmp_emptyValue[0] = Long.valueOf( enptyValueTX_1.getText() );
			    	tmp_calibValue[0] = Long.valueOf( calibValueTX_1.getText());
			    	tmp_calibWeight[0] = Double.valueOf( calibWeightTX_1.getText());
			    	tmp_resolution[0] =(tmp_calibValue[0]-tmp_emptyValue[0])/tmp_calibWeight[0];

			    	tmp_emptyValue[1] = Long.valueOf( enptyValueTX_2.getText() );
			    	tmp_calibValue[1] = Long.valueOf( calibValueTX_2.getText());
			    	tmp_calibWeight[1] = Double.valueOf( calibWeightTX_2.getText());
			    	tmp_resolution[1] =(tmp_calibValue[1]-tmp_emptyValue[1])/tmp_calibWeight[1];

					Platform.runLater(() ->ch1_resultLB.setText(String.format("%.5f",tmp_resolution[0] )));
					Platform.runLater(() ->ch2_resultLB.setText(String.format("%.5f",tmp_resolution[1] )));

			   }catch(Exception e){
				   infoText ="入力した文字列が不正です";
			   }

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

	   //画面表示完了まで十分待機する
	   try {
		Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

    }
}
