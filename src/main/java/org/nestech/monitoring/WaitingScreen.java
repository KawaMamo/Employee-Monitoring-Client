package org.nestech.monitoring;

import javafx.fxml.FXML;

import java.io.IOException;

public class WaitingScreen {

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
