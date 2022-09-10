package org.nestech.monitoring;

import com.zkteco.biometric.FingerprintSensorEx;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import com.zkteco.biometric.FingerprintSensorErrorCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;


public class AddFingerModal {

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

    @FXML
    private Label employeeNameLbl;

    @FXML
    private Label informationLbl;

    @FXML
    private ImageView fingerprintImg;

    private MySQLAccess mySQLAccess = new MySQLAccess();
    private WorkThread workThread = null;

    @FXML
    private void initialize(){
        employeeNameLbl.setText(AddFP.employeeName);
        String infoToShow = null;
        try {
            infoToShow = btnOpen();
            informationLbl.setText(infoToShow);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("couldn't start the device");
        }

        infoToShow =  btnEnroll();
        System.out.println(infoToShow);
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
        //For ISO/Ansi End

        //set fakefun off
        //FingerprintSensorEx.SetParameter(mhDevice, 2002, changeByte(nFakeFunOn), 4);

        byte[] paramValue = new byte[4];
        int[] size = new int[1];
        //GetFakeOn
        //size[0] = 4;
        //FingerprintSensorEx.GetParameters(mhDevice, 2002, paramValue, size);
        //nFakeFunOn = byteArrayToInt(paramValue);

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
            //mySQLAccess.getTemplate(mhDB);
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

    private void OnCatpureOK(byte[] imgBuf)
    {
        try {
            Utilities.writeBitmap(imgBuf, fpWidth, fpHeight, "fingerprint.bmp");
            String path = "fingerprint.bmp";
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Image image = new Image(new File("fingerprint.bmp").toURI().toString());
                    fingerprintImg.setImage(image);
                }
            });

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
                        //String fullName = nameField.getText();


                        int check = 0;
                        //int shift_id = mySQLAccess.getShiftIdByName(choiceBox.getText());
                        //mySQLAccess.insertBlob(regTemp, iFid, fullName, father, mother, nationalId, birthPlace, shift_id, branch, fNumber, sNumber, salary_catCB.getValue().toString());

                        //System.out.print("inserted: "+ name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //iFid = 1+ mySQLAccess.getMaxId();
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
                            try {

                                LocalDate mydate = LocalDate.parse("");
                                LocalDate defaultDate = LocalDate.now();
                                if(mydate == null){
                                    // payDate.setValue(defaultDate);
                                    mydate = defaultDate;
                                }
                                String month = mydate.getMonth().toString();
                                int yaer = mydate.getYear();
                                String dateString = month+"-"+yaer;

                                /*String name = mySQLAccess.getData(fid[0]);
                                String nationalID = mySQLAccess.getNation(fid[0]);
                                String salary = mySQLAccess.getSalary(fid[0]);
                                String food = mySQLAccess.getFood(fid[0]);
                                String smoke = mySQLAccess.getSmoke(fid[0]);
                                String extra = mySQLAccess.getExtra(fid[0]);
                                String job = mySQLAccess.getJob(fid[0]);
                                String center = mySQLAccess.getCenter(fid[0]);
                                String date = mySQLAccess.getDate(fid[0]);
                                String str[] = date.split("-");*/

                                //int year = Integer.parseInt(str[0]);
                                //if(year <2014){year=2014;}

                                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                payId = fid[0];
                                //int MoneyOfYears = (currentYear - year)*1500;

                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        }
                    });

                }
                else
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
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

    public String btnEnroll(){

        if(0 == mhDevice)
        {
            System.out.print("Please Open device first!\n");
            info = "الرجاء تشغيل الجهاز أولا";
            return info;
        }
        if(!bRegister)
        {
            enroll_idx = 0;
            bRegister = true;
            System.out.print("Please your finger 3 times!\n");
            info = "الرجار وضع الإصبع على الجهاز ثلاث مرات";
        }
        return info;
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
                    OnCatpureOK(imgbuf);
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

}
