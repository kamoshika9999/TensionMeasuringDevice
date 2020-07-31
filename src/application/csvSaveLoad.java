package application;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
			writer = new CSVWriter(new FileWriter("./init.csv"));
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
		try {
			FileReader fr = new FileReader("./init.csv");
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
		/*
	    public static double ch1RatioValue;
	    public static long ch1MaxErrorValue;
	    public static long ch1MinErrorValue;
	    public static double ch2RatioValue;
	    public static long ch2MaxErrorValue;
	    public static long ch2MinErrorValue;
	    */

		String[] headStr = new String[6];
		headStr[0]="ch1RatioValue";
		headStr[1]="ch1MaxErrorValue";
		headStr[2]="ch1MinErrorValue";
		headStr[3]="ch2RatioValue";
		headStr[4]="ch2MaxErrorValue";
		headStr[5]="ch2MinErrorValue";

		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter("./init2.csv"));
	        writer.writeNext(headStr);  //ヘッダー書き込み

	        String[] subStr = new String[6];

	        subStr[0] = String.valueOf( settingMenu.ch1RatioValue );
	        subStr[1] = String.valueOf( settingMenu.ch1MaxErrorValue );
	        subStr[2] = String.valueOf( settingMenu.ch1MinErrorValue );
	        subStr[3] = String.valueOf( settingMenu.ch2RatioValue );
	        subStr[4] = String.valueOf( settingMenu.ch2MaxErrorValue );
	        subStr[5] = String.valueOf( settingMenu.ch2MinErrorValue );

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
	    public static double ch1RatioValue;
	    public static long ch1MaxErrorValue;
	    public static long ch1MinErrorValue;
	    public static double ch2RatioValue;
	    public static long ch2MaxErrorValue;
	    public static long ch2MinErrorValue;
	    */
		try {
			FileReader fr = new FileReader("./init2.csv");
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


		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
