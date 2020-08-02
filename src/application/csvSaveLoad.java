package application;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.jfree.data.xy.XYSeriesCollection;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class csvSaveLoad {

	 /* キャリブレーションデーターCSV書き込み
	 * @return
	 */
	public static boolean calibDataCsvWrite(
					long[] emptyValue_,long[] calibValue_,double[] weight_,double[] resolution_) {
        //実行するjarのフォルダを得る-----------------------------------------------------------------------------
		File jarFile = null;
		try {
			jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String folderPath = jarFile.getParent() + File.separator;
		//--------------------------------------------------------------------------------------------------------
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
			writer = new CSVWriter(new FileWriter(folderPath+"init.csv"));
	        writer.writeNext(headStr);  //ヘッダー書き込み

        	String[] subStr= new String[ 4*MainScreenController.ch_cnt ];
	        for(int i=0;i<MainScreenController.ch_cnt;i++) {
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
        //実行するjarのフォルダを得る-----------------------------------------------------------------------------
		File jarFile = null;
		try {
			jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String folderPath = jarFile.getParent() + File.separator;
		//--------------------------------------------------------------------------------------------------------
		try {
			FileReader fr = new FileReader(folderPath+"init.csv");
			CSVReader reader=new CSVReader( fr );

			String[] headStr = reader.readNext();//ヘッダー読み込み
			String[] subStr = reader.readNext();//キャリブレーションデータ読み込み
			reader.close();

			for(int i=0;i<MainScreenController.ch_cnt;i++) {
				System.out.println(headStr.length + " " + headStr[i]);
				System.out.println(subStr.length + " " + subStr[i]);
			}

			for(int i=0;i<MainScreenController.ch_cnt;i++) {
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
        //実行するjarのフォルダを得る-----------------------------------------------------------------------------
		File jarFile = null;
		try {
			jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String folderPath = jarFile.getParent() + File.separator;
		//--------------------------------------------------------------------------------------------------------
		/*
	    public static double ch1RatioValue;
	    public static long ch1MaxErrorValue;
	    public static long ch1MinErrorValue;
	    public static double ch2RatioValue;
	    public static long ch2MaxErrorValue;
	    public static long ch2MinErrorValue;
	    */

		String[] headStr = new String[8];
		headStr[0]="ch1RatioValue";
		headStr[1]="ch1MaxErrorValue";
		headStr[2]="ch1MinErrorValue";
		headStr[3]="ch2RatioValue";
		headStr[4]="ch2MaxErrorValue";
		headStr[5]="ch2MinErrorValue";
		headStr[6]="ch1TareValue";
		headStr[7]="ch2TareValue";

		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter(folderPath+"init2.csv"));
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        String[] subStr = new String[8];

	        subStr[0] = String.valueOf( settingMenu.ch1RatioValue );
	        subStr[1] = String.valueOf( settingMenu.ch1MaxErrorValue );
	        subStr[2] = String.valueOf( settingMenu.ch1MinErrorValue );
	        subStr[3] = String.valueOf( settingMenu.ch2RatioValue );
	        subStr[4] = String.valueOf( settingMenu.ch2MaxErrorValue );
	        subStr[5] = String.valueOf( settingMenu.ch2MinErrorValue );
	        subStr[6] = String.valueOf( settingMenu.ch1TareValue );
	        subStr[7] = String.valueOf( settingMenu.ch2TareValue );

        	writer.writeNext(subStr);
	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean settingValueLoad() {
        //実行するjarのフォルダを得る-----------------------------------------------------------------------------
		File jarFile = null;
		try {
			jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String folderPath = jarFile.getParent() + File.separator;
		//--------------------------------------------------------------------------------------------------------
		/*
	    public static double ch1RatioValue;
	    public static long ch1MaxErrorValue;
	    public static long ch1MinErrorValue;
	    public static double ch2RatioValue;
	    public static long ch2MaxErrorValue;
	    public static long ch2MinErrorValue;
	    public static double ch1TareValue;
	    public static double ch2TareValue;
	    */
		try {
			FileReader fr = new FileReader(folderPath+"init2.csv");
			CSVReader reader=new CSVReader( fr );

			String[] headStr = reader.readNext();//ヘッダー読み込み
			String[] subStr = reader.readNext();//キャリブレーションデータ読み込み
			reader.close();

			settingMenu.ch1RatioValue = Double.valueOf(subStr[0]);
			settingMenu.ch1MaxErrorValue = Long.valueOf(subStr[1]);
			settingMenu.ch1MinErrorValue = Long.valueOf(subStr[2]);
			settingMenu.ch2RatioValue = Double.valueOf(subStr[3]);
			settingMenu.ch2MaxErrorValue = Long.valueOf(subStr[4]);
			settingMenu.ch2MinErrorValue = Long.valueOf(subStr[5]);
			settingMenu.ch1TareValue = Double.valueOf(subStr[6]);
			settingMenu.ch2TareValue = Double.valueOf(subStr[7]);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	//データーセットを保存する
	public static boolean saveDataSet(XYSeriesCollection dataSet,Timestamp startTime) {
		Timestamp EndTime = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm'm'ss's'");
        String StartDate = sdf.format(startTime);
        String EndDate = sdf.format(EndTime);
        
        //デバッグコード-----------------------------------------------------------------------------------------------
        if( MainScreenController.debugFlg) {
	        String fileName = "sample2.wav";
	        File jarFile = null;
			try {
				jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
	        String inputFilePath = jarFile.getParent() + File.separator + fileName;
	        File tmpFile = new File(inputFilePath);
	        System.out.println(inputFilePath);
        }
        //-------------------------------------------------------------------------------------------------------------
        
        CSVWriter writer;
		try {
	        //実行するjarのフォルダを得る-----------------------------------------------------------------------------
			File jarFile = null;
			try {
				jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			String folderPath = jarFile.getParent() + File.separator;
			//--------------------------------------------------------------------------------------------------------
			
			File folder = new File(folderPath + "tentionlog");
	    	if( !folder.exists()) {
	    		if( !folder.mkdir() ) {
	    			System.out.println("tentionlogフォルダ作成失敗");
	    		}
	    	}
	    	writer = new CSVWriter(new FileWriter(folderPath+"tentionlog/"+StartDate + ">>>" + EndDate + ".csv"));

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

        	String[] subStr= new String[ 4*MainScreenController.ch_cnt ];
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
        	headStr = new String[8];
    		headStr[0]="ch1RatioValue";
    		headStr[1]="ch1MaxErrorValue";
    		headStr[2]="ch1MinErrorValue";
    		headStr[3]="ch2RatioValue";
    		headStr[4]="ch2MaxErrorValue";
    		headStr[5]="ch2MinErrorValue";
    		headStr[6]="ch1TareValue";
    		headStr[7]="ch2TareValue";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        subStr = new String[8];
	        subStr[0] = String.valueOf( settingMenu.ch1RatioValue );
	        subStr[1] = String.valueOf( settingMenu.ch1MaxErrorValue );
	        subStr[2] = String.valueOf( settingMenu.ch1MinErrorValue );
	        subStr[3] = String.valueOf( settingMenu.ch2RatioValue );
	        subStr[4] = String.valueOf( settingMenu.ch2MaxErrorValue );
	        subStr[5] = String.valueOf( settingMenu.ch2MinErrorValue );
	        subStr[6] = String.valueOf( settingMenu.ch1TareValue );
	        subStr[7] = String.valueOf( settingMenu.ch2TareValue );
        	writer.writeNext(subStr);

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
        	
        	
        	
        	//生データー
			writer.writeNext(new String[] { "" });
			writer.writeNext(new String[] { "[RawData]" });
        	headStr = new String[3];
    		headStr[0]="ElapsedTime(sec)";
    		headStr[1]="CH1 tention(g)";
    		headStr[2]="CH2 tention(g)";
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        int count = dataSet.getSeries(0).getItems().size();
	        subStr = new String[3];
	        for(int i=0;i<count;i++) {
	        	subStr[0] = (String.valueOf(
	        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(0).getItems().get(i)).getX()));
	        	subStr[1] = (String.valueOf(
	        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(0).getItems().get(i)).getYValue()));
	        	subStr[2] = (String.valueOf(
	        			((org.jfree.data.xy.XYDataItem)dataSet.getSeries(1).getItems().get(i)).getYValue()));
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
