package com.mirea.kt.ribo.oao_salty;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ArrayBlockingQueue;

public class BottomActivity extends AppCompatActivity {

    public BottomActivity() {
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    static ArrayBlockingQueue<String> blockedNetworkRelatedQueue = new ArrayBlockingQueue<>(1, true);
    static ArrayBlockingQueue<String> blockedFilesRelatedQueue = new ArrayBlockingQueue<>(1, true);

    static ArrayBlockingQueue<String> blockedExceptionReasonQueue = new ArrayBlockingQueue<>(1, true);

    private Handler mHandler = new Handler();
    private Runnable syncCheckerTask = new Runnable() {
        @Override
        public void run() {
            Toolbar toolbar = findViewById(R.id.mainToolbar);
            setSupportActionBar(toolbar);

            SharedPreferences tempPathsCounter = getApplicationContext().getSharedPreferences("TempPathsCounterData", MODE_PRIVATE);

            tempPathsCounter.getInt("TEMPhowManyItemsToUpload", 0);

            mHandler.post(syncCheckerTask);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(syncCheckerTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(syncCheckerTask);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent serviceTogglerIntent = new Intent(this, EventsNotifierService.class);
        this.startService(serviceTogglerIntent);

        setContentView(R.layout.activity_bottom);

        replaceFragment(new FoldersFragment());

        FloatingActionButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(item ->
        {
            Intent intentMainInterface = new Intent(this, SettingsActivity.class);
            startActivity(intentMainInterface);
        });

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            switch (item.getItemId()) {
                case R.id.navigation_folders:
                    replaceFragment(new FoldersFragment());
                    return true;
                case R.id.navigation_sync:
                    replaceFragment(new SyncFragment());
                    return true;
            }
            return false;
        });


        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });*/
    }
}