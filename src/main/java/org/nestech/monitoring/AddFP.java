package org.nestech.monitoring;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class AddFP {

    @FXML
    private ListView<Employee> employeeListView;

    @FXML
    private void initialize(){

    }

    @FXML
    private void back() throws IOException {
        HelloApplication main = new HelloApplication();
        main.changeScene("waitingScreen.fxml");
    }

    @FXML
    private void load(){

        WebClient client = new WebClient("app.config");
        client.setEndPoint("api/employees");
        JSONArray employeeArray = (JSONArray) client.sendGetRequest().get("data");
        for (Object obj:employeeArray){
            JSONObject jObj = new JSONObject(obj.toString());
            employeeListView.getItems().add(new Employee(Integer.parseInt(jObj.get("id").toString()), jObj.getString("name")));
        }
    }

    @FXML
    private void addFP(){
        
    }
}
