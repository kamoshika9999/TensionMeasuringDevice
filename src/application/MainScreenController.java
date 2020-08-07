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
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
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
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    @FXML
    private Label infoLB;
    @FXML
    private Button resetBT;
    @FXML
    private Button settingMenuBT;
    @FXML
    private Label mesureCntLB;
    @FXML
    private Label ch1AveLB;
    @FXML
    private Label ch2AveLB;
    @FXML
    private Label ch1ErrCntLB;
    @FXML
    private Label ch2ErrCntLB;
    @FXML
    private Button shutdownBT;
    @FXML
    private Label ch1MaxLB;
    @FXML
    private Label ch1MinLB;
    @FXML
    private Label ch2MaxLB;
    @FXML
    private Label ch2MinLB;

    //デバッグフラグ
    public static boolean debugFlg = false;
    //HX711 接続ピンリスト BCM番号で指定 「gpio readall」 で物理ピンと確認すること
	final Pin[] pinNoDAT = {RaspiPin.GPIO_21,RaspiPin.GPIO_23};
	final Pin[] pinNoCLK = {RaspiPin.GPIO_22,RaspiPin.GPIO_24};
	//HX711 オブジェクト
	GpioPinDigitalInput[] pinHXDAT = new GpioPinDigitalInput[2];
    GpioPinDigitalOutput[] pinHXCLK = new GpioPinDigitalOutput[2];
    static HX711[] hx = new HX711[2];

    //チャート用データーセット
    JFreeChart chart_tention;
    XYSeriesCollection tention_dataset;

    //測定データー
    List<Double> ch1_tention = new ArrayList<Double>();
    List<Double> ch2_tention = new ArrayList<Double>();
    double ch1_max = 0;
    double ch2_max = 0;
    double ch1_min = 9999;
    double ch2_min = 9999;
    double ch1_ave = 0;
    double ch2_ave = 0;
    long shotCnt = 0;
    Timestamp startTime;

    static boolean mesureFlg =false; //計測中フラグ
    boolean mesureTreshFlg =false;//計測結果が絶対値で10g超えている場合True;
    boolean mesureStopFlg = false;//trueで計測停止
    long lockedTimer =System.currentTimeMillis();
	long lockedTimerThresh = 3000;//3秒以上10ｇを越えなければ測定停止
	//機器計測エラー数
	public static int[] mesureErrCnt = new int[2];
	final int mesureErrCntTreth = 2000;
	boolean mesureErrFlg = false;
	//テンションエラーフラグ
	boolean tentionErrFlg = false;
	//スレッドオブジェクト
	ScheduledExecutorService tr = Executors.newSingleThreadScheduledExecutor();
	Runnable tentionMesure;

	//メディアプレイヤー
	AudioClip mp_error;
	final String wavFileString = "117.wav";
	AudioClip mp_error2;
	final String wav2FileString = "error.wav";
	AudioClip mp_error3;
	final String wav3FileString = "warning.wav";


	//各機能実行中フラグ
	boolean resetExFlg = false;
	boolean settingExFlg = false;
	boolean calibExFlg = false;

    /**
     *
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
	    	result[0][1] = (98 + num  - settingMenu.ch1TareValue)* (settingMenu.CH1SignInversionFlg?-1:1);
	    	result[0][2] = 98 + num;
	    	num = rand.nextInt(30)-15;
	    	result[1][1] = (120 + num - settingMenu.ch2TareValue)* (settingMenu.CH2SignInversionFlg?-1:1);
	    	result[1][2] = 120 + num;
	    	double resolution = 3204;
	    	result[0][0] = result[0][2] * resolution;
	    	result[1][0] = result[1][2] * resolution;

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
					        aveValue[i] += hx[i].value;
					        aveWeight[i] += hx[i].weight;
					        enableCnt[i]++;
				        }else {
				        	//System.out.println("faileMesureCnt(RpeetOver10) = " + cnt );
				        	mesureErrCnt[i]++;
				        }
        			}
		        }

    		}
    		//測定のレンジがhx[i].resolution * 10倍(10g)を超えていたら結果は-1になる
    		boolean flg=true;
    		for(int i=0;i<2;i++) {
    			if( hx[i].calibrationWeight > 0 ) {
	    			if( maxValue[i] - minValue[i] > hx[i].resolution * 10) {
	    				flg=false;
	    				mesureErrCnt[i]++;
	    				System.out.println("******************************");
	    				System.out.println("MesureOver " + (hx[i].resolution * 10) + "="+(maxValue[i] - minValue[i]));
	    				for(int j=0;j<rpeetCnt;j++) {
	    					System.out.println("tmpValue[" + j + "]="+tmpValue[i][j]);
	    				}
	    				System.out.println("******************************");
	    			}
    			}
    		}
    		if( flg ) {
	    		for(int i=0;i<2;i++) {
			        aveValue[i] /= enableCnt[i];
			        aveWeight[i] /= enableCnt[i];
	    		}
    			result[0][0] = aveValue[0];
    			result[1][0] = aveValue[1];
    			result[0][1] = (aveWeight[0] - settingMenu.ch1TareValue) * (settingMenu.CH1SignInversionFlg?-1:1);
    			result[1][1] = (aveWeight[1] - settingMenu.ch2TareValue) * (settingMenu.CH2SignInversionFlg?-1:1);
    			result[0][2] = aveWeight[0];
    			result[1][2] = aveWeight[1];
    		}
    	}catch(Exception e) {
    		System.out.println(e);

    	}
    	mesureFlg = false;

     	return result;
    }

    @FXML
    void onGetValueBT(ActionEvent event) {
    	mesure();
    }

    @FXML
    void onCaliblationController(ActionEvent event) {
    	if( calibExFlg ) return;
    	calibExFlg = true;

    	if( !passwordCheck() ) {
    		calibExFlg = false;
    		return;
    	}

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

		tr = Executors.newSingleThreadScheduledExecutor();
		tr.scheduleAtFixedRate(tentionMesure, 0, 33, TimeUnit.MILLISECONDS);
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

    	//約60秒未満は保存しない
    	if(shotCnt > 100) {//startTimeオブジェクトがnullの時は実行しない
	    	if( System.currentTimeMillis() - startTime.getTime() > 60*1000 ) {
				if( !csvSaveLoad.saveDataSet(tention_dataset, startTime,ch1_max,ch1_min,ch1_ave,ch2_max,ch2_min,ch2_ave,shotCnt) ) {
					System.out.println("データーセット保存異常");
					Platform.runLater( () ->this.infoLB.setText("データーセット保存異常"));
				}
			}
    	}

    	ch1_tention.clear();
        ch2_tention.clear();
        ch1_max = 0;
        ch2_max = 0;
        ch1_min = 9999;
        ch2_min = 9999;
        ch1_ave = 0;
        ch2_ave = 0;
        shotCnt=0;

		//チャート用データーセットリセット
		Platform.runLater( () ->tention_dataset.getSeries(0).clear());
		Platform.runLater( () ->tention_dataset.getSeries(1).clear());
		Platform.runLater( () ->mesureCntLB.setText("----------"));

		//機器異常リセット ※eventがnullの時は自動停止時に呼び出されたと判断しリセットしない
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


    	resetExFlg = false;
    }

    /**
     * ボタンの表示、非表示の切り替え
     * @param visibleFlg true:表示 false:非表示
     */
    private void visibleBT(boolean visibleFlg) {
    	if( !visibleFlg ) {
			Platform.runLater( () -> this.calibrationMenuBT.setVisible(false));
			Platform.runLater( () -> this.settingMenuBT.setVisible(false));
			Platform.runLater( () -> this.resetBT.setVisible(false));
			Platform.runLater( () -> this.shutdownBT.setVisible(false));
    	}else {
			Platform.runLater( () -> this.calibrationMenuBT.setVisible(true));
			Platform.runLater( () -> this.settingMenuBT.setVisible(true));
			Platform.runLater( () -> this.resetBT.setVisible(true));
			Platform.runLater( () -> this.shutdownBT.setVisible(true));
    	}
    }

    private void mesure() {
    		if(mesureFlg) return;
    		//デバッグコード--------------------
    		if( debugFlg ) {
		    	try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    		//-----------------------------------
	    	double[][] result = getLoadCellValue();
	    	if( result[0][0] != -1 ) {
		    	Platform.runLater(() ->hxvalueLB1.setText(String.format("%.0f",result[0][0])));
		    	Platform.runLater(() ->hxvalueLB2.setText(String.format("%.0f",result[0][1])));

		    	Platform.runLater(() ->hxvalueLB4.setText(String.format("%.0f",result[1][0])));
		    	Platform.runLater(() ->hxvalueLB5.setText(String.format("%.0f",result[1][1])));
	    	}else {
		    	Platform.runLater(() ->hxvalueLB1.setText("Mesure Error"));
		    	Platform.runLater(() ->hxvalueLB2.setText("Mesure Error"));

		    	Platform.runLater(() ->hxvalueLB4.setText("Mesure Error"));
		    	Platform.runLater(() ->hxvalueLB5.setText("Mesure Error"));

	    	}

	    	if( Math.abs(result[0][1]) > 10 || Math.abs(result[1][1]) > 10 ) {
	    		mesureTreshFlg = true;
	    	}else {
	    		mesureTreshFlg = false;
	    	}


	    	if( !mesureStopFlg ) {
	    		shotCnt++;
    			//Platform.runLater(() ->judgmentLB.setTextFill(Paint.valueOf("0xffffffff")));

		    	if( result[0][0] != -1 ) {
		    		int judgmentFlg = 0;//0:合格 1～2:警告 3～:規格外
		    		//規格判定
		    		if( result[0][1] >
		    			settingMenu.ch1MaxErrorValue - (settingMenu.ch1MaxErrorValue*settingMenu.ch1RatioValue) ) {
		    			Platform.runLater(() ->infoLB.setText("CH1 MaxWarning"));
		    			judgmentFlg += 1;
		    		}
		    		if( result[0][1] > settingMenu.ch1MaxErrorValue) {
		    			Platform.runLater(() ->infoLB.setText("CH1 MaxOver!!"));
		    			judgmentFlg += 3;
		    		}
		    		if( result[0][1] <
	    			settingMenu.ch1MinErrorValue + (settingMenu.ch1MinErrorValue*settingMenu.ch1RatioValue) ) {
		    			Platform.runLater(() ->infoLB.setText("CH1 MinWarning"));
		    			judgmentFlg += 1;
		    		}
		    		if( result[0][1] < settingMenu.ch1MinErrorValue) {
		    			Platform.runLater(() ->infoLB.setText("CH1 MinLower!!"));
		    			judgmentFlg += 3;
		    		}
		    		if( result[1][1] >
	    			settingMenu.ch2MaxErrorValue - (settingMenu.ch2MaxErrorValue*settingMenu.ch2RatioValue) ) {
		    			Platform.runLater(() ->infoLB.setText("CH2 MaxWarning"));
		    			judgmentFlg += 1;
		    		}
		    		if( result[1][1] > settingMenu.ch2MaxErrorValue) {
		    			Platform.runLater(() ->infoLB.setText("CH2 MaxOver!!"));
		    			judgmentFlg += 3;
		    		}
		    		if( result[1][1] <
	    			settingMenu.ch2MinErrorValue + (settingMenu.ch2MinErrorValue*settingMenu.ch2RatioValue) ) {
		    			Platform.runLater(() ->infoLB.setText("CH2 MinWarning"));
		    			judgmentFlg += 1;
		    		}
		    		if( result[1][1] < settingMenu.ch2MinErrorValue) {
		    			Platform.runLater(() ->infoLB.setText("CH2 MinLower!!"));
		    			judgmentFlg += 3;
		    		}
		    		//判定表示
		    		if( judgmentFlg == 0 ) {
			    		Platform.runLater(() ->infoLB.setText("Measuring"));
		    			Platform.runLater(() ->judgmentLB.setTextFill(Paint.valueOf("#5eff00ff")));
		    			Platform.runLater(() ->judgmentLB.setText("OK"));
		    			Platform.runLater(() ->judgmentLB.setAlignment(Pos.CENTER));
		    		}else if( judgmentFlg ==1 || judgmentFlg ==2) {
		    			Platform.runLater(() ->judgmentLB.setTextFill(Paint.valueOf("#ffff00ff")));
		    			Platform.runLater(() ->judgmentLB.setText("!!!"));
		    			Platform.runLater(() ->judgmentLB.setAlignment(Pos.CENTER));
		    	    	//約60秒未満は鳴らさない
		    			if( !mp_error3.isPlaying() && !mesureErrFlg &&
		    					( System.currentTimeMillis() - startTime.getTime() > 60*1000 )) {
			    			mp_error3.play();
		    			}

		    		}else if( judgmentFlg > 2){
		    			Platform.runLater(() ->judgmentLB.setTextFill(Paint.valueOf("#ff0000ff")));
		    			Platform.runLater(() ->judgmentLB.setText("NG"));
		    			Platform.runLater(() ->judgmentLB.setAlignment(Pos.CENTER));
		    			if( shotCnt>100 && System.currentTimeMillis() - startTime.getTime() > 60*1000 ) {
		    				tentionErrFlg = true;
		    			}
		    		}

		    		//チャート更新
		    		long chartTime = System.currentTimeMillis() - startTime.getTime();//経過時間(mSec単位)
		    		//データーセットは表示の都合でDouble値で格納する
		    		Platform.runLater( () ->tention_dataset.getSeries(0).add((double)chartTime/1000.0,result[0][1]));
		    		Platform.runLater( () ->tention_dataset.getSeries(1).add((double)chartTime/1000.0,result[1][1]));
		    		//デバッグ用-------------------------------------------------------------------------------------
		    		if( debugFlg ) {
		    	        //System.out.println("[ShotCnt]="+shotCnt+" [dataSetSize]=" + ( (8+8)*2*shotCnt/1024) + "Kbyte");
		    		}
		    		//-----------------------------------------------------------------------------------------------
		    		//経過時間表示
		    		if( shotCnt > 0) {
			    		Platform.runLater( () ->mesureCntLB.setText( String.format("%d H %d M %d S",
			    				(int)Math.floor( (double)chartTime/1000.0/3600.0 ),
			    				(int)Math.floor( ((double)chartTime/1000.0/60.0) % 60),
			    				(int)(chartTime/1000) % 60
			    				)));
		    		}else {
		    			Platform.runLater( () ->mesureCntLB.setText("----------"));
		    		}

		    		//データーセットはdouble値 チャート表示は整数とする為、変換表示させる
		    		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_tention.getPlot()).getDomainAxis()).
							setRange( (chartTime/1000)<=60 ? 0 : (chartTime/1000)-60,(chartTime/1000)<1?1:(chartTime/1000) ));

		    		//データー追加
		    		ch1_tention.add(result[0][1]);
		    		ch2_tention.add(result[1][1]);
		    		if( ch1_max < result[0][1] ) ch1_max = result[0][1];
		    		if( ch1_min > result[0][1] ) ch1_min = result[0][1];
		    		if( ch2_max < result[1][1] ) ch2_max = result[1][1];
		    		if( ch2_min > result[1][1] ) ch2_min = result[1][1];
		    		ch1_ave +=  result[0][1];
		    		ch2_ave +=  result[1][1];
		    		Platform.runLater(() ->ch1AveLB.setText(String.format("%.0f",ch1_ave/shotCnt)));
		    		Platform.runLater(() ->ch2AveLB.setText(String.format("%.0f",ch2_ave/shotCnt)));
		    		Platform.runLater(() ->ch1MaxLB.setText(String.format("%.0f",ch1_max)));
		    		Platform.runLater(() ->ch1MinLB.setText(String.format("%.0f",ch1_min)));
		    		Platform.runLater(() ->ch2MaxLB.setText(String.format("%.0f",ch2_max)));
		    		Platform.runLater(() ->ch2MinLB.setText(String.format("%.0f",ch2_min)));


		    		final double rangeRatio = 0.4;
		    		double minRange = ch1_min<ch2_min?ch1_min-Math.abs(ch1_min*rangeRatio):ch2_min-Math.abs(ch2_min*rangeRatio);
		    		double maxRange = ch1_max>ch2_max?ch1_max+Math.abs(ch1_max*rangeRatio):ch2_max+Math.abs(ch2_max*rangeRatio);
		    		if( maxRange < minRange) {
		    			maxRange = 300;
		    			minRange = -300;
		    		}
		    		final double maxRange_ = maxRange;
		    		final double minRange_ = minRange;
		    		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_tention.getPlot()).getRangeAxis()).
							setRange(minRange_,maxRange_));
		    	}else {
			    	Platform.runLater(() ->hxvalueLB1.setText(String.format("%.0f",0.0)));
			    	Platform.runLater(() ->hxvalueLB2.setText(String.format("%.0f",0.0)));

			    	Platform.runLater(() ->hxvalueLB4.setText(String.format("%.0f",0.0)));
			    	Platform.runLater(() ->hxvalueLB5.setText(String.format("%.0f",0.0)));
		    		Platform.runLater(() ->infoLB.setText("Mesure Error"));
		    	}
	    	}else{
		    	Platform.runLater(() ->hxvalueLB1.setText(String.format("%.0f",0.0)));
		    	Platform.runLater(() ->hxvalueLB2.setText(String.format("%.0f",0.0)));

		    	Platform.runLater(() ->hxvalueLB4.setText(String.format("%.0f",0.0)));
		    	Platform.runLater(() ->hxvalueLB5.setText(String.format("%.0f",0.0)));
		    	Platform.runLater(() ->infoLB.setText("Mesure Stop"));
	    	}
	    	//テンション異常
	    	if( tentionErrFlg ) {
    			if( !mp_error.isPlaying() && !mesureErrFlg ) {
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
    			Platform.runLater(() ->judgmentLB.setAlignment(Pos.CENTER));
    		}
    		Platform.runLater(() ->this.ch1ErrCntLB.setText(String.valueOf( mesureErrCnt[0] )));
    		Platform.runLater(() ->this.ch2ErrCntLB.setText(String.valueOf( mesureErrCnt[1] )));

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
        GpioUtil.enableNonPrivilegedAccess();

        GpioController gpio = GpioFactory.getInstance();

        for(int i=0;i<2;i++) {
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
        //設定データーロード
        csvSaveLoad.settingValueLoad();

        //測定データリセット実行
        onResetBT(null);

        for(int i=0;i<2;i++) {
	    	hx[i].emptyValue =  CaliblationController.emptyValue[i];
	    	hx[i].calibrationValue = CaliblationController.calibValue[i];
	    	hx[i].calibrationWeight =CaliblationController.calibWeight[i];
	    	hx[i].resolution = CaliblationController.resolution[i];
    	}

        //メディアプレイヤー準備
		File jarFile = null;
		try {
			jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String filePath = jarFile.getParent() + File.separator + wavFileString;
		mp_error = new AudioClip(new File(filePath).toURI().toString());//テンション規格外
		filePath = jarFile.getParent() + File.separator + wav2FileString;
		mp_error2 = new AudioClip(new File(filePath).toURI().toString());//機器以上
		filePath = jarFile.getParent() + File.separator + wav3FileString;
		mp_error3 = new AudioClip(new File(filePath).toURI().toString());//機器以上



        //チャートオブジェクト作成
        chart_tention = chartFact();

        //自動計測開始
 	   tentionMesure = new Runnable() {
		@Override
 		   public void run() {
			  mesure();
 			  if( mesureTreshFlg ) {
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
 			  if( mesureStopFlg && System.currentTimeMillis() - lockedTimer > 8 * 60 *60 *1000 ) {
 				  shotCnt = 0;
 				  mesureErrCnt[0] = 0;
 				  mesureErrCnt[1] = 0;
 			  }
 		   }
 	   };
	   tr.scheduleAtFixedRate(tentionMesure, 0, 33, TimeUnit.MILLISECONDS);


    }
    private JFreeChart chartFact() {
    	XYSeriesCollection tentionDataSet;
		tentionDataSet = getChartData();
		JFreeChart chart = createInitChart("TentionRealTimeMonitor","(g)","ElapsedTime(sec)",tentionDataSet ,0.0,300);
		ChartViewer chV = new ChartViewer(chart);
        chV.addChartMouseListener( new ChartMouseListenerFX() {
				@Override
				public void chartMouseClicked(ChartMouseEventFX e) {
					XYPlot xyplot = e.getChart().getXYPlot();
					double value = xyplot.getRangeCrosshairValue();
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
                false,//凡例
                false,//tooltips
                false);//urls
        // 背景色を設定
    	chart.setBackgroundPaint(ChartColor.WHITE);

        // 凡例の設定
        //LegendTitle lt = chart.getLegend();
        //lt.setFrame(new BlockBorder(1d, 2d, 3d, 4d, ChartColor.WHITE));

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
        Stroke stroke = new BasicStroke(1.0f);
        renderer.setSeriesStroke(0, stroke);
		//色
        renderer.setSeriesPaint(0, ChartColor.BLUE);

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

