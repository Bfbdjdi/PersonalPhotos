package com.mirea.kt.ribo.oao_salty;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class ServerAuthProcess extends MainActivity implements Runnable {
    private String userLogin;
    private String userPassword;
    private BlockingQueue<String> blockedQueue;

    public ServerAuthProcess(String userLogin, String userPassword, BlockingQueue<String> blockedQueue) {
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        this.blockedQueue = blockedQueue;
    }

    private String generateStringBody(HashMap<String, String> userCredentialsMap) {
        StringBuilder sbParams = new StringBuilder();

        int i = 0;

        for (String key : userCredentialsMap.keySet()) {
            try {
                if (i != 0) sbParams.append("&");
                sbParams.append(key).append("=").append(URLEncoder.encode(userCredentialsMap.get(key), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            i++;
        }
        return sbParams.toString();
    }

    @Override
    public void run() {

        String serverAddress = "https://android-for-students.ru/coursework/login.php";

        HashMap<String, String> userCredentialsMap = new HashMap();
        userCredentialsMap.put("lgn", userLogin);
        userCredentialsMap.put("pwd", userPassword);
        userCredentialsMap.put("g", "RIBO-01-22");

        String responseBody = "";

        try {
            URL url = new URL(serverAddress);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            OutputStreamWriter osw = new OutputStreamWriter(httpConnection.getOutputStream());
            osw.write(generateStringBody(userCredentialsMap));
            osw.flush();
            int responseCode = httpConnection.getResponseCode();

            if (responseCode != -1) {
                InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String currentLine;
                StringBuilder sbResponse = new StringBuilder();

                while ((currentLine = br.readLine()) != null) sbResponse.append(currentLine);

                responseBody = sbResponse.toString();
                Log.d("rec_tag", responseBody);

                JSONObject jSONObject = new JSONObject(responseBody);
                int result = jSONObject.getInt("result_code");

                switch (result) {
                    case 1:
                        blockedQueue.add("allowed");
                        break;
                    case -1:
                        blockedQueue.add("not allowed");
                        break;
                    default:
                        //log.d
                        break;
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

