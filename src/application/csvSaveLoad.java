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
}
