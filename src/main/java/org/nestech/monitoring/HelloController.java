package org.nestech.monitoring;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.controlsfx.control.Notifications;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HelloController {
    @FXML
    private PasswordField passPF;

    @FXML
    private TextField userNameTF;

    @FXML
    void submit(ActionEvent event) {
        WebClient client = new WebClient("app.config");
        client.setEndPoint("api/login");

        Map<String, String> postParameters = new HashMap<>();
        postParameters.put("email", userNameTF.getText());
        postParameters.put("password", passPF.getText());
        client.setPostParameters(postParameters);
        JSONObject dataObj = client.sendPostRequest();
        Notifications.create().title("Message").text(dataObj.getString("message")).showInformation();
        JSONObject userObj = new JSONObject(dataObj.get("data").toString());
        WebClient.setToken(userObj.get("token").toString());

        HelloApplication main = new HelloApplication();
        try {
            main.changeScene("waitingScreen.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.setEndPoint("api/period-times");
        JSONObject periodTimes = client.sendGetRequest();
        JSONArray data = new JSONArray(periodTimes.get("data").toString());
        for (Object obj: data){
            if(((JSONObject) obj).get("type").toString().equals("2")){
                PeriodTimes.preTolerance = String.valueOf(((JSONObject) obj).get("value"));
            }else if(((JSONObject) obj).get("type").toString().equals("1")){
                PeriodTimes.pastTolerance = String.valueOf(((JSONObject) obj).get("value"));
            }

        }

    }
}