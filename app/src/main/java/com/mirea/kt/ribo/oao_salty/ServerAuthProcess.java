package com.mirea.kt.ribo.oao_salty;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

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
    private Context contexter;

    public ServerAuthProcess(String userLogin, String userPassword, BlockingQueue<String> blockedQueue, Context contexter) {
        this.contexter = contexter;
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

    // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
    /*public boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) { return false; }
    }*/

    @Override
    public void run() {

        //POSTing
        String serverAddress = "https://android-for-students.ru/coursework/login.php";

        HashMap<String, String> userCredentialsMap = new HashMap<>();
        userCredentialsMap.put("lgn", userLogin);
        userCredentialsMap.put("pwd", userPassword);
        userCredentialsMap.put("g", "RIBO-01-22");

        String responseBody;

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

            //If the server successfully processed the POST
            if (responseCode != -1) {

                //Storing the server's response
                InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String currentLine;
                StringBuilder sbResponse = new StringBuilder();

                while ((currentLine = br.readLine()) != null) sbResponse.append(currentLine);

                responseBody = sbResponse.toString();
                Log.d("AuthProcess", responseBody);

                //Doing something depending on resultCode, that has been received from the server
                JSONObject jSONObject = new JSONObject(responseBody);
                int result = jSONObject.getInt("result_code");

                switch (result) {
                    //Auth-ed
                    case 1:
                        if (blockedQueue.isEmpty()) {
                            blockedQueue.add("allowed");
                        }
                        autoSaveLoginUsingSharedPrefs(userLogin, userPassword, contexter);
                        break;
                    //Not auth-ed
                    case -1:
                        if (blockedQueue.isEmpty()) {
                            blockedQueue.add("not allowed");
                        }
                        break;
                    default:
                        Log.e("AuthServerReturnedSomethingStrange", "Auth server has returned an unknown auth-code");
                        break;
                }
            }
        } catch (MalformedURLException | JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.e("AuthProcess", "No internet connectivity. Unable to auth the user.");

            if (blockedQueue.isEmpty()) {
                blockedQueue.add("not connected to auth-server");
            }
        }
    }

    private SharedPreferences userCredentials;

    //The method that saves a login and a password to auto-fill them after every app's launch
    private void autoSaveLoginUsingSharedPrefs(String userLogin, String userPassword, Context context) {
        userCredentials = contexter.getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences prefReader = userCredentials;
        Gson gson = new Gson();

        prefReader.edit().putString("userLogin", gson.toJson(userLogin)).apply();
        prefReader.edit().putString("userPassword", gson.toJson(userPassword)).apply();
    }
}

