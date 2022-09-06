package org.nestech.monitoring;

import java.util.Base64;

public class LoginParameter {

    private static String userName;
    private static String password;
    private static String basicAuthentication;

    public LoginParameter(String userName, String password){
        this.userName = userName;
        this.password = password;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

    public static String getBasicAuthentication() {
        return basicAuthentication;
    }

    public static void generateBasicAuthentication() {
        LoginParameter.basicAuthentication = "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
    }

    public static void setUserName(String userName) {
        LoginParameter.userName = userName;
    }

    public static void setPassword(String password) {
        LoginParameter.password = password;
    }
}
