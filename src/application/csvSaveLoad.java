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

	 /* キャリブレーションデーターCSV書き込み
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

			String[] headStr = reader.readNext();//ヘッダー読み込み
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
			List<Double> ch1TentionRawDataList, List<Double> ch2TentionRawDataList,
			XYSeriesCollection dataSet,Timestamp startTime,
			double ch1_max, double ch1_min, double ch1_ave,
			double ch2_max, double ch2_min, double ch2_ave, long shotCnt) {
		Timestamp EndTime = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm'm'ss's'");
        String StartDate = sdf.format(startTime);
        String EndDate = sdf.format(EndTime);

        CSVWriter writer;
		try {
			File folder = new File( MyUtil.getJarFolder() + "tentionlog");
	    	if( !folder.exists()) {
	    		if( !folder.mkdir() ) {
	    			System.out.println("tentionlogフォルダ作成失敗");
	    		}
	    	}
	    	writer = new CSVWriter(new FileWriter( MyUtil.getJarFolder() + "tentionlog/"+StartDate + "---" + EndDate + ".csv"));

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
			writer.writeNext(new String[] { "[MesureErrorCount]" });
			writer.writeNext(new String[] {
					String.valueOf(MainScreenController.mesureErrCnt[0]),
					String.valueOf(MainScreenController.mesureErrCnt[1]) });

        	//日時データー
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[Date]" });
        	headStr = new String[2];
    		headStr[0]="StartDate";
    		headStr[1]="EndDate";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        subStr = new String[2];
    		subStr[0]=StartDate;
    		subStr[1]=EndDate;
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
        	headStr = new String[7];
    		headStr[0]="ch1_max";
    		headStr[1]="ch1_min";
    		headStr[2]="ch1_ave";
    		headStr[3]="ch2_max";
    		headStr[4]="ch2_min";
    		headStr[5]="ch2_ave";
    		headStr[6]="mesureCount";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        subStr = new String[7];
    		subStr[0]=String.valueOf( ch1_max );
    		subStr[1]=String.valueOf( ch1_min );
    		subStr[2]=String.valueOf( ch1_ave / shotCnt);
    		subStr[3]=String.valueOf( ch2_max );
    		subStr[4]=String.valueOf( ch2_min );
    		subStr[5]=String.valueOf( ch2_ave / shotCnt);
    		subStr[6]=String.valueOf( shotCnt );
        	writer.writeNext(subStr);

        	//生データー
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[RawData]" });
        	headStr = new String[5];
    		headStr[0]="ElapsedTime(sec)";
    		headStr[1]="CH1 tention(g)";
    		headStr[2]="CH2 tention(g)";
    		headStr[3]="CH1 RawTention(g)";
    		headStr[4]="CH2 RawTention(g)";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        int count = dataSet.getSeries(0).getItems().size();
	        subStr = new String[5];
	        for(int i=0;i<count;i++) {
	        	subStr[0] = (String.valueOf(
	        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(0).getItems().get(i)).getX()));
	        	subStr[1] = (String.valueOf(
	        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(0).getItems().get(i)).getYValue()));
	        	subStr[2] = (String.valueOf(
	        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(1).getItems().get(i)).getYValue()));
	        	subStr[3] = (String.valueOf(ch1TentionRawDataList.get(i)));
	        	subStr[4] = (String.valueOf(ch2TentionRawDataList.get(i)));
	        	writer.writeNext(subStr);
	        }

	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
