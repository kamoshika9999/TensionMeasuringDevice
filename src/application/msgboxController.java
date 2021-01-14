package application;


import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Window;

public class msgboxController {
	static String msg;//表示するメッセージ

	@FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label infoLB;

    @FXML
    private Button oKBT;

    @FXML
    void onOkBT(ActionEvent event) {
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void initialize() {
    	Platform.runLater(() ->infoLB.setText(msg));
    	Platform.runLater(() ->infoLB.setAlignment(Pos.CENTER));
    }
}
