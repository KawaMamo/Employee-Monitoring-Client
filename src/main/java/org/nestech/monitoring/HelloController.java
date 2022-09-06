package org.nestech.monitoring;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.controlsfx.control.Notifications;
import org.json.JSONArray;
import org.json.JSONObject;

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

        client.setEndPoint("api/employees");

        //System.out.println(client.sendGetRequest().get("data").toString());
        JSONArray employeeArray = (JSONArray) client.sendGetRequest().get("data");
        for (Object obj:employeeArray){
            JSONObject jObj = new JSONObject(obj.toString());
            //System.out.println(jObj);
            System.out.println(jObj.getString("name")+jObj.get("id"));
        }
    }
}