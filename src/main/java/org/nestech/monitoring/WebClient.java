package org.nestech.monitoring;

import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class WebClient {

    private static String token = "dummyToken";

    public static void setToken(String token) {
        WebClient.token = token;
    }

    private String endPoint;
    private Map<String, String> postParameters;
    HttpClient client = HttpClient.newHttpClient();
    private int requestTries = 0;

    Config config;

    public WebClient(String fileName){

        try {
            config = new Config(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setPostParameters(Map<String, String> postParameters) {
        this.postParameters = postParameters;
    }

    public static String getToken() {
        return token;
    }

    public JSONObject sendPostRequest(){
        JSONObject jsonObject = null;
        postParameters.put("token", token);
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(config.getProp().getProperty("domain")+endPoint))
                    .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(postParameters)))
                    .header("Content-Type",  "application/x-www-form-urlencoded")
                    .header("Authorization", "Bearer "+token)
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println(response.body().toString());
        jsonObject = new JSONObject(response.body().toString());

        return jsonObject;

    }

    private static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }

    public JSONObject sendGetRequest(){

        JSONObject jsonObject = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(config.getProp().getProperty("domain")+endPoint))
                    .GET()
                    .header("Authorization", "Bearer "+token)
                    .build();

            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println(response.body().toString());
            jsonObject = new JSONObject(response.body().toString());


        } catch (URISyntaxException e) {
            e.printStackTrace();
            // TODO: do log4j
        } catch (ConnectException e){
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            // do log4j
        } catch (InterruptedException e) {
            e.printStackTrace();
            // do log4j
        }

        return jsonObject;
    }
}
