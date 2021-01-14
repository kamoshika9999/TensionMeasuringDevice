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
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ProgressBarController{
	ScheduledExecutorService tr;
	static Stage stage;
	static Label CH1movingaverageLB,CH2movingaverageLB;
	static boolean resultFlg;

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private ProgressBar progress1;
    @FXML
    private Button OkBT;
    @FXML
    private Label ch1ResultTX;
    @FXML
    private Label ch2ResultTX;
    @FXML
    private Button CANCELBT;


    @FXML
    void onCancelBT(ActionEvent event) {
    	tr.shutdown();
    	resultFlg = false;
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void onOkBT(ActionEvent event) {
    	tr.shutdown();
    	resultFlg = true;
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void initialize() {
    	Platform.runLater(() ->progress1.setProgress(0.0f));

    	double startTime = System.currentTimeMillis();
    	double threthHold = 30*1000;//30秒


    	Runnable mesureTh = new Runnable() {
 	    	double nowTime;
 	    	boolean timeUpFlg = false;

 	   		@Override
 			public void run() {
				Platform.runLater(() ->stage.toFront());
				if( !timeUpFlg && nowTime - startTime > threthHold ) {
					//終了処理
					timeUpFlg = true;
					Platform.runLater(() ->OkBT.setDisable(false));
					Platform.runLater(() ->ch1ResultTX.setText( CH1movingaverageLB.getText() + " [g]"));
					Platform.runLater(() ->ch2ResultTX.setText( CH2movingaverageLB.getText() + " [g]"));
				}else {
					//プログレスバー更新
					if( !timeUpFlg ) {
						nowTime =  System.currentTimeMillis();
						if((nowTime - startTime)/1000/30 < 1) {
					    	Platform.runLater(() ->progress1.setProgress(
					    			(nowTime - startTime)/1000/30
					    			));
						}
					}
				}
			}
		};
		tr = Executors.newSingleThreadScheduledExecutor();
		tr.scheduleAtFixedRate(mesureTh, 1000, 500, TimeUnit.MILLISECONDS);

    }

}
