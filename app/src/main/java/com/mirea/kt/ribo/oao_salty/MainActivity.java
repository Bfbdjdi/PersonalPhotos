package com.mirea.kt.ribo.oao_salty;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button authButton;
    private EditText loginInputField;
    private EditText passwordInputField;
    private Toolbar toolBar;

    //Setting up the Queue to be able to notify users about some login-related problems
    ArrayBlockingQueue<String> blockedQueue = new ArrayBlockingQueue<>(1, true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting up a shortcut that starts the syncing process without starting up anything else
        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(this, "filesBackgroundUploadStarter")
                .setShortLabel(getString(R.string.SyncFilesShortShortcut))
                .setLongLabel(getString(R.string.SyncFilesLongShortcut))
                .setIcon(IconCompat.createWithResource(this, R.mipmap.upload_shortcut_icon))
                .setIntent(new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, OnHomeScreenShortcut.class))
                .build();

        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut);

        //Asking the user for the POST_NOTIFICATIONS permission (Android 13 and later)
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, 1);
            }
        }

        authButton = findViewById(R.id.loginButton);
        loginInputField = findViewById(R.id.loginInput);
        passwordInputField = findViewById(R.id.passwordInput);

        toolBar = findViewById(R.id.loginToolbar);
        setSupportActionBar(toolBar);

        authButton.setOnClickListener(this);

        //Retrieving some user's data from SharedPrefs
        SharedPreferences prefReader = getSharedPreferences("UserData", MODE_PRIVATE);
        String userLogin = prefReader.getString("userLogin", "null");
        String userPassword = prefReader.getString("userPassword", "null");

        //If the boolean is true, a login and a password will be auto-pasted in corresponding EditTexts
        //every time the app is opened
        boolean autoLoginSwitch = (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autoPasteLogin", true));

        if ((!userLogin.equals("null")) && (!userPassword.equals("null")) && autoLoginSwitch) {
            loginInputField.setText(userLogin.replace("\"", ""));
            passwordInputField.setText(userPassword.replace("\"", ""));
        }
    }

    @Override
    public void onClick(View v) {

        //Authenticating the user
        if (v.getId() == R.id.loginButton) {
            String strLoginInputField = loginInputField.getText().toString();
            String strPasswordInputField = passwordInputField.getText().toString();

            //Creating a new object to use some auth the user
            ServerAuthProcess credentialsToCheck = new ServerAuthProcess(strLoginInputField, strPasswordInputField, blockedQueue, this);

            //Change the TitleBar's text to "Connecting..." while auth-ing the user
            Thread userAuthThread = new Thread(credentialsToCheck);
            userAuthThread.start();

            if (this.getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.connectingToAuthServer);
            }
            try {
                userAuthThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            String networkPermission = blockedQueue.poll();

            //Doing something depending on a situation
            if (networkPermission != null) {
                String answ = Objects.requireNonNull(networkPermission);
                switch (answ) {
                    //if auth-ed
                    case "allowed":
                        Intent intentMainInterface = new Intent(this, BottomActivity.class);
                        intentMainInterface.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentMainInterface);
                        break;
                    //if not auth-ed
                    case "not allowed":
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("PersonalPhotos");
                        }
                        Toast.makeText(this, R.string.incorrectAuthServerLoginOrPassword, Toast.LENGTH_LONG).show();
                        break;
                    //no connection
                    case "not connected to auth-server":
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("PersonalPhotos");
                        }
                        Toast.makeText(this, R.string.notConnectedToAuthServer, Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, R.string.askUserToCheckInternet, Toast.LENGTH_SHORT).show();
                        break;
                    //disconnected by the server
                    case "was connected, but then was suddenly disconnected":
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("PersonalPhotos");
                        }
                        Toast.makeText(this, R.string.authServerDisconnected, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }
}
