package application;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainScreenController {
	@FXML
	private ResourceBundle resources;
	@FXML
	private URL location;
    @FXML
    private Label hxvalueLB1;//CH1 ロードセル生値ラベル
    @FXML
    private Label hxvalueLB2;//CH1 変換後の測定値ラベル
    @FXML
    private Label hxvalueLB4;//CH2 ロードセル生値ラベル
    @FXML
    private Label hxvalueLB5;//CH2 変換後の測定値ラベル
    @FXML
    private Button calibrationMenuBT;//キャリブレーションメニューを開くボタン
    @FXML
    private BorderPane chartPane;//グラフ表示用コンテナ
    @FXML
    private Label judgmentLB;//判定値表示ラベル
    @FXML
    private Label infoLB;//メッセージ表示ラベル
    @FXML
    private Button resetBT;//リセットボタン
    @FXML
    private Button settingMenuBT;//設定メニューを開くボタン
    @FXML
    private Label mesureCntLB;//経過時間表示ラベル
    @FXML
    private Label ch1ErrCntLB;//CH1 計測異常回数(HX711通信異常とノイズによる測定値異常)
    @FXML
    private Label ch2ErrCntLB;//CH2 計測異常回数(HX711通信異常とノイズによる測定値異常)
    @FXML
    private Button shutdownBT;//シャットダウンボタン
    @FXML
    private Label ch1AveLB;//CH1 平均値
    @FXML
    private Label ch1MaxLB;//CH1 最大値
    @FXML
    private Label ch1MinLB;//CH1 最小値
    @FXML
    private Label ch2AveLB;//CH2 平均値
    @FXML
    private Label ch2MaxLB;//CH2 最大値
    @FXML
    private Label ch2MinLB;//CH2 最小値
    @FXML
    private Label CH1movingaverageLB;//[settingMenu.movingAverageTime]秒間毎の移動平均
    @FXML
    private Label CH2movingaverageLB;//[settingMenu.movingAverageTime]秒間毎の移動平均
    @FXML
    private Circle blinkShape;//可動状態インジケーター

    //デバッグフラグ
    public static boolean debugFlg = false;
    /*HX711 接続ピンリスト BCM番号で指定 「gpio readall」 で物理ピンと確認すること
    +-----+-----+---------+------+---+---Pi 3B--+---+------+---------+-----+-----+
    | BCM | wPi |   Name  | Mode | V | Physical | V | Mode | Name    | wPi | BCM |
    +-----+-----+---------+------+---+----++----+---+------+---------+-----+-----+
    |     |     |    3.3v |      |   |  1 || 2  |   |      | 5v      |     |     |
    |   2 |   8 |   SDA.1 | ALT0 | 1 |  3 || 4  |   |      | 5v      |     |     |
    |   3 |   9 |   SCL.1 | ALT0 | 1 |  5 || 6  |   |      | 0v      |     |     |
    |   4 |   7 | GPIO. 7 |   IN | 1 |  7 || 8  | 1 | ALT5 | TxD     | 15  | 14  |
    |     |     |      0v |      |   |  9 || 10 | 1 | ALT5 | RxD     | 16  | 15  |
    |  17 |   0 | GPIO. 0 |   IN | 0 | 11 || 12 | 0 | IN   | GPIO. 1 | 1   | 18  |
    |  27 |   2 | GPIO. 2 |   IN | 0 | 13 || 14 |   |      | 0v      |     |     |
    |  22 |   3 | GPIO. 3 |   IN | 0 | 15 || 16 | 0 | IN   | GPIO. 4 | 4   | 23  |
    |     |     |    3.3v |      |   | 17 || 18 | 0 | IN   | GPIO. 5 | 5   | 24  |
    |  10 |  12 |    MOSI | ALT0 | 0 | 19 || 20 |   |      | 0v      |     |     |
    |   9 |  13 |    MISO | ALT0 | 0 | 21 || 22 | 0 | IN   | GPIO. 6 | 6   | 25  |
    |  11 |  14 |    SCLK | ALT0 | 0 | 23 || 24 | 1 | OUT  | CE0     | 10  | 8   |
    |     |     |      0v |      |   | 25 || 26 | 1 | OUT  | CE1     | 11  | 7   |
    |   0 |  30 |   SDA.0 |   IN | 1 | 27 || 28 | 1 | IN   | SCL.0   | 31  | 1   |
    |   5 |  21 | GPIO.21 |   IN | 0 | 29 || 30 |   |      | 0v      |     |     |
    |   6 |  22 | GPIO.22 |  OUT | 0 | 31 || 32 | 0 | IN   | GPIO.26 | 26  | 12  |
    |  13 |  23 | GPIO.23 |   IN | 0 | 33 || 34 |   |      | 0v      |     |     |
    |  19 |  24 | GPIO.24 |  OUT | 0 | 35 || 36 | 0 | IN   | GPIO.27 | 27  | 16  |
    |  26 |  25 | GPIO.25 |   IN | 0 | 37 || 38 | 0 | IN   | GPIO.28 | 28  | 20  |
    |     |     |      0v |      |   | 39 || 40 | 0 | IN   | GPIO.29 | 29  | 21  |
    +-----+-----+---------+------+---+----++----+---+------+---------+-----+-----+
    | BCM | wPi |   Name  | Mode | V | Physical | V | Mode | Name    | wPi | BCM |
    +-----+-----+---------+------+---+---Pi 3B--+---+------+---------+-----+-----+
    */
	final Pin[] pinNoDAT = {RaspiPin.GPIO_21,RaspiPin.GPIO_23};
	final Pin[] pinNoCLK = {RaspiPin.GPIO_22,RaspiPin.GPIO_24};

	//HX711 オブジェクト
	GpioPinDigitalInput[] pinHXDAT = new GpioPinDigitalInput[2];
    GpioPinDigitalOutput[] pinHXCLK = new GpioPinDigitalOutput[2];
    static HX711[] hx = new HX711[2];

    //チャートオブジェクト
    JFreeChart chart_tention;
    //チャート用データーセット
    XYSeriesCollection tention_dataset;

    //測定データー
    List<Double> ch1MovingaverageTimeList = new ArrayList<Double>();//チャートのデータセットはデータ格納にラグが生じる為、読み出し時に不整合が出る
    List<Double> ch2MovingaverageTimeList = new ArrayList<Double>();//チャートのデータセットはデータ格納にラグが生じる為、読み出し時に不整合が出る
    List<Double> ch1TentionRawDataList = new ArrayList<Double>();//ラグを防止する為にデータ追加時にPlatform.runLater(() ->を
    List<Double> ch2TentionRawDataList = new ArrayList<Double>();//つけないと例外が発生する。非効率だが、UIと切り離されたオブジェクトで対応する
    double ch1_ave;
    double ch1_max;
    double ch1_min;
    double ch2_ave;
    double ch2_max;
    double ch2_min;
    int shotCnt;
    int[] ch_ShotCnt = new int[2];
    Timestamp startTime;

    static boolean mesureFlg =false; //mesure()メソッドを排他的に呼び出すためのフラグ
    boolean mesureTreshFlg =false;//計測結果が絶対値で10g超えている場合True;
    boolean mesureStopFlg = false;//測定が継続されている場合true
    long lockedTimer =System.currentTimeMillis();//測定を継続するかの判定用のタイマ
	long lockedTimerThresh = 3000;//3秒以上10ｇを越えなければ測定停止
	//機器計測エラー数
	public static int[] mesureErrCnt = new int[2];//計測エラーの回数。チャンネル毎にカウント
	final int mesureErrCntTreth = 5000;//8Hの間にmesureErrCntが閾値を超えれば機器異常発生
	boolean mesureErrFlg = false;//機器異常が発生している場合true
	//テンションエラーフラグ
	boolean tentionErrFlg = false;//テンションが設定値を超えればtrue
	//スレッドオブジェクト
	ScheduledExecutorService tr;//33msec毎に計測が実行される。そのオブジェクト
	Runnable tentionMesure;//スケジューラーで呼び出されるオブジェクト

	//メディアプレイヤー
	AudioClip mp_error;
	final String wavFileString = "117.wav";//117.wavとなっているが各号機でwavファイルの中身を変更し配置
	AudioClip mp_error2;
	final String wav2FileString = "error.wav";//テンションが設定範囲を超えた場合に鳴動
	AudioClip mp_error3;
	final String wav3FileString = "warning.wav";//テンションが設定範囲を超えた場合に鳴動


	//各機能実行中フラグ
	//マルチスレッド特有の問題を回避する為に、メソッドを排他的呼び出される様するフラグ
	boolean resetExFlg = false;
	boolean settingExFlg = false;
	boolean calibExFlg = false;

    /**
     *ロードセルからの値を取得
     * @return  double[ch][0] hx.value   double[ch][1] hx.weight
     * @throws InterruptedException
     */
    public static double[][] getLoadCellValue(){

    	mesureFlg = true;

    	double[][] result= {{-1,-1,-1},{-1,-1,-1}};
		double[] aveValue= {0,0};
		double[] aveWeight = {0,0};
		final int rpeetCnt = 3;//n回平均を取る
		double[][] tmpValue = new double[2][rpeetCnt];
		double[] maxValue= new double[2];
		double[] minValue= new double[2];

	   	//デバッグ用---------------
    	if( debugFlg ) {
	        Random rand = new Random();
	        int num = rand.nextInt(30)-15;
	    	result[0][1] = (61 + num  - settingMenu.tareValue[0])* (settingMenu.signInversionFlg[0]?-1:1);
	    	result[0][2] = 61 + num;
	    	num = rand.nextInt(30)-15;
	    	result[1][1] = (72 + num - settingMenu.tareValue[1])* (settingMenu.signInversionFlg[1]?-1:1);
	    	result[1][2] = 72 + num;
	    	double resolution = 3204;
	    	result[0][0] = result[0][2] * resolution;
	    	result[1][0] = result[1][2] * resolution;

	    	if( rand.nextDouble() > 0.99 && hx[0].calibrationWeight > 0) {
		    	result[0][0] = -1;
		    	result[0][1] = -1;
		    	result[0][2] = -1;
		    	mesureErrCnt[0]++;
	    	}
	    	if( rand.nextDouble() > 0.99 && hx[1].calibrationWeight > 0) {
		    	result[1][0] = -1;
		    	result[1][1] = -1;
		    	result[1][2] = -1;
		    	mesureErrCnt[1]++;
	    	}
	    	if( hx[0].calibrationWeight == 0 ) {
		    	result[0][0] = -1;
		    	result[0][1] = -1;
		    	result[0][2] = -1;
	    	}
	    	if( hx[1].calibrationWeight == 0 ) {
		    	result[1][0] = -1;
		    	result[1][1] = -1;
		    	result[1][2] = -1;
	    	}
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	mesureFlg = false;
	    	return result;
    	}
    	//-------------------------
		maxValue[0] = 0;maxValue[1]=0;
		minValue[0] = 99999999;minValue[1]=99999999;
    	int[] enableCnt = new int[2];
		try {
    		for(int j=0;j<rpeetCnt;j++) {
    			for(int i=0;i<2;i++) {
        			if( hx[i].calibrationWeight > 0 ) {
        				hx[i].read();
				        int cnt = 0;
				        while( hx[i].value == -1) {
				        	hx[i].read();
				        	cnt++;
				        	if( cnt > 10 ) {
				        		break;
				        	}
				        }
				        if( cnt <=10 ) {
					        tmpValue[i][j] = hx[i].value;
					        if( maxValue[i] < hx[i].value) maxValue[i] = hx[i].value;
					        if( minValue[i] > hx[i].value) minValue[i] = hx[i].value;
					        aveValue[i] += hx[i].value;//平均計算様に加算
					        aveWeight[i] += hx[i].weight;//平均計算様に加算
					        enableCnt[i]++;//有効な測定回数
				        }else {
				        	mesureErrCnt[i]++;//11回連続-1ならば機器異常回数をプラスする
				        }
        			}
		        }
    		}
    		//測定のレンジがhx[i].resolution * 15倍(15g)を超えていたら結果は-1になる
    		boolean[] flg = new boolean[2];
    		flg[0] = true;
    		flg[1] = true;
    		for(int i=0;i<2;i++) {
    			if( hx[i].calibrationWeight > 0 && enableCnt[i] > 0) {
	    			if( maxValue[i] - minValue[i] > hx[i].resolution * 15) {
	    				flg[i]=false;
	    				mesureErrCnt[i]++;//機器異常回数をプラスする
	    				System.out.println("******************************");
	    				System.out.println("MesureOver " + (hx[i].resolution * 10) + "="+(maxValue[i] - minValue[i]));
	    				for(int j=0;j<rpeetCnt;j++) {
	    					System.out.println("tmpValue[" + j + "]="+tmpValue[i][j]);
	    				}
	    				System.out.println("******************************");
	    			}
    			}
    		}
    		//測定レンジが規定値以上の場合resultは-1が入ったまま返される
    		for(int i=0;i<2;i++) {
	    		if( flg[i] && enableCnt[i] > 0) {
					result[i][0] = aveValue[i] / enableCnt[i];
					result[i][1] = ((aveWeight[i] / enableCnt[i]) - settingMenu.tareValue[i])
																	* (settingMenu.signInversionFlg[i]?-1:1);
					result[i][2] = aveWeight[i] / enableCnt[i];
	    		}
    		}
    	}catch(Exception e) {
    		System.out.println(e);
    	}
    	mesureFlg = false;
     	return result;
    }

    /**
     * キャリブレーションメニューを開く
     * @param event
     */
    @FXML
    void onCaliblationController(ActionEvent event) {
    	if( calibExFlg ) return;//連続でボタンを押されるのを回避
    	calibExFlg = true;

    	//パスワードチェック パスワードはソースコードに埋め込み
    	if( !passwordCheck() ) {
    		calibExFlg = false;
    		return;
    	}

    	//計測スレッド停止
    	try {
			tr.shutdown();
			tr.awaitTermination(33, TimeUnit.MICROSECONDS);
		} catch(Exception e) {
			System.out.println(e);
		}

    	FXMLLoader loader = new FXMLLoader(getClass().getResource("caliblation.fxml"));
    	AnchorPane root = null;
		try {
			root = (AnchorPane) loader.load();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setScene(scene);
		stage.setResizable(false);

		//設定ウィンドウを開く
		stage.showAndWait();

		//キャリブレーションデーター反映
        for(int i=0;i<2;i++) {
	    	hx[i].emptyValue =  CaliblationController.emptyValue[i];
	    	hx[i].calibrationValue = CaliblationController.calibValue[i];
	    	hx[i].calibrationWeight =CaliblationController.calibWeight[i];
	    	hx[i].resolution = CaliblationController.resolution[i];
    	}
		//計測スレッド再開
		tr = Executors.newSingleThreadScheduledExecutor();
		tr.scheduleAtFixedRate(tentionMesure, 0, 33, TimeUnit.MILLISECONDS);

    	//チャートレンジ、上限下限線描画
    	chartLineRangeSetting();

    	calibExFlg = false;
    }

    /**
     * パスワードチェック
     * @return
     */
    private boolean passwordCheck() {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("passwordDialog.fxml"));
		AnchorPane root = null;
		try {
			root = (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Scene scene = new Scene(root,640,480);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setResizable(false);
		stage.showAndWait();
		if( PasswordDialogController.flg == false ){//パスワードが不一致の場合
    		Platform.runLater(() ->infoLB.setText("パスワードが違います\n"));
    		return false;
		}
		return true;
    }

    /**
     * データーリセット
     * @param event
     */
    @FXML
    void onResetBT(ActionEvent event) {
    	if( resetExFlg ) return;
    	resetExFlg = true;

    	try {
    	//約60秒未満は保存しない
    	if(startTime !=null) {//startTimeオブジェクトがnullの時は実行しない
	    	if( System.currentTimeMillis() - startTime.getTime() > 60*1000 ) {
				if( !csvSaveLoad.saveDataSet(
						ch1TentionRawDataList,ch2TentionRawDataList,tention_dataset,
						startTime,ch1_max,ch1_min,ch1_ave,ch2_max,ch2_min,ch2_ave,ch_ShotCnt,shotCnt) ) {
					System.out.println("データーセット保存異常");
					Platform.runLater( () ->this.infoLB.setText("データーセット保存異常"));
				}
			}
    	}

        ch1_max = 0;
        ch2_max = 0;
        ch1_min = 9999;
        ch2_min = 9999;
        ch1_ave = 0;
        ch2_ave = 0;
        shotCnt=0;
        ch_ShotCnt[0] = 0;
        ch_ShotCnt[1] = 0;

		//チャート用データーセットリセット
		Platform.runLater( () ->tention_dataset.getSeries(0).clear());
		Platform.runLater( () ->tention_dataset.getSeries(1).clear());
		Platform.runLater( () ->mesureCntLB.setText("----------"));
		ch1TentionRawDataList.clear();
		ch2TentionRawDataList.clear();
		ch1MovingaverageTimeList.clear();
		ch2MovingaverageTimeList.clear();

		//ラベル表示リセット
		Platform.runLater(() ->ch1AveLB.setText("---"));
		Platform.runLater(() ->ch2AveLB.setText("---"));
		Platform.runLater(() ->ch1MaxLB.setText("---"));
		Platform.runLater(() ->ch1MinLB.setText("---"));
		Platform.runLater(() ->ch2MaxLB.setText("---"));
		Platform.runLater(() ->ch2MinLB.setText("---"));
		Platform.runLater(() ->CH1movingaverageLB.setText("---"));
		Platform.runLater(() ->CH2movingaverageLB.setText("---"));
		Platform.runLater(() ->hxvalueLB1.setText("---"));
		Platform.runLater(() ->hxvalueLB2.setText("---"));
		Platform.runLater(() ->hxvalueLB4.setText("---"));
		Platform.runLater(() ->hxvalueLB5.setText("---"));
		Platform.runLater(() ->ch1ErrCntLB.setText("---"));
		Platform.runLater(() ->ch2ErrCntLB.setText("---"));


		//機器異常リセット ※eventがnullの時は自動停止時に呼び出されたと判断しアラーム鳴動を止めない
		//機器異常は常時監視されている8H以内に規定の回数計測エラーが発生した場合、巻きが停止して
		//いても機器異常のアラームは鳴動する
		if( event != null ) {
			mesureErrFlg = false;
	        for(int i=0;i<2;i++) {
	        	mesureErrCnt[i] = 0;
	        }
	        tentionErrFlg = false;
	        //メディアプレイヤー再生強制停止
			if( mp_error != null ) mp_error.stop();
			if( mp_error2 != null ) mp_error2.stop();
		}

		startTime = new Timestamp(System.currentTimeMillis());
    	}catch(Exception e) {
    		System.out.println(e);
    	}

    	//チャートレンジ、上限下限線描画
    	chartLineRangeSetting();

    	resetExFlg = false;
    }

    /**
     * 計測用メインメソッド
     */
    private void mesure() {
    		final int disableTime = 60;//判定、最大値、最小値の更新無効タイマ

    		if(mesureFlg) return;//別スレッドから同時複数呼び出しを無効にする

	    	double[][] result = getLoadCellValue();
	    	if( result[0][0] != -1 && hx[0].calibrationWeight > 0) {
		    	Platform.runLater(() ->hxvalueLB1.setText(String.format("%.0f",result[0][0])));
		    	Platform.runLater(() ->hxvalueLB2.setText(String.format("%.0f",result[0][1])));
	    	}else if(result[0][0] == -1 && hx[0].calibrationWeight > 0){
				Platform.runLater(() ->hxvalueLB1.setText("Error"));
				Platform.runLater(() ->hxvalueLB2.setText("Error"));
	    	}
		    if( result[1][0] != -1 && hx[1].calibrationWeight > 0) {
		    	Platform.runLater(() ->hxvalueLB4.setText(String.format("%.0f",result[1][0])));
		    	Platform.runLater(() ->hxvalueLB5.setText(String.format("%.0f",result[1][1])));
		    }else if(result[1][0] == -1 && hx[1].calibrationWeight > 0) {
		    	Platform.runLater(() ->hxvalueLB4.setText("Error"));
		    	Platform.runLater(() ->hxvalueLB5.setText("Error"));
		    }

	    	if( Math.abs(result[0][1]) > 30 || Math.abs(result[1][1]) > 30 ) {
	    		mesureTreshFlg = true;
	    	}else {
	    		mesureTreshFlg = false;
	    	}

    		//経過時間計算
    		long chartTime = System.currentTimeMillis() - startTime.getTime();//経過時間(mSec単位)

	    	if( !mesureStopFlg ) {
		    	if( mesureTreshFlg ) {//どちらかのチャンネルが10gを超えている場合
		    		shotCnt++;
		    		//n秒間の生データの移動平均計算
		    		//判定も移動平均で行う
		    		final double movingaverageTime = settingMenu.movingAverageTime;
		    		int chartDataIndex;
		    		boolean movingaverageFlg;
		    		double[] movingaverage = new double[2];
		    		double tmpCnt = 0;
		    		double tmpTime;
		    		try {
		    		//CH1を計算----------------------------------------------------------------------------------------
		    		if( ch_ShotCnt[0] > 1 && chartTime/1000.0 > movingaverageTime) {
			    		chartDataIndex = ch_ShotCnt[0] - 1;
			    		tmpCnt=0;
			    		movingaverageFlg=false;
			    		while( !movingaverageFlg ) {
				    		tmpTime = ch1MovingaverageTimeList.get( chartDataIndex );
				    		if( result[0][0] > 0 && ch1TentionRawDataList.get( chartDataIndex ) > 0) {
				    			movingaverage[0] += ch1TentionRawDataList.get( chartDataIndex );
				    			tmpCnt++;
				    		}
				    		if( tmpTime > chartTime/1000.0 - movingaverageTime && chartDataIndex > 0) {
				    			chartDataIndex--;
				    		}else {
				    			movingaverageFlg = true;
					    		if( tmpCnt > 0) {
				    				movingaverage[0] /= tmpCnt;
					    		}else {
					    			//移動平均が計算出来なかった場合(movingaverageTime時間内で計測値が無い場合)
					    			movingaverage[0] = result[0][1];
					    		}
				    		}
			    		}
		    		}else {
		    			movingaverage[0] = result[0][1];
		    		}
		    		if( result[0][0] != -1) {
	    				ch1_ave +=  movingaverage[0];
				    	ch1MovingaverageTimeList.add(chartTime/1000.0);
			    		ch1TentionRawDataList.add(result[0][1]);//生データを格納

			    		//データーセットは表示の都合でDouble値で格納する
			    		Platform.runLater( () ->tention_dataset.getSeries(0).add((double)chartTime/1000.0,movingaverage[0]));

			    		//テンションの最大値、最小値、平均を更新
			    		if( chartTime/1000 > disableTime) {
				    		if( ch1_max < movingaverage[0] ) ch1_max = movingaverage[0];
				    		if( ch1_min > movingaverage[0] ) ch1_min = movingaverage[0];
			    		}
			    		ch_ShotCnt[0]++;
		    		}
		    		//-------------------------------------------------------------------------------------------------
		    		//CH2を計算
		    		if( ch_ShotCnt[1] > 1 && chartTime/1000.0 > movingaverageTime) {
			    		chartDataIndex = ch_ShotCnt[1] - 1;
			    		tmpCnt=0;
			    		movingaverageFlg=false;
			    		while( !movingaverageFlg ) {
				    		tmpTime = ch2MovingaverageTimeList.get( chartDataIndex );
				    		if( result[1][0] > 0 && ch2TentionRawDataList.get( chartDataIndex ) > 0) {
				    			movingaverage[1] += ch2TentionRawDataList.get( chartDataIndex );
				    			tmpCnt++;
				    		}
				    		if( tmpTime > chartTime/1000.0 - movingaverageTime && chartDataIndex > 0) {
				    			chartDataIndex--;
				    		}else {
				    			movingaverageFlg = true;
					    		if( tmpCnt > 0) {
				    				movingaverage[1] /= tmpCnt;
					    		}else {
					    			//移動平均が計算出来なかった場合(movingaverageTime時間内で計測値が無い場合)
					    			movingaverage[1] = result[1][1];
					    		}
				    		}
			    		}
		    		}else {
		    			movingaverage[1] = result[1][1];
		    		}
		    		if( result[1][0] != -1) {
	    				ch2_ave +=  movingaverage[1];
				    	ch2MovingaverageTimeList.add(chartTime/1000.0);
			    		ch2TentionRawDataList.add(result[1][1]);//生データを格納

			    		//データーセットは表示の都合でDouble値で格納する
			    		Platform.runLater( () ->tention_dataset.getSeries(1).add((double)chartTime/1000.0, movingaverage[1]));

			    		//テンションの最大値、最小値、平均を更新
			    		if( chartTime/1000 > disableTime) {
				    		if( ch2_max < movingaverage[1] ) ch2_max = movingaverage[1];
				    		if( ch2_min > movingaverage[1] ) ch2_min = movingaverage[1];
			    		}
			    		ch_ShotCnt[1]++;
		    		}
		    		//-------------------------------------------------------------------------------------------------

		    		//テンションの最大値、最小値、平均を更新
		    		if( chartTime/1000 > disableTime) {
			    		if( hx[0].calibrationWeight > 0 ) {
				    		Platform.runLater(() ->ch1MaxLB.setText(String.format("%.0f",ch1_max)));
				    		Platform.runLater(() ->ch1MinLB.setText(String.format("%.0f",ch1_min)));
			    		}
			    		if( hx[1].calibrationWeight > 0 ) {
				    		Platform.runLater(() ->ch2MaxLB.setText(String.format("%.0f",ch2_max)));
				    		Platform.runLater(() ->ch2MinLB.setText(String.format("%.0f",ch2_min)));
			    		}
			    	}else {
			    		Platform.runLater(() ->ch1MaxLB.setText("---"));
			    		Platform.runLater(() ->ch1MinLB.setText("---"));
			    		Platform.runLater(() ->ch2MaxLB.setText("---"));
			    		Platform.runLater(() ->ch2MinLB.setText("---"));
		    		}

		    		if( hx[0].calibrationWeight > 0 ) {
		    			Platform.runLater(() ->ch1AveLB.setText(String.format("%.0f",ch1_ave/ch_ShotCnt[0])));
		    		}
		    		if( hx[1].calibrationWeight > 0 ) {
		    			Platform.runLater(() ->ch2AveLB.setText(String.format("%.0f",ch2_ave/ch_ShotCnt[1])));
		    		}


		    		}catch(Exception e) {
		    			System.out.println(e);
		    		}
		    		//移動平均表示
		    		if( hx[0].calibrationWeight > 0 && result[0][0] > 0) {
		    			Platform.runLater(() ->CH1movingaverageLB.setText(String.format("%.0f", movingaverage[0])));
		    		}
		    		if( hx[1].calibrationWeight > 0 && result[1][0] > 0) {
		    			Platform.runLater(() ->CH2movingaverageLB.setText(String.format("%.0f", movingaverage[1])));
		    		}
		    		int judgmentFlg = 0;//0:合格 1～2:警告 3～:規格外
		    		//規格判定
		    		for(int i=0;i<2;i++) {
		    			if( hx[i].calibrationWeight > 0 && result[i][0] > 0) {
				    		if( movingaverage[i] >
				    			settingMenu.maxErrorValue[i] - (settingMenu.maxErrorValue[i]*settingMenu.ratioValue[i]) ) {
				    			Platform.runLater(() ->infoLB.setText("MaxWarning"));
				    			judgmentFlg += 1;
				    		}
				    		if( movingaverage[i] > settingMenu.maxErrorValue[i]) {
				    			Platform.runLater(() ->infoLB.setText("MaxOver!!"));
				    			judgmentFlg += 3;
				    		}
				    		if( movingaverage[i] <
			    			settingMenu.minErrorValue[i] + (settingMenu.minErrorValue[i]*settingMenu.ratioValue[i]) ) {
				    			Platform.runLater(() ->infoLB.setText("MinWarning"));
				    			judgmentFlg += 1;
				    		}
				    		if( movingaverage[i] < settingMenu.minErrorValue[i]) {
				    			Platform.runLater(() ->infoLB.setText("MinLower!!"));
				    			judgmentFlg += 3;
				    		}
		    			}
		    		}

		    		//判定表示
		    		if( judgmentFlg == 0 ) {
			    		Platform.runLater(() ->infoLB.setText("Measuring"));
		    			Platform.runLater(() ->judgmentLB.setTextFill(Paint.valueOf("#5eff00ff")));
		    			Platform.runLater(() ->judgmentLB.setText("OK"));
		    		}else if( judgmentFlg ==1 || judgmentFlg ==2) {
		    			Platform.runLater(() ->judgmentLB.setTextFill(Paint.valueOf("#ffff00ff")));
		    			Platform.runLater(() ->judgmentLB.setText("!!!"));
		    	    	//約disableTime秒未満は鳴らさない
		    			if( !mp_error3.isPlaying() && !mesureErrFlg && mesureTreshFlg &&
		    					( System.currentTimeMillis() - startTime.getTime() > disableTime*1000 )) {
			    			mp_error3.play();//テンション警告を鳴らす
		    			}

		    		}else if( judgmentFlg > 2){
		    			Platform.runLater(() ->judgmentLB.setTextFill(Paint.valueOf("#ff0000ff")));
		    			Platform.runLater(() ->judgmentLB.setText("NG"));
		    			//計測開始から一定時間経過後かつ10gを超えている場合のみエラーフラグをたてる
		    			if( System.currentTimeMillis()-startTime.getTime()>disableTime*1000  && mesureTreshFlg) {
		    				tentionErrFlg = true;
		    			}
		    		}


		    		//経過時間表示
		    		Platform.runLater( () ->mesureCntLB.setText( String.format("%d H %d M %d S",
		    				(int)Math.floor( (double)chartTime/1000.0/3600.0 ),
		    				(int)Math.floor( ((double)chartTime/1000.0/60.0) % 60),
		    				(int)(chartTime/1000) % 60
		    				)));

		    		//データーセットはdouble値 チャート表示は整数とする為、変換表示させる
		    		try {
			    		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_tention.getPlot()).getDomainAxis()).
								setRange( (chartTime/1000)<=settingMenu.graphXaxisTime ? 0.0 :
										(chartTime/1000)-settingMenu.graphXaxisTime,
										chartTime/1000<1.0?1.0:(chartTime/1000)));
		    		}catch(Exception e) {
		    			//特に何もしない
		    			//レンジ設定に伴う例外をスルーする為
		    		}


		    	}else {
		    		Platform.runLater(() ->infoLB.setText("Mesure Error"));
		    	}
	    	}else{
	    		if( hx[0].calibrationWeight > 0 ) {
			    	//Platform.runLater(() ->hxvalueLB1.setText(String.format("%.0f",0.0)));
			    	//Platform.runLater(() ->hxvalueLB2.setText(String.format("%.0f",0.0)));
	    		}
	    		if( hx[1].calibrationWeight > 0 ) {
			    	//Platform.runLater(() ->hxvalueLB4.setText(String.format("%.0f",0.0)));
			    	//Platform.runLater(() ->hxvalueLB5.setText(String.format("%.0f",0.0)));
	    		}
		    	Platform.runLater(() ->infoLB.setText("Mesure Stop"));
	    	}
	    	//テンション異常
	    	if( tentionErrFlg ) {
    			if( !mp_error.isPlaying() && !mesureErrFlg) {
	    			mp_error.play();
    			}
	    	}
	    	//機器異常判定
	    	if( mesureErrCnt[0] > mesureErrCntTreth || mesureErrCnt[1] > mesureErrCntTreth) {
    			mesureErrFlg = true;
    		}
    		if( mesureErrFlg ) {
    			if( !mp_error2.isPlaying() ) {
	    			mp_error2.play();
    			}
    			Platform.runLater(() ->infoLB.setText("Equipment abnormality"));
    			Platform.runLater(() ->judgmentLB.setTextFill(Paint.valueOf("#ff0000ff")));
    			Platform.runLater(() ->judgmentLB.setText("×"));
    		}
    		if( hx[0].calibrationWeight > 0 ) {
    			Platform.runLater(() ->ch1ErrCntLB.setText(String.valueOf( mesureErrCnt[0] )));
    		}
    		if( hx[1].calibrationWeight > 0 ) {
    			Platform.runLater(() ->ch2ErrCntLB.setText(String.valueOf( mesureErrCnt[1] )));
    		}
    }

    /**
     * 設定メニュー
     * @param event
     */
    @FXML
    void onSettingMenuBT(ActionEvent event) {
    	if( settingExFlg ) return;

    	settingExFlg = true;

    	if( !passwordCheck() ) {
    		settingExFlg = false;
    		return;
    	}

    	try {
			tr.shutdown();
			tr.awaitTermination(33, TimeUnit.MICROSECONDS);
		} catch(Exception e) {
			System.out.println(e);
		}

    	FXMLLoader loader = new FXMLLoader(getClass().getResource("settingMenu.fxml"));
		AnchorPane root = null;
		try {
			root = (AnchorPane) loader.load();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setScene(scene);
		stage.setResizable(false);

		//設定ウィンドウを開く
		stage.showAndWait();

		debugFlg = settingMenu.demoMode;

    	//チャートレンジ、上限下限線描画
    	chartLineRangeSetting();

		tr = Executors.newSingleThreadScheduledExecutor();
		tr.scheduleAtFixedRate(tentionMesure, 0, 33, TimeUnit.MILLISECONDS);

		settingExFlg = false;
    }

    /**
     * 終了
     * @param event
     */
    @FXML
    void onShutdownBT(ActionEvent event) {
    	System.exit(0);
    }

    /**
     * 初期化
     * @throws InterruptedException
     */
    @FXML
    void initialize() throws InterruptedException {
    	//GPIO初期化
    	GpioUtil.enableNonPrivilegedAccess();
        GpioController gpio = GpioFactory.getInstance();
        for(int i=0;i<2;i++) {
	        pinHXDAT[i] = gpio.provisionDigitalInputPin(pinNoDAT[i],
	        		"HX_DAT"+String.valueOf(i), PinPullResistance.OFF);
	        pinHXCLK[i] = gpio.provisionDigitalOutputPin(pinNoCLK[i],
	        		"HX_CLK"+String.valueOf(i), PinState.LOW);
	        hx[i] = new HX711(pinHXDAT[i], pinHXCLK[i], 128);
        }

        //設定データーロード
        csvSaveLoad.settingValueLoad();

        //キャリブレーションデーターロード
        csvSaveLoad.calibDataLoad(
        		CaliblationController.emptyValue,
        		CaliblationController.calibValue,
        		CaliblationController.calibWeight,
        		CaliblationController.resolution);

        //ロードされたキャリブレーションデーターをhxオブジェクトへセット
        for(int i=0;i<2;i++) {
	    	hx[i].emptyValue =  CaliblationController.emptyValue[i];
	    	hx[i].calibrationValue = CaliblationController.calibValue[i];
	    	hx[i].calibrationWeight =CaliblationController.calibWeight[i];
	    	hx[i].resolution = CaliblationController.resolution[i];
    	}

        //測定データリセット実行
        onResetBT(null);

        //メディアプレイヤー準備
        //wavファイルはjarと同一フォルダに置くこと
		File jarFile = null;
		try {
			jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String filePath = jarFile.getParent() + File.separator + wavFileString;
		mp_error = new AudioClip(new File(filePath).toURI().toString());//テンション規格外
		filePath = jarFile.getParent() + File.separator + wav2FileString;
		mp_error2 = new AudioClip(new File(filePath).toURI().toString());//機器異常
		filePath = jarFile.getParent() + File.separator + wav3FileString;
		mp_error3 = new AudioClip(new File(filePath).toURI().toString());//機器異常

        //チャートオブジェクト作成
		chart_tention = chartFact();

		//自動計測用スレッド
 	   	tentionMesure = new Runnable() {
		@Override
 			public void run() {
		    	if( blinkShape.getFill() != Color.YELLOW) {
		    		Platform.runLater( () ->blinkShape.setFill(Color.YELLOW));
		    	}else {
		    		Platform.runLater( () ->blinkShape.setFill(Color.GREEN));
		    	}
				mesure();
				if( mesureTreshFlg ) {//30gを超えていれば設備は動作中と判断
					lockedTimer = System.currentTimeMillis();
  			  	}
				if( System.currentTimeMillis() - lockedTimer > lockedTimerThresh) {
					if( !mesureStopFlg) {
						mesureStopFlg  = true;
						onResetBT(null);
					}
				}else {
					if( mesureStopFlg ) {
						startTime = new Timestamp(System.currentTimeMillis());
						mesureStopFlg  = false;
					}
				}
				if( System.currentTimeMillis() - lockedTimer > 8 * 60 *60 *1000 ) {
					shotCnt = 0;
					mesureErrCnt[0] = 0;
					mesureErrCnt[1] = 0;
				}
 		   	}
 	   	};

 	   	//書式設定
 	   	Platform.runLater(() ->judgmentLB.setAlignment(Pos.CENTER));
 	   	Platform.runLater(() ->hxvalueLB1.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->hxvalueLB2.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->hxvalueLB4.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->hxvalueLB5.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->ch1AveLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->ch2AveLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->ch1MaxLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->ch2MaxLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->ch1MinLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->ch2MinLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->ch1ErrCntLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->ch2ErrCntLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->CH1movingaverageLB.setAlignment(Pos.CENTER_RIGHT));
 	   	Platform.runLater(() ->CH2movingaverageLB.setAlignment(Pos.CENTER_RIGHT));

 	   	startTime = new Timestamp(System.currentTimeMillis());//メソッド内でnullを避けるため
 	   	mesureStopFlg = true;
 	   	tr = Executors.newSingleThreadScheduledExecutor();
 	   	tr.scheduleAtFixedRate(tentionMesure, 0, 33, TimeUnit.MILLISECONDS);

    }

    /**
     * チャートのレンジと上限下限線を描画
     */
    private void chartLineRangeSetting() {
    	if(chart_tention == null) return;

        // 上限線、下限線を引く
		XYPlot xyPlot = chart_tention.getXYPlot();
		xyPlot.clearRangeMarkers();
		float dash [] = {4f, 5f};
		Stroke dashed = new BasicStroke(2f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                dash,
                0.0f);
		Marker marker = new ValueMarker(settingMenu.maxErrorValue[0]);
		marker.setStroke(dashed);
		marker.setPaint(org.jfree.chart.ChartColor.red);
		xyPlot.addRangeMarker(marker);

		marker = new ValueMarker(settingMenu.minErrorValue[0]);
		marker.setStroke(dashed);
		marker.setPaint(org.jfree.chart.ChartColor.red);
		xyPlot.addRangeMarker(marker);

		//グラフレンジの設定
		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_tention.getPlot()).getRangeAxis()).
				setRange(settingMenu.minErrorValue[0]-15.0,settingMenu.maxErrorValue[0]+15.0));

    }
    /**
     * チャートファクトリ
     * @return
     */
    private JFreeChart chartFact() {
    	XYSeriesCollection tentionDataSet;
		tentionDataSet = getChartData();
		JFreeChart chart = createInitChart("TentionRealTimeMonitor","(g)","ElapsedTime(sec)",tentionDataSet ,30,100);
		ChartViewer chV = new ChartViewer(chart);
        chV.addChartMouseListener( new ChartMouseListenerFX() {
				@Override
				public void chartMouseClicked(ChartMouseEventFX e) {
					//XYPlot xyplot = e.getChart().getXYPlot();
					//double value = xyplot.getRangeCrosshairValue();
				}

				@Override
				public void chartMouseMoved(ChartMouseEventFX e) {
				}
        	}
        );
        this.chartPane.setCenter(chV);

        return chart;

    }
    /**
     * グラフの雛形作成
     * @param title
     * @param valueAxisLabel
     * @param categoryAxisLabel
     * @return
     */
    private JFreeChart createInitChart(String title,String valueAxisLabel,
    							String categoryAxisLabel,XYSeriesCollection dataset,double lower,double upper){
    	JFreeChart chart = ChartFactory.createXYLineChart(title,categoryAxisLabel,valueAxisLabel,
                dataset,//データーセット
                PlotOrientation.VERTICAL,//値の軸方向
                true,//凡例
                false,//tooltips
                false);//urls
        // 背景色を設定
    	chart.setBackgroundPaint(ChartColor.WHITE);

        // 凡例の設定
        LegendTitle lt = chart.getLegend();
        lt.setFrame(new BlockBorder(1d, 2d, 3d, 4d, ChartColor.WHITE));
        lt.setPosition(RectangleEdge.LEFT);

        XYPlot plot = (XYPlot) chart.getPlot();
        // 背景色
        plot.setBackgroundPaint(ChartColor.gray);
        // 背景色 透明度
        plot.setBackgroundAlpha(0.5f);
        // 前景色 透明度
        plot.setForegroundAlpha(0.5f);
        // 縦線の色
        plot.setDomainGridlinePaint(ChartColor.white);
        // 横線の色
        plot.setRangeGridlinePaint(ChartColor.white);
        // カーソル位置で横方向の補助線をいれる
        plot.setDomainCrosshairVisible(true);
        // カーソル位置で縦方向の補助線をいれる
        plot.setRangeCrosshairVisible(true);
        // 横軸の設定
        NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
        xAxis.setAutoRange(false);
        xAxis.setRange(1,200);
        // 縦軸の設定
        NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
        yAxis.setAutoRange(false);
        yAxis.setRange(lower,upper);

        // プロットをつける
        XYLineAndShapeRenderer  renderer = new XYLineAndShapeRenderer(true,false);
        plot.setRenderer(renderer);
        //renderer.setDefaultShapesVisible(true);
        //renderer.setDefaultShapesFilled(true);
        //プロットのサイズ
        Stroke stroke = new BasicStroke(2.0f);
        renderer.setSeriesStroke(0, stroke);
        renderer.setSeriesStroke(1, stroke);
		//色
        renderer.setSeriesPaint(0, ChartColor.BLUE);
        renderer.setSeriesPaint(1, ChartColor.DARK_MAGENTA);

        /*
        // プロットに値を付ける
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        XYItemLabelGenerator generator =
            new StandardXYItemLabelGenerator(
                StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT,
                format, format);
        renderer.setDefaultItemLabelGenerator(generator);
        renderer.setDefaultItemLabelsVisible(true);
         */

        return chart;
    }
    private XYSeriesCollection getChartData(){
        tention_dataset = new XYSeriesCollection();
        XYSeries ch1_series = new XYSeries("CH1");
        tention_dataset.addSeries(ch1_series);

        XYSeries ch2_series = new XYSeries("CH2");
        tention_dataset.addSeries(ch2_series);

        return tention_dataset;
    }

}

