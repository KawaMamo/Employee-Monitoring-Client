package org.nestech.monitoring;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddFP {

    public static Stage addFingerModal;
    @FXML
    private ListView<Employee> employeeListView;

    @FXML
    private ChoiceBox<Department> departmentCB;

    @FXML
    private TextField pageTF;

    public static String employeeName;
    public static int employeeId;


    @FXML
    private void initialize(){

        employeeListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Employee>() {
            @Override
            public void changed(ObservableValue<? extends Employee> observableValue, Employee employee, Employee t1) {
                employeeName = t1.getName();
                employeeId = t1.getId();
            }
        });

        pageTF.setText("1");
        populateChoiceBox();

    }

    @FXML
    private void back() throws IOException {
        HelloApplication main = new HelloApplication();
        main.changeScene("waitingScreen.fxml");
    }

    @FXML
    private void load(){
        populateList();
    }

    private void populateList(){

        employeeListView.getItems().clear();
        WebClient client = new WebClient("app.config");
        String getParameters = "?page="+pageTF.getText();
        if(departmentCB.getValue() != null){
            getParameters += "&filter[department.name]="+departmentCB.getValue().name;
        }
        client.setEndPoint("api/desktop/employees"+getParameters);
        JSONArray employeeArray = (JSONArray) client.sendGetRequest().get("data");
        for (Object obj:employeeArray){
            JSONObject jObj = new JSONObject(obj.toString());
            employeeListView.getItems().add(new Employee(Integer.parseInt(jObj.get("id").toString()), jObj.getString("name"), jObj.getString("department_name")));
        }
    }

    private void populateChoiceBox(){
        departmentCB.getItems().clear();
        WebClient client = new WebClient("app.config");
        client.setEndPoint("api/departments");
        final String string = client.sendGetRequestString();
        final JSONArray jsonArray = new JSONArray(new JSONObject(string).get("data").toString());
        jsonArray.getJSONObject(0).get("name");

        List<Department> departmentList = new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++){
            departmentList.add(new Department(jsonArray.getJSONObject(i).get("name").toString()));
        }

        ObservableList<Department> departments = FXCollections.observableList(departmentList);
        departmentCB.setItems(departments);


    }

    @FXML
    private void addFP() throws IOException {
        addFingerModal = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("addFingerModal.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 368, 276);
        addFingerModal.setTitle("Monitoring");
        addFingerModal.setScene(scene);
        addFingerModal.initModality(Modality.APPLICATION_MODAL);
        addFingerModal.initStyle(StageStyle.UNDECORATED);
        addFingerModal.show();
    }

    @FXML
    private void next(){
        pageTF.setText(String.valueOf(Integer.parseInt(pageTF.getText())+1));
        populateList();
    }

    @FXML
    private void previous(){
        pageTF.setText(String.valueOf(Integer.parseInt(pageTF.getText())-1));
        populateList();
    }
}
