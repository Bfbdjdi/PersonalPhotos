package com.mirea.kt.ribo.oao_salty;

import android.annotation.SuppressLint;
import android.content.Intent;

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

    //Replacing fragments with each other when a user taps on BottomNav's buttons
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //Setting up the EventsNotifierService's variables, using which different classes can notify
    //users about internal errors
    static ArrayBlockingQueue<String> blockedNetworkRelatedQueue = new ArrayBlockingQueue<>(1, true);
    static ArrayBlockingQueue<String> blockedFilesRelatedQueue = new ArrayBlockingQueue<>(1, true);

    static ArrayBlockingQueue<String> blockedExceptionReasonQueue = new ArrayBlockingQueue<>(1, true);

    static int howManyFilesWereAlreadyUploadedRecFromFUS = 0;
    static int howManyFilesToUploadRecFromFUS = 0;

    //Async task constantly working (if home screen shortcut wasn't used by a user) in the background.
    //It replaces ToolBar's title with a number of already uploaded files and vice versa
    //(only when syncing files)
    private Handler mHandler = new Handler();
    private Runnable syncCheckerTask = new Runnable() {
        @Override
        public void run() {
            Toolbar toolbar = findViewById(R.id.mainToolbar);
            setSupportActionBar(toolbar);

            if (howManyFilesWereAlreadyUploadedRecFromFUS != 0) {
                toolbar.setTitle(getString(R.string.toolbarAlreadyUploadedTitle) + howManyFilesWereAlreadyUploadedRecFromFUS + "/" + howManyFilesToUploadRecFromFUS);
            }
            else
            {
                toolbar.setTitle("PersonalPhotos");
            }

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

        //Starting the Notifier service
        Intent serviceTogglerIntent = new Intent(this, EventsNotifierService.class);
        this.startService(serviceTogglerIntent);

        setContentView(R.layout.activity_bottom);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        //If a user hasn't chosen any directories on his/her device yet, the FoldersFragment will
        //appear the first after launching the app, in other cases the SyncFragment will be the first
        //to be displayed
        if (this.getSharedPreferences("PathsData", MODE_PRIVATE)
                .getString("listOfPaths", "[]").equals("[]"))
        {
            replaceFragment(new FoldersFragment());
            bottomNavigationView.setSelectedItemId(R.id.navigation_folders);
        }
        else
        {
            replaceFragment(new SyncFragment());
            bottomNavigationView.setSelectedItemId(R.id.navigation_sync);
        }

        //The button to start (open) the settings activity
        FloatingActionButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(item ->
        {
            Intent intentMainInterface = new Intent(this, SettingsActivity.class);
            startActivity(intentMainInterface);
        });

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        //Listening to on-BottomNav user's presses to switch fragments accordingly
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