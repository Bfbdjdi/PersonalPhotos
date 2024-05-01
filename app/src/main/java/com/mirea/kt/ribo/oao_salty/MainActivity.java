package com.mirea.kt.ribo.oao_salty;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button authButton;

    private EditText loginInputField;
    private EditText passwordInputField;

    ArrayBlockingQueue<String> blockedQueue = new ArrayBlockingQueue<>(1, true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authButton = findViewById(R.id.loginButton);
        loginInputField = findViewById(R.id.loginInput);
        passwordInputField = findViewById(R.id.passwordInput);
        authButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.loginButton)
        {
            String strLoginInputField = loginInputField.getText().toString();
            String strPasswordInputField = passwordInputField.getText().toString();

            ServerAuthProcess credentialsToCheck = new ServerAuthProcess(strLoginInputField, strPasswordInputField, blockedQueue);

            Thread userAuthThread = new Thread(credentialsToCheck);
            userAuthThread.start();
            try {
                userAuthThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (Objects.equals(blockedQueue.poll(), "allowed"))
            {
                Toast.makeText(this, "Добро пожаловать, Геогрий Salty.", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "Неверные логин и/или пароль.", Toast.LENGTH_LONG).show();
            }
        }
    }
}