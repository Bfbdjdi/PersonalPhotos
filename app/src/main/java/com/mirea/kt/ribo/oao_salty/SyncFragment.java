package com.mirea.kt.ribo.oao_salty;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SyncFragment extends Fragment {
    public SyncFragment() {
        // Required empty public constructor
    }

    TextView mSyncStateChecker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Handler mHandler = new Handler();
    private Runnable syncCheckerTask = new Runnable() {
        @Override
        public void run() {
            Button togglerSyncButtonTV = (Button) rootViewThisLocal.findViewById(R.id.togglerSyncButton);
            if (!syncTogglerButton[0]) togglerSyncButtonTV.setText("Начать синхронизацию");
            mHandler.post(syncCheckerTask);
        }
    };

    private View rootViewThisLocal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootViewThisLocal = inflater.inflate(R.layout.fragment_sync, container, false);

        mSyncStateChecker = rootViewThisLocal.findViewById(R.id.previousSyncMomentTV);
        updateTextViews();
        return rootViewThisLocal;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTextViews();
        mHandler.post(syncCheckerTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(syncCheckerTask);
    }

    final static Boolean[] syncTogglerButton = {false};

    void updateTextViews() {

        TextView previousSyncMomentTV = (TextView) rootViewThisLocal.findViewById(R.id.previousSyncMomentTV);
        TextView totalFilesSavedTV = (TextView) rootViewThisLocal.findViewById(R.id.totalFilesSavedTV);
        TextView driveURLTV = (TextView) rootViewThisLocal.findViewById(R.id.driveURLTV);
        Button togglerSyncButtonTV = (Button) rootViewThisLocal.findViewById(R.id.togglerSyncButton);

        if (syncTogglerButton[0]) {
            togglerSyncButtonTV.setText("Отменить синхронизацию...");
        } else {
            togglerSyncButtonTV.setText("Начать синхронизацию");
        }

        String driveURL = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("driveURL", "driveURL not avail");
        String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("folderNameUploadIn", "folderNameUploadIn not avail");

        //Setting up GSON
        //Gson gson = new Gson();
        //Type convertType = new TypeToken<HashSet<String>>() {}.getType();

        WEBDAVSync WEBDAVUtil = new WEBDAVSync(requireContext());

        togglerSyncButtonTV.setOnClickListener(item ->
        {
            String serviceControllerCommand;
            syncTogglerButton[0] = !syncTogglerButton[0];

            if (syncTogglerButton[0]) {
                serviceControllerCommand = "startServiceFileUploader";
                togglerSyncButtonTV.setText("Отменить синхронизацию...");
            } else {
                serviceControllerCommand = "stopServiceFileUploader";
                togglerSyncButtonTV.setText("Начать синхронизацию");
            }

            WEBDAVUtil.fileUploader(serviceControllerCommand);
        });

        driveURLTV.setText(driveURL + "/" + folderNameUploadIn);
    }
}