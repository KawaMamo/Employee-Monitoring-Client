package org.nestech.monitoring;


import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.Notifications;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nestech.monitoring.model.EmployeeDetails;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WaitingScreen {

    //the width of fingerprint image
    int fpWidth = 0;
    //the height of fingerprint image
    int fpHeight = 0;
    //for verify test
    private byte[] lastRegTemp = new byte[2048];
    //the length of lastRegTemp
    private int cbRegTemp = 0;
    //pre-register template
    private byte[][] regtemparray = new byte[3][2048];
    //Register
    private boolean bRegister = false;
    //Identify
    private boolean bIdentify = true;
    //finger id
    private int iFid = 1;

    private int nFakeFunOn = 1;
    //must be 3
    static final int enroll_cnt = 3;
    //the index of pre-register function
    private int enroll_idx = 0;
    private byte[] imgbuf = null;
    private byte[] template = new byte[2048];
    private int[] templateLen = new int[1];
    private boolean mbStop = true;
    private long mhDevice = 0;
    private long mhDB = 0;

    public int payId;
    public String info;

    private  String fNumber = "";
    private String sNumber = "";

    private final MySQLAccess mySQLAccess = new MySQLAccess();
    private WorkThread workThread = null;

    @FXML
    private ChoiceBox<String> passageCB;

    @FXML
    private Label informationLbl;

    @FXML
    private TextField codeTF;

    String name;

    @FXML
    private void initialize(){

        passageCB.getItems().add("دخول");
        passageCB.getItems().add("خروج");

        try {
            btnClose();
            btnOpen();
            btnIdentify();
        }catch (Exception e){

        }

    }

    private void btnIdentify(){
        if(0 == mhDevice)
        {
            informationLbl.setText("يرجى التحقق من توصيل الجهاز");
            return;
        }
        if(bRegister)
        {
            enroll_idx = 0;
            bRegister = false;
        }
        if(!bIdentify)
        {
            bIdentify = true;
        }
    }

    public String btnOpen() throws Exception {

        // TODO Auto-generated method stub
        if (0 != mhDevice)
        {
            //already inited
            System.out.print("Please close device first!\n");
            info = "الرجاء إغلاق الجهاز أولا";
            return info;
        }
        int ret = FingerprintSensorErrorCode.ZKFP_ERR_OK;
        //Initialize
        cbRegTemp = 0;
        bRegister = false;
        bIdentify = false;
        //iFid = 1+mySQLAccess.getMaxId();
        iFid = 1;

        enroll_idx = 0;
        if (FingerprintSensorErrorCode.ZKFP_ERR_OK != FingerprintSensorEx.Init())
        {
            System.out.print("Init failed!\n");
            info = "فشل تشغيل الجهاز تأكد من أنه متصل بشكل صحيح";
            return info;
        }
        ret = FingerprintSensorEx.GetDeviceCount();
        if (ret < 0)
        {
            System.out.print("No devices connected!\n");
            info = "لا توجد أجهزة متصلة";
            FreeSensor();
            return info;
        }
        if (0 == (mhDevice = FingerprintSensorEx.OpenDevice(0)))
        {
            System.out.print("Open device fail, ret = " + ret + "!\n");
            info = "فش تشغيل الجهاز - رمز الخطأ" + ret;
            FreeSensor();
            return info;
        }
        if (0 == (mhDB = FingerprintSensorEx.DBInit()))
        {
            System.out.print("Init DB fail, ret = " + ret + "!\n");
            info = "فشل في تشغيل قاعدة البيانات رمز الخطأ" + ret;
            FreeSensor();
            return info;
        }

        //For ISO/Ansi
        int nFmt = 0;	//Ansi

        FingerprintSensorEx.DBSetParameter(mhDB,  5010, nFmt);

        byte[] paramValue = new byte[4];
        int[] size = new int[1];

        size[0] = 4;
        FingerprintSensorEx.GetParameters(mhDevice, 1, paramValue, size);
        fpWidth = Utilities.byteArrayToInt(paramValue);
        size[0] = 4;
        FingerprintSensorEx.GetParameters(mhDevice, 2, paramValue, size);
        fpHeight = Utilities.byteArrayToInt(paramValue);

        byte[] paramValue2 = new byte[32];
        int[] size2 = new int[32];

        size2[0] = 32;
        int resulto = FingerprintSensorEx.GetParameters(mhDevice, 1102, paramValue2, size2);
        String device = new String(paramValue2);


        byte[] paramValue3 = new byte[64];
        int[] size3 = new int[64];
        size3[0] = 64;
        int resulto3 = FingerprintSensorEx.GetParameters(mhDevice, 1103, paramValue3, size3);
        String device3 = new String(paramValue3);



        imgbuf = new byte[fpWidth*fpHeight];
        //btnImg.resize(fpWidth, fpHeight);



        mbStop = false;
        workThread = new WorkThread();
        workThread.setDaemon(true);
        workThread.start();// 线程启动
        System.out.print("Open succ! Finger Image");
        info = "تم تشغيل الجهاز بنجاح";
        informationLbl.setText(info);
        String zK7500 [] = device.split("\\s+");
        String sN [] = device3.split("}");

        fNumber = zK7500[0];
        sNumber = sN[0];


        try {
            mySQLAccess.getTemplate(mhDB);
            FingerprintSensorEx.DBCount(mhDB);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    private void FreeSensor()
    {
        mbStop = true;
        try {		//wait for thread stopping
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (0 != mhDB)
        {
            FingerprintSensorEx.DBFree(mhDB);
            mhDB = 0;
        }
        if (0 != mhDevice)
        {
            FingerprintSensorEx.CloseDevice(mhDevice);
            mhDevice = 0;
        }
        FingerprintSensorEx.Terminate();
    }

    private void OnExtractOK(byte[] template, int len) throws Exception {
        if(bRegister)
        {
            int[] fid = new int[1];
            int[] score = new int [1];
            int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);


            if (ret == 0)
            {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.print("the finger already enroll by " + fid[0] + ",cancel enroll\n");
                        informationLbl.setText("تم تعريف البصمة مسبقا وهي ذات المعرف رقم"+fid[0]);
                    }
                });

                bRegister = false;
                enroll_idx = 0;
                return;
            }
            if (enroll_idx > 0 && FingerprintSensorEx.DBMatch(mhDB, regtemparray[enroll_idx-1], template) <= 0)
            {
                System.out.print("please press the same finger 3 times for the enrollment\n");
                informationLbl.setText("الرجاء وضع الإصبع ذاتها ثلاث مرات على الجهاز اللتسجيل");
                return;
            }
            System.arraycopy(template, 0, regtemparray[enroll_idx], 0, 2048);
            enroll_idx++;
            if (enroll_idx == 3) {
                int[] _retLen = new int[1];
                _retLen[0] = 2048;
                byte[] regTemp = new byte[_retLen[0]];

                if (0 == (ret = FingerprintSensorEx.DBMerge(mhDB, regtemparray[0], regtemparray[1], regtemparray[2], regTemp, _retLen)) &&
                        0 == (ret = FingerprintSensorEx.DBAdd(mhDB, iFid, regTemp))) {

                    try {

                        mySQLAccess.insertBlob(regTemp, AddFP.employeeId, AddFP.employeeName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    iFid = 1+ mySQLAccess.getMaxId();
                    cbRegTemp = _retLen[0];
                    System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
                    btnClose();
                    //Base64 Template
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            informationLbl.setText("تم التسجيل بنجاح");
                        }
                    });

                } else {
                    System.out.print("enroll fail, error code=" + ret + "\n");
                }
                bRegister = false;
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.print("You need to press the " + (3 - enroll_idx) + " times fingerprint\n");
                        informationLbl.setText("عليك أن تضع إصبعك "+(3 - enroll_idx)+" مرة للتسجيل");
                    }
                });

            }
        }
        else
        {
            if (bIdentify)
            {
                int[] fid = new int[1];
                int[] score = new int [1];
                int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
                if (ret == 0)
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            System.out.print("Identify succ, fid=" + fid[0] + ",score=" + score[0] +"\n");
                            informationLbl.setText("نجح التمييز رقم المعرف "+ fid[0] +" بنسبة "+ score[0]);
                            if (passageCB.getValue() == null){
                                informationLbl.setText("يرجى تحديد نوع المرور");
                            }else {
                                try {


                                    name = mySQLAccess.getData(fid[0]);
                                    int id = mySQLAccess.getId(fid[0]);
                                    informationLbl.setText(name);

                                    WebClient webClient = new WebClient("app.config");
                                    webClient.setEndPoint("api/desktop/employees/"+id);
                                    JSONObject employeeDetails = webClient.sendGetRequest();
                                    if(employeeDetails.getJSONObject("data").get("active_system").toString() != "null"){
                                        JSONObject activeSystem = employeeDetails.getJSONObject("data").getJSONObject("active_system");
                                        LocalTime startTime = null;
                                        LocalTime endTime = null;
                                        try {
                                            startTime = LocalTime.parse(activeSystem.getString("start_time"));
                                            endTime = LocalTime.parse(activeSystem.getString("end_time"));
                                        }catch (JSONException ignored){

                                            ObjectMapper objectMapper = new ObjectMapper();
                                            final EmployeeDetails employeeData = objectMapper.readValue(employeeDetails.toString(), EmployeeDetails.class);
                                            System.out.println(employeeData.getData().getActive_system().working_information.get(0).getStart_from());
                                            startTime = LocalTime.parse(employeeData.getData().getActive_system().working_information.get(0).getStart_from());
                                            endTime = LocalTime.parse(employeeData.getData().getActive_system().working_information.get(0).getStart_from())
                                                    .plusHours(employeeData.getData().getActive_system().working_information.get(0).working_hours);
                                        }

                                        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

                                        if(Objects.nonNull(startTime) && Integer.parseInt(String.valueOf(Duration.between(startTime, LocalTime.now()).toMinutes())) < Integer.parseInt(PeriodTimes.preTolerance) ) {
                                            timeStamp = LocalDate.now()+" "+startTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
                                        }

                                        if(Objects.nonNull(endTime) && Integer.parseInt(String.valueOf(Duration.between(LocalTime.now(),endTime).toMinutes())) < Integer.parseInt(PeriodTimes.pastTolerance) ) {
                                            timeStamp = LocalDate.now()+" "+endTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
                                        }

                                        webClient.setEndPoint("api/desktop/employee/check-in");
                                        Map<String, String> postParameters = new HashMap<>();
                                        postParameters.put("date", timeStamp);
                                        postParameters.put("employee_id", String.valueOf(id));
                                        int type = (passageCB.getValue().equals("دخول")) ? 1:0;

                                        postParameters.put("type", String.valueOf(type));

                                        webClient.setPostParameters(postParameters);
                                        JSONObject checkInObj = webClient.sendPostRequest();
                                        if(checkInObj.get("data") != JSONObject.NULL && checkInObj.get("success").equals(true)){
                                            Notifications.create().title(passageCB.getValue()).text(name).showInformation();
                                        }else {
                                            Notifications.create().title("Error").text("إجراء غير صحيح").showError();
                                        }
                                    }else {
                                        Notifications.create().text("لم يتم تحديد نظام دوام لهذا الموظف").title("Error").showError();
                                    }

                                    payId = fid[0];

                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                            }


                        }
                    });

                }
                else
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            informationLbl.setText("فشل التعرف");
                            System.out.print("Identify fail, errcode=" + ret + "\n");
                        }
                    });

                }

            }
            else
            {
                if(cbRegTemp <= 0)
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            System.out.print("Please register first!\n");
                        }
                    });

                }
                else
                {
                    int ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp, template);
                    if(ret > 0)
                    {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                System.out.print("Verify succ, score=" + ret + "\n");
                            }
                        });

                    }
                    else
                    {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                System.out.print("Verify fail, ret=" + ret + "\n");
                            }
                        });

                    }
                }
            }
        }

    }

    public void btnClose(){

        // TODO Auto-generated method stub
        FreeSensor();

        System.out.print("Close succ!\n");


    }

    class WorkThread extends Thread{

        @Override
        public void run() {

            super.run();

            int ret = 0;
            while (!mbStop) {
                templateLen[0] = 2048;
                if (0 == (ret = FingerprintSensorEx.AcquireFingerprint(mhDevice, imgbuf, template, templateLen)))
                {
                    if (nFakeFunOn == 1)
                    {
                        byte[] paramValue = new byte[4];
                        int[] size = new int[1];
                        size[0] = 4;
                        int nFakeStatus = 0;
                        //GetFakeStatus
                        ret = FingerprintSensorEx.GetParameters(mhDevice, 2004, paramValue, size);
                        nFakeStatus = Utilities.byteArrayToInt(paramValue);
                        System.out.println("ret = "+ ret +",nFakeStatus=" + nFakeStatus);
                        if (0 == ret && (byte)(nFakeStatus & 31) != 31)
                        {
                            System.out.print("Is a fake finger?\n");
                            return;
                        }
                    }
                    try {
                        OnExtractOK(template, templateLen[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @FXML
    private void goToSetting(){
        btnClose();
        HelloApplication main = new HelloApplication();
        try {
            main.changeScene("addFP.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void checkByCode(){

        if (passageCB.getValue() == null){
            informationLbl.setText("يرجى تحديد نوع المرور");
        }else {
            WebClient client = new WebClient("app.config");
            client.setEndPoint("api/getEmployeeByCode/"+codeTF.getText());
            JSONObject employee = new JSONObject(client.sendGetRequest().toString());
            int id = 0;
            try {

                id = (int) employee.get("id");
                name = (String) employee.get("name");

                client.setEndPoint("api/desktop/employees/"+id);
                JSONObject employeeDetails = client.sendGetRequest();

                if(employeeDetails.getJSONObject("data").get("active_system").toString() != "null"){

                    JSONObject activeSystem = employeeDetails.getJSONObject("data").getJSONObject("active_system");
                    LocalTime startTime = LocalTime.parse(activeSystem.getString("start_time"));
                    LocalTime endTime = LocalTime.parse(activeSystem.getString("end_time"));

                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

                    if(Integer.parseInt(String.valueOf(Duration.between(startTime, LocalTime.now()).toMinutes()))
                            < Integer.parseInt(PeriodTimes.preTolerance) ) {
                        timeStamp = LocalDate.now()+" "+startTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
                    }

                    if(Integer.parseInt(String.valueOf(Duration.between(LocalTime.now(),endTime).toMinutes())) < Integer.parseInt(PeriodTimes.pastTolerance) ) {
                        timeStamp = LocalDate.now()+" "+endTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
                    }

                    Map<String, String> postParameters = new HashMap<>();
                    postParameters.put("date", timeStamp);
                    postParameters.put("employee_id", String.valueOf(id));
                    int type = (passageCB.getValue().equals("دخول")) ? 1:0;
                    System.out.println("type"+type);
                    postParameters.put("type", String.valueOf(type));
                    client.setEndPoint("api/desktop/employee/check-in");

                    client.setPostParameters(postParameters);
                    JSONObject checkInObj = client.sendPostRequest();
                    if(checkInObj.get("data") != JSONObject.NULL && checkInObj.get("success").equals(true)){
                        Notifications.create().title(passageCB.getValue()).text(name).showInformation();
                    }else {
                        Notifications.create().title("Error").text("إجراء غير صحيح").showError();
                    }
                }else {
                    Notifications.create().text("لم يتم تحديد نظام دوام لهذا الموظف").title("Error").showError();
                }

            }catch (JSONException e){
                Notifications.create().title("Error").text("الكود غير صحيح").showError();
            }

        }

    }
}
