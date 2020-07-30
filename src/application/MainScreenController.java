package application;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
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

    //HX711のチャンネル数
	static final int ch_cnt =2;
    //HX711 接続ピンリスト BCM番号で指定 「gpio readall」 で物理ピンと確認すること
	final Pin[] pinNoDAT = {RaspiPin.GPIO_21,RaspiPin.GPIO_23};
	final Pin[] pinNoCLK = {RaspiPin.GPIO_22,RaspiPin.GPIO_24};
	//HX711 オブジェクト
	GpioPinDigitalInput[] pinHXDAT = new GpioPinDigitalInput[ch_cnt];
    GpioPinDigitalOutput[] pinHXCLK = new GpioPinDigitalOutput[ch_cnt];
    HX711[] hx = new HX711[ch_cnt];

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

    //計測中フラグ
    boolean mesureFlg =false;
    boolean mesureTreshFlg =false;//計測結果が絶対値で10g超えている場合True;
    boolean mesureStopFlg = false;//trueで計測停止
    long lockedTimer =System.currentTimeMillis();
	long lockedTimerThresh = 2000;
	//測定計測エラー数
	int[] mesureErrCnt = new int[ch_cnt];

	//スレッドオブジェクト
	ScheduledExecutorService tr = Executors.newSingleThreadScheduledExecutor();
	Runnable tentionMesure;

    /**
     *
     * @return  double[ch][0] hx.value   double[ch][1] hx.weight
     */
    public double[][] getLoadCellValue(){
    	mesureFlg = true;

    	double[][] result= {{-1,-1},{-1,-1}};
		double[] aveValue= {0,0};
		double[] aveWeight = {0,0};
		final int rpeetCnt = 3;//n回平均を取る
		double[][] tmpValue = new double[ch_cnt][rpeetCnt];
		double[] maxValue= new double[ch_cnt];
		double[] minValue= new double[ch_cnt];
		maxValue[0] = 0;maxValue[1]=0;
		minValue[0] = 99999999;minValue[1]=99999999;
    	int[] enableCnt = new int[ch_cnt];
		try {
    		for(int j=0;j<rpeetCnt;j++) {
    			for(int i=0;i<ch_cnt;i++) {
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
				        	//System.out.println("faileMesureCnt = " + cnt );
				        }else {
				        	System.out.println("faileMesureCnt(RpeetOver10) = " + cnt );
				        }
        			}
		        }

    		}
    		//測定のレンジがhx[i].resolution * 10倍(10g)を超えていたら結果は-1になる
    		boolean flg=true;
    		for(int i=0;i<ch_cnt;i++) {
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
	    		for(int i=0;i<ch_cnt;i++) {
			        aveValue[i] /= enableCnt[i];
			        aveWeight[i] /= enableCnt[i];
	    		}
	    		for(int i=0;i<ch_cnt;i++) {
	    			result[i][0] = aveValue[i];
	    			result[i][1] = aveWeight[i];
	    		}
    		}
    		//System.out.println("CH1Resolution="+hx[0].resolution);
    		//System.out.println("CH1 MAX="+maxValue[0]+" CH1 MIN="+minValue[0]+"CH1 Weight="+aveWeight[0]);



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

		CaliblationController.hx = this.hx;//参照を渡す
		//設定ウィンドウを開く
		stage.showAndWait();

		tr = Executors.newSingleThreadScheduledExecutor();
		tr.scheduleAtFixedRate(tentionMesure, 0, 33, TimeUnit.MILLISECONDS);

    }

    /**
     * データーリセット
     * @param event
     */
    @FXML
    void onResetBT(ActionEvent event) {
        ch1_tention.clear();
        ch2_tention.clear();
        ch1_max = 0;
        ch2_max = 0;
        ch1_min = 9999;
        ch2_min = 9999;
        ch1_ave = 0;
        ch2_ave = 0;
        shotCnt=0;
        for(int i=0;i<ch_cnt;i++) {
        	mesureErrCnt[i] = 0;
        }
		//チャート更新
		Platform.runLater( () ->tention_dataset.getSeries(0).clear());
		Platform.runLater( () ->tention_dataset.getSeries(1).clear());
		Platform.runLater( () ->mesureCntLB.setText(String.valueOf(shotCnt)));
		Platform.runLater(() ->this.ch1ErrCntLB.setText(String.valueOf( mesureErrCnt[0] )));
		Platform.runLater(() ->this.ch2ErrCntLB.setText(String.valueOf( mesureErrCnt[1] )));

    }

    private void mesure() {
    		if(mesureFlg) return;

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
	    	//----------デバッグ用-------
	    	//mesureTreshFlg = true;
	    	//---------------------------

	    	if( Math.abs(result[0][1]) > 10 || Math.abs(result[1][1]) > 10 ) {
	    		mesureTreshFlg = true;
	    	}else {
	    		mesureTreshFlg = false;
	    	}


	    	if( !mesureStopFlg ) {
	    		shotCnt++;
	    		Platform.runLater( () ->mesureCntLB.setText(String.valueOf(shotCnt)));


		    	if( result[0][0] != -1 ) {
		    		Platform.runLater(() ->infoLB.setText("Measuring"));

		    		//チャート更新
		    		Platform.runLater( () ->tention_dataset.getSeries(0).add(shotCnt,result[0][1]));
		    		Platform.runLater( () ->tention_dataset.getSeries(1).add(shotCnt,result[1][1]));


		    		Platform.runLater( () ->((NumberAxis)((XYPlot)chart_tention.getPlot()).getDomainAxis()).
							setRange( shotCnt<=2000 ? 0 : shotCnt-2000,shotCnt<1?1:shotCnt ));

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

		    		//System.out.println("ChartRangeMax="+(ch1_max>ch2_max?ch1_max+Math.abs(ch1_max*rangeRatio):ch2_max+Math.abs(ch2_max*rangeRatio)));
		    	}else {
			    	Platform.runLater(() ->hxvalueLB1.setText(String.format("%.0f",0.0)));
			    	Platform.runLater(() ->hxvalueLB2.setText(String.format("%.0f",0.0)));

			    	Platform.runLater(() ->hxvalueLB4.setText(String.format("%.0f",0.0)));
			    	Platform.runLater(() ->hxvalueLB5.setText(String.format("%.0f",0.0)));
		    		Platform.runLater(() ->infoLB.setText("Mesure Error"));
		    		Platform.runLater(() ->this.ch1ErrCntLB.setText(String.valueOf( mesureErrCnt[0] )));
		    		Platform.runLater(() ->this.ch2ErrCntLB.setText(String.valueOf( mesureErrCnt[1] )));
		    	}
	    	}else{
		    	Platform.runLater(() ->hxvalueLB1.setText(String.format("%.0f",0.0)));
		    	Platform.runLater(() ->hxvalueLB2.setText(String.format("%.0f",0.0)));

		    	Platform.runLater(() ->hxvalueLB4.setText(String.format("%.0f",0.0)));
		    	Platform.runLater(() ->hxvalueLB5.setText(String.format("%.0f",0.0)));
		    	Platform.runLater(() ->infoLB.setText("Mesure Stop"));

	    	}

    }

    /**
     * 設定メニュー
     * @param event
     */
    @FXML
    void onSettingMenuBT(ActionEvent event) {

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

        //チャートオブジェクト作成
        chart_tention = chartFact();

        //自動計測開始

 	   tentionMesure = new Runnable() {

		@Override
 		   public void run() {
			  mesure();
 			  if( mesureTreshFlg ) {
 				  lockedTimer = System.currentTimeMillis();
 				  if(mesureStopFlg) {
 					  onResetBT(null);
 				  }

 			  }
 			  if( System.currentTimeMillis() - lockedTimer > lockedTimerThresh) {
 				  mesureStopFlg  = true;
 			  }else {
 				 mesureStopFlg  = false;
 			  }
 		   }
 	   };
	   tr.scheduleAtFixedRate(tentionMesure, 0, 33, TimeUnit.MILLISECONDS);

    }
    private JFreeChart chartFact() {
    	XYSeriesCollection ch1_tention;
		ch1_tention = getChartData();
		JFreeChart chart = createInitChart("Tention","(g)","n",ch1_tention ,0.0,300);
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

