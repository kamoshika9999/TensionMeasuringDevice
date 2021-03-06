package application;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import org.jfree.data.xy.XYSeriesCollection;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class csvSaveLoad {

	 /** キャリブレーションデーターCSV書き込み
	 * @return
	 */
	public static boolean calibDataCsvWrite(
					long[] emptyValue_,long[] calibValue_,double[] weight_,double[] resolution_) {
		String[] headStr = new String[8];
		headStr[0]="CH1 EmptyValue";
		headStr[1]="CH1 CalibValue";
		headStr[2]="CH1 Weight";
		headStr[3]="CH1 Resolution";

		headStr[4]="CH2 EmptyValue";
		headStr[5]="CH2 CalibValue";
		headStr[6]="CH2 Weight";
		headStr[7]="CH1 Resolution";

		CSVWriter writer;

		try {
			writer = new CSVWriter(new FileWriter( MyUtil.getJarFolder() + "init.csv"));
	        writer.writeNext(headStr);  //ヘッダー書き込み

        	String[] subStr= new String[ 4*2 ];
	        for(int i=0;i<2;i++) {
	        	subStr[i*4] = String.format("%d", emptyValue_[i]);
	        	subStr[i*4+1] = String.format("%d", calibValue_[i]);
	        	subStr[i*4+2] = String.format("%f",weight_[i]);
	        	subStr[i*4+3] = String.format("%f",resolution_[i]);
	        }
        	writer.writeNext(subStr);
	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
   }

	public static boolean calibDataLoad(long[] emptyValue_,long[] calibValue_,double[] weight_,double[] resolution_) {
		try {
			FileReader fr = new FileReader( MyUtil.getJarFolder() + "init.csv");
			CSVReader reader=new CSVReader( fr );

			reader.readNext();//ヘッダー読み込み
			String[] subStr = reader.readNext();//キャリブレーションデータ読み込み
			reader.close();


			for(int i=0;i<2;i++) {
				CaliblationController.emptyValue[i] = Long.valueOf(subStr[i*4+0]);
				CaliblationController.calibValue[i] = Long.valueOf(subStr[i*4+1]);
				CaliblationController.calibWeight[i] = Double.valueOf(subStr[i*4+2]);
				CaliblationController.resolution[i] = Double.valueOf(subStr[i*4+3]);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	public static boolean settingValueSave() {
		/*
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
		*/
		String[] headStr = new String[12];
		headStr[0]="ch1RatioValue";
		headStr[1]="ch1MaxErrorValue";
		headStr[2]="ch1MinErrorValue";

		headStr[3]="ch2RatioValue";
		headStr[4]="ch2MaxErrorValue";
		headStr[5]="ch2MinErrorValue";

		headStr[6]="ch1TareValue";
		headStr[7]="ch2TareValue";
		headStr[8]="CH1SignInversionFlg";
		headStr[9]="CH2SignInversionFlg";

		headStr[10]="movingAverageTime";
		headStr[11]="graphXaxisTime";

		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter( MyUtil.getJarFolder() + "init2.csv"));
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        String[] subStr = new String[12];

	        subStr[0] = String.valueOf( settingMenu.ratioValue[0] );
	        subStr[1] = String.valueOf( settingMenu.maxErrorValue[0] );
	        subStr[2] = String.valueOf( settingMenu.minErrorValue[0] );

	        subStr[3] = String.valueOf( settingMenu.ratioValue[1] );
	        subStr[4] = String.valueOf( settingMenu.maxErrorValue[1] );
	        subStr[5] = String.valueOf( settingMenu.minErrorValue[1] );

	        subStr[6] = String.valueOf( settingMenu.tareValue[0] );
	        subStr[7] = String.valueOf( settingMenu.tareValue[1] );
	        subStr[8] = String.valueOf( settingMenu.signInversionFlg[0] );
	        subStr[9] = String.valueOf( settingMenu.signInversionFlg[1] );

	        subStr[10] = String.valueOf( settingMenu.movingAverageTime );
	        subStr[11] = String.valueOf( settingMenu.graphXaxisTime );

        	writer.writeNext(subStr);
	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean settingValueLoad() {
		/*
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
		*/

		try {
			FileReader fr = new FileReader( MyUtil.getJarFolder() + "init2.csv");
			CSVReader reader=new CSVReader( fr );
			reader.readNext();//ヘッダー読み込み
			String[] subStr = reader.readNext();//キャリブレーションデータ読み込み
			reader.close();

			settingMenu.ratioValue[0] = Double.valueOf(subStr[0]);
			settingMenu.maxErrorValue[0] = Long.valueOf(subStr[1]);
			settingMenu.minErrorValue[0] = Long.valueOf(subStr[2]);

			settingMenu.ratioValue[1] = Double.valueOf(subStr[3]);
			settingMenu.maxErrorValue[1] = Long.valueOf(subStr[4]);
			settingMenu.minErrorValue[1] = Long.valueOf(subStr[5]);

			settingMenu.tareValue[0] = Double.valueOf(subStr[6]);
			settingMenu.tareValue[1] = Double.valueOf(subStr[7]);
			settingMenu.signInversionFlg[0] = subStr[8].equals("false")?false:true;
			settingMenu.signInversionFlg[1] = subStr[9].equals("false")?false:true;

			settingMenu.movingAverageTime = Double.valueOf(subStr[10]);
			settingMenu.graphXaxisTime = Double.valueOf(subStr[11]);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	//データーセットを保存する
	public static boolean saveDataSet(
			List<Double> ch1TensionRawDataList, List<Double> ch2TensionRawDataList,
			XYSeriesCollection dataSet,Timestamp startTime,Timestamp machineStartTime,
			double ch1_max, double ch1_min, double ch1_ave,
			double ch2_max, double ch2_min, double ch2_ave, int[] shotCnt,int mesureCnt) {
		Timestamp EndTime = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm'm'ss's'");
        String StartDate = sdf.format(startTime);
        String EndDate = sdf.format(EndTime);
        String MachinStartDate = sdf.format(machineStartTime);

        CSVWriter writer;
		try {
			File folder = new File( MyUtil.getJarFolder() + "tensionlog");
	    	if( !folder.exists()) {
	    		if( !folder.mkdir() ) {
	    			System.out.println("tensionlogフォルダ作成失敗");
	    		}
	    	}
	    	writer = new CSVWriter(new FileWriter( MyUtil.getJarFolder() + "tensionlog/"+StartDate + "---" + EndDate + ".csv"));

			//キャリブレーションデーター
			writer.writeNext(new String[] { "[CalibrationData]" });
			String[] headStr = new String[8];
			headStr[0]="CH1 EmptyValue";
			headStr[1]="CH1 CalibValue";
			headStr[2]="CH1 Weight";
			headStr[3]="CH1 Resolution";

			headStr[4]="CH2 EmptyValue";
			headStr[5]="CH2 CalibValue";
			headStr[6]="CH2 Weight";
			headStr[7]="CH1 Resolution";

	        writer.writeNext(headStr);  //ヘッダー書き込み

        	String[] subStr= new String[ 4*2 ];
	        for(int i=0;i<2;i++) {
	        	subStr[i*4] = String.format("%d", CaliblationController.emptyValue[i]);
	        	subStr[i*4+1] = String.format("%d", CaliblationController.calibValue[i]);
	        	subStr[i*4+2] = String.format("%f",CaliblationController.calibWeight[i]);
	        	subStr[i*4+3] = String.format("%f",CaliblationController.resolution[i]);
	        }
        	writer.writeNext(subStr);

        	//設定データー
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[SettingData]" });
        	headStr = new String[9];
    		headStr[0]="ch1RatioValue";
    		headStr[1]="ch1MaxErrorValue";
    		headStr[2]="ch1MinErrorValue";
    		headStr[3]="ch2RatioValue";
    		headStr[4]="ch2MaxErrorValue";
    		headStr[5]="ch2MinErrorValue";
    		headStr[6]="ch1TareValue";
    		headStr[7]="ch2TareValue";
    		headStr[8]="movingAverageTime";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        subStr = new String[9];
	        subStr[0] = String.valueOf( settingMenu.ratioValue[0] );
	        subStr[1] = String.valueOf( settingMenu.maxErrorValue[0] );
	        subStr[2] = String.valueOf( settingMenu.minErrorValue[0] );
	        subStr[3] = String.valueOf( settingMenu.ratioValue[1] );
	        subStr[4] = String.valueOf( settingMenu.maxErrorValue[1] );
	        subStr[5] = String.valueOf( settingMenu.minErrorValue[1] );
	        subStr[6] = String.valueOf( settingMenu.tareValue[0] );
	        subStr[7] = String.valueOf( settingMenu.tareValue[1] );
	        subStr[8] = String.valueOf( settingMenu.movingAverageTime );
        	writer.writeNext(subStr);

        	//エラーカウント
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[CH1 MesureErrorCount]","[CH2 MesureErrorCount]" });
			writer.writeNext(new String[] {
					String.valueOf(MainScreenController.mesureErrCnt[0]),
					String.valueOf(MainScreenController.mesureErrCnt[1]) });

        	//日時データー
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[Date]" });
        	headStr = new String[3];
    		headStr[0]="StartDate";
    		headStr[1]="EndDate";
    		headStr[2]="MachinStartDate";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        subStr = new String[3];
    		subStr[0]=StartDate;
    		subStr[1]=EndDate;
    		subStr[2]= MachinStartDate;
        	writer.writeNext(subStr);
        	//統計量
        	/*
			    public double ch1_max = 0;
			    public double ch2_max = 0;
			    public double ch1_min = 9999;
			    public double ch2_min = 9999;
			    public double ch1_ave = 0;
			    public double ch2_ave = 0;
			    public long shotCnt = 0;
        	 */
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[Statistics]" });
        	headStr = new String[9];
    		headStr[0]="ch1_max";
    		headStr[1]="ch1_min";
    		headStr[2]="ch1_ave";
    		headStr[3]="ch2_max";
    		headStr[4]="ch2_min";
    		headStr[5]="ch2_ave";
    		headStr[6]="CH1 mesureCount";
    		headStr[7]="CH2 mesureCount";
    		headStr[8]="AllmesureCount";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        subStr = new String[9];
    		subStr[0]=String.valueOf( ch1_max );
    		subStr[1]=String.valueOf( ch1_min );
    		subStr[2]=String.valueOf( ch1_ave / shotCnt[0]);
    		subStr[3]=String.valueOf( ch2_max );
    		subStr[4]=String.valueOf( ch2_min );
    		subStr[5]=String.valueOf( ch2_ave / shotCnt[0]);
    		subStr[6]=String.valueOf( shotCnt[0] );
    		subStr[7]=String.valueOf( shotCnt[1] );
    		subStr[8]=String.valueOf( mesureCnt );

        	writer.writeNext(subStr);

        	//生データー
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[RawData]" });
        	headStr = new String[6];
    		headStr[0]="Ch1 ElapsedTime(sec)";
    		headStr[1]="CH1 tension(g)";
    		headStr[2]="CH1 RawTension(g)";

    		headStr[3]="Ch2 ElapsedTime(sec)";
    		headStr[4]="CH2 tension(g)";
    		headStr[5]="CH2 RawTension(g)";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        int ch1_count = dataSet.getSeries(0).getItems().size();
	        int ch2_count = dataSet.getSeries(1).getItems().size();
	        subStr = new String[6];
	        int index = 0;
	        while( index < ch1_count || index < ch2_count) {
	        	if( index < ch1_count ) {
		        	subStr[0] = (String.valueOf(
		        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(0).getItems().get(index)).getX()));
		        	subStr[1] = (String.valueOf(
		        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(0).getItems().get(index)).getYValue()));
		        	subStr[2] = (String.valueOf(ch1TensionRawDataList.get(index)));
	        	}else {
	        		subStr[0] ="";
	        		subStr[1] ="";
	        		subStr[2] ="";
	        	}

	        	if( index < ch2_count ) {
		        	subStr[3] = (String.valueOf(
		        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(1).getItems().get(index)).getX()));
		        	subStr[4] = (String.valueOf(
		        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(1).getItems().get(index)).getYValue()));
		        	subStr[5] = (String.valueOf(ch2TensionRawDataList.get(index)));
	        	}else {
	        		subStr[3] ="";
	        		subStr[4] ="";
	        		subStr[5] ="";

	        	}
	        	index++;
	        	writer.writeNext(subStr);
	        }

	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	 /** 分銅による測定結果を保存する
	 * @return
	 */
	public static boolean STDsampleMesureResultLogWrite(int ch1AVE,int ch2AVE) {
		CSVWriter writer;

		Timestamp nowTime = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm'm'ss's'");
        String nowDate = sdf.format(nowTime);


		try {
			File folder = new File( MyUtil.getJarFolder() + "std_sample_log");
	    	if( !folder.exists()) {
	    		if( !folder.mkdir() ) {
	    			System.out.println("std_sample_logフォルダ作成失敗");
	    		}
	    	}
	    	writer = new CSVWriter(new FileWriter( MyUtil.getJarFolder()
	    					+ "std_sample_log/" + nowDate  + ".csv"));

	    	//計測データー書き込み
	    	String[] headStr = new String[3];
			headStr[0]="date";
			headStr[1]="CH1 AveValue";
			headStr[2]="CH2 AveValue";
	    	writer.writeNext(headStr);  //ヘッダー書き込み

	    	String[] subStr= new String[3];
	    	subStr[0] = nowDate;
	    	subStr[1] = String.format("%d",ch1AVE );
	    	subStr[2] = String.format("%d",ch2AVE );
        	writer.writeNext(subStr);


			//キャリブレーションデーター
			writer.writeNext(new String[] { "[CalibrationData]" });
			String[] headStr2 = new String[8];
			headStr2[0]="CH1 EmptyValue";
			headStr2[1]="CH1 CalibValue";
			headStr2[2]="CH1 Weight";
			headStr2[3]="CH1 Resolution";

			headStr2[4]="CH2 EmptyValue";
			headStr2[5]="CH2 CalibValue";
			headStr2[6]="CH2 Weight";
			headStr2[7]="CH1 Resolution";
	        writer.writeNext(headStr2);  //ヘッダー書き込み

	        subStr= new String[ 4*2 ];
	        for(int i=0;i<2;i++) {
	        	subStr[i*4] = String.format("%d", CaliblationController.emptyValue[i]);
	        	subStr[i*4+1] = String.format("%d", CaliblationController.calibValue[i]);
	        	subStr[i*4+2] = String.format("%f",CaliblationController.calibWeight[i]);
	        	subStr[i*4+3] = String.format("%f",CaliblationController.resolution[i]);
	        }
        	writer.writeNext(subStr);

        	//設定データー
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[SettingData]" });
        	headStr = new String[9];
    		headStr[0]="ch1RatioValue";
    		headStr[1]="ch1MaxErrorValue";
    		headStr[2]="ch1MinErrorValue";
    		headStr[3]="ch2RatioValue";
    		headStr[4]="ch2MaxErrorValue";
    		headStr[5]="ch2MinErrorValue";
    		headStr[6]="ch1TareValue";
    		headStr[7]="ch2TareValue";
    		headStr[8]="movingAverageTime";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        subStr = new String[9];
	        subStr[0] = String.valueOf( settingMenu.ratioValue[0] );
	        subStr[1] = String.valueOf( settingMenu.maxErrorValue[0] );
	        subStr[2] = String.valueOf( settingMenu.minErrorValue[0] );
	        subStr[3] = String.valueOf( settingMenu.ratioValue[1] );
	        subStr[4] = String.valueOf( settingMenu.maxErrorValue[1] );
	        subStr[5] = String.valueOf( settingMenu.minErrorValue[1] );
	        subStr[6] = String.valueOf( settingMenu.tareValue[0] );
	        subStr[7] = String.valueOf( settingMenu.tareValue[1] );
	        subStr[8] = String.valueOf( settingMenu.movingAverageTime );
        	writer.writeNext(subStr);

	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
  }

}
