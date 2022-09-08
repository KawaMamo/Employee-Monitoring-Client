package org.nestech.monitoring;

import com.zkteco.biometric.FingerprintSensorEx;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import com.zkteco.biometric.FingerprintSensorErrorCode;
import javafx.scene.image.Image;
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

    private MySQLAccess mySQLAccess = new MySQLAccess();
    private WorkThread workThread = null;

    @FXML
    private void initialize(){
        String infoToShow = null;
        try {
            infoToShow = btnOpen();
            informationLbl.setText(infoToShow);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("couldn't start the device");
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

        /*if(zK7500[0].equals("ZK7500") && (sNumber.equals("{BE751357-49D7-411B-B06E-EFBE1B2C1D19") || sNumber.equals("{574721DE-2DEF-4BAE-8509-27C78A359F1E") || sNumber.equals("{7C8759D1-D4EE-44ED-B490-2CE546CCD95C") || sNumber.equals("{E3548164-4A9D-46CA-BDCD-D1623688FECB") || sNumber.equals("{E7E55CF1-2437-45CD-8788-57529A0B9B64") || sNumber.equals("{8E697CFD-993B-404A-9AB1-4B75E915A3F5") || sNumber.equals("{0B967262-CBDB-4309-AE2D-082A6ACAE66B") || sNumber.equals("{C3F7EB3B-E1E8-4BF8-AFCC-4707950E69D2") || sNumber.equals("{127AB279-269F-4F82-9AB3-2597DF185AC6") || sNumber.equals("{ABBE8022-BF1C-4A05-AEE5-6E445F2FC720") || sNumber.equals("{759090F3-0826-4559-BA42-7CCF91FA5147") || sNumber.equals("{87B3F3F5-5F0B-4493-90B6-11A333E45AA8") || sNumber.equals("{891A763A-A31B-4B02-8004-71BABA4C5210") || sNumber.equals("{260133A9-A858-4F44-AD08-6A90A244258A") || sNumber.equals("{3AC02821-52C1-4664-928E-6714778A4D7C") || sNumber.equals("{15B8E319-3B3D-47DE-8131-06F36A84A16C") || sNumber.equals("{7C723BDD-2672-44A3-9A01-15BAB371DFB7") || sNumber.equals("{2F10B240-F853-4C07-BF38-AACEEE05483D") || sNumber.equals("{FBEEEF36-90FB-4515-A0B8-BF2B607BEAB9") || sNumber.equals("{1AA271A9-5FD3-4EE5-B429-F8CF5C600B91") || sNumber.equals("{141996B8-B4E1-4ED7-A904-4B598338D650") || sNumber.equals("{DA1449C2-B721-4FB8-923C-F216586C0DEE") || sNumber.equals("{76E4CF84-96AC-46A3-92D3-6AF590A02A17") || sNumber.equals("{B5ABA1C1-8BF2-4CD0-B6D5-40543724FAB7") || sNumber.equals("{5D7B98B9-5CB3-4D10-9B07-9986EA79EA10") || sNumber.equals("{263795B7-387D-4814-B798-24F215EAE03C") || sNumber.equals("{79740D9A-5697-4936-95DE-169BF8570536") || sNumber.equals("{26F9A870-7F39-4C72-AECE-31B66A0C86B4") || sNumber.equals("{D4470C6D-D818-4F1B-A35E-23BFFED41345") || sNumber.equals("{42B57DFD-6B67-4152-91CA-DD909014E34F") || sNumber.equals("{44CE31BF-BF18-4102-B0FC-D70F740FB4BF") || sNumber.equals("{95777F7C-AE0A-403B-A6A5-2806074EB395") || sNumber.equals("{58BA8F44-ECE7-4FEE-8F6D-97F49F6E3A44") || sNumber.equals("{B8D89940-5649-4802-8B90-F254009E710E") || sNumber.equals("{58336B1E-810F-41CF-9135-ECBAF2F2663F") || sNumber.equals("{BC8ED60B-9BCC-4BE7-94E0-13798264E527") || sNumber.equals("{213FAFA8-680F-427E-9323-83A01EC66AFB") || sNumber.equals("{EBAF2B13-7265-4D46-87CF-43A212CB036A") || sNumber.equals("{7632F731-3F3F-4702-B3AE-9B54AA4E226F") || sNumber.equals("{B6B0456B-326E-46BC-8030-202D7A717363") || sNumber.equals("{7AF58914-135B-4DCC-BDF6-8C07C73C72CA") || sNumber.equals("{5558F1CE-3DBA-4092-861C-2CD1F4B1DEEC") || sNumber.equals("{D47FB9C0-BAA9-4D43-826E-BF702F7A87F5") || sNumber.equals("{9CD4AB0C-B23E-427F-9DD9-A0CF1092C3E5") || sNumber.equals("{70AD0B17-DEEB-4944-8B9F-6C9B04C8A201") || sNumber.equals("{EF1405DB-D771-49FE-AA04-596A380396F5") || sNumber.equals("{4E638D0C-E04E-41F9-ABF7-CADB31F4E499") || sNumber.equals("{D1F4F0B0-150E-47C5-A3B2-4A30A2157B70") || sNumber.equals("{8A13BCE5-0E70-44C6-BB97-44C3719FC18A") || sNumber.equals("{F2030F2B-A37E-4952-84A0-841F1EE9E4BE") || sNumber.equals("{C06A4AE0-E8B4-4556-A934-C0094C1E55B4") || sNumber.equals("{78A310A0-A59B-4774-9637-6D55ED1C96F0") || sNumber.equals("{C05FDC96-BCEE-453C-B0E4-068D5FB2EAA6") || sNumber.equals("{C60E62EB-54F2-4467-ACAD-8861D9DDDF17") || sNumber.equals("{0D8035FF-B967-4534-B910-0DEEABD26136") || sNumber.equals("{66957712-6305-45BF-A6BC-A2068D820E87") || sNumber.equals("{AB41BACE-7D1A-4476-87CE-EA7730506BE5") || sNumber.equals("{248D6341-6036-4568-89FE-7781DBAFB468") || sNumber.equals("{F5EFF54D-978F-498F-B65C-9DF5B4A09D3F") || sNumber.equals("{6917C530-95FC-4412-AC30-A922041CEA30") || sNumber.equals("{1C4B2A06-57F3-4AD1-82B3-7297F25F8AEB") || sNumber.equals("{CE8F7BBB-C776-48A2-8DE6-AA4B8C0B17F0") || sNumber.equals("{4F822916-1915-4F38-8F87-0EFB1087F09E") || sNumber.equals("{18525037-00F2-493C-B822-195CB48F2870")  || sNumber.equals("{C27C4D51-E61A-4F12-9DFF-430F14D7BE93") || sNumber.equals("{FB9A580D-B71A-498D-A443-6560E3B41D2E") || sNumber.equals("{1EB12C5A-FA23-4A47-8C8A-A1C277E7064B") || sNumber.equals("{EAC79769-7C4C-44F1-8F31-03CE7AF0C181") || sNumber.equals("{36638790-A386-4F57-8B13-8E95456B3867") || sNumber.equals("{9D8183F6-3A3F-4B72-9215-97D76CE426EB") || sNumber.equals("{2E566A5C-E0E1-4A5A-A40B-00682636BFC9"))){

        }else{
            mhDevice = 0;
            System.out.print("Not Supported");
            info = "الجهاز غير مدعوم راجع شركة Solutions";
        }*/

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
                    //capturedImg.setImage(image);
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
                    //Base64 Template
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            System.out.print("enroll  success:\n");
                            HelloApplication main = new HelloApplication();
                            try {
                                btnClose();
                                main.changeScene("sample.fxml");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
