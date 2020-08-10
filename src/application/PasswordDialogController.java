package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Window;

public class PasswordDialogController {
	public static boolean flg = false;

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private PasswordField pass_text;
    @FXML
    private Button OK_btn;

    @FXML
    void onOKbtn(ActionEvent event) {
    	if(pass_text.getText().matches("4321")) {
    		flg = true;
    	}else {
    		flg = false;
    	}
		Scene scene = ((Node) event.getSource()).getScene();
		Window window = scene.getWindow();
		window.hide();
    }

    @FXML
    void initialize() {
    }
}
