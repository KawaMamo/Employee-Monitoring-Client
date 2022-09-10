package org.nestech.monitoring;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class AddFP {

    @FXML
    private ListView<Employee> employeeListView;

    public static String employeeName;

    @FXML
    private void initialize(){

        employeeListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Employee>() {
            @Override
            public void changed(ObservableValue<? extends Employee> observableValue, Employee employee, Employee t1) {
                employeeName = t1.getName();
            }
        });
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
    private void addFP() throws IOException {
        Stage addFingerModal = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("addFingerModal.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 368, 276);
        addFingerModal.setTitle("Monitoring");
        addFingerModal.setScene(scene);
        addFingerModal.initModality(Modality.APPLICATION_MODAL);
        addFingerModal.show();
    }
}
