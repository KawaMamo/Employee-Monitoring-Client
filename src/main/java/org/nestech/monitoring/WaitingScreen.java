package org.nestech.monitoring;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

import java.io.IOException;

public class WaitingScreen {

    @FXML
    private ChoiceBox<String> passageCB;

    @FXML
    private void initialize(){
        passageCB.getItems().add("دخول");
        passageCB.getItems().add("خروج");
    }

    @FXML
    private void goToSetting(){
        HelloApplication main = new HelloApplication();
        try {
            main.changeScene("addFP.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
