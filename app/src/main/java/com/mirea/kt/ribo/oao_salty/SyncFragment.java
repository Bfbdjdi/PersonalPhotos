package com.mirea.kt.ribo.oao_salty;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
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
            Button togglerSyncButtonTV = rootViewThisLocal.findViewById(R.id.togglerSyncButton);
            TextView previousSyncMomentTV = rootViewThisLocal.findViewById(R.id.previousSyncMomentTV);
            TextView totalFilesSavedTV = rootViewThisLocal.findViewById(R.id.totalFilesSavedTV);

            String theTimeTheUploadSucceeded = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("theLastSuccessfullSyncMoment", "(пока синхронизаций не было)");
            SharedPreferences totalPathsCounterSP = requireContext().getSharedPreferences("TempPathsCounterData", MODE_PRIVATE);
            Integer totalPathsCounter = totalPathsCounterSP.getInt("totalFilesDownloaded", -0);

            totalFilesSavedTV.setText(totalPathsCounter.toString());
            previousSyncMomentTV.setText(theTimeTheUploadSucceeded);

            if (!syncTogglerButton[0]) {
                togglerSyncButtonTV.setText("Начать синхронизацию");
            }

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

        TextView driveURLTV = rootViewThisLocal.findViewById(R.id.driveURLTV);
        Button togglerSyncButtonTV = rootViewThisLocal.findViewById(R.id.togglerSyncButton);

        if (syncTogglerButton[0]) {
            togglerSyncButtonTV.setText("Отменить синхронизацию...");
        } else {
            togglerSyncButtonTV.setText("Начать синхронизацию");
        }

        String driveURL = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("driveURL", "driveURL not avail");
        String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("folderNameUploadIn", "folderNameUploadIn not avail");
        String theTimeTheUploadSucceeded = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("theLastSuccessfullSyncMoment", "last sync moment not avail");

        if ((!driveURL.equals("driveURL not avail")) && (!theTimeTheUploadSucceeded.equals("last sync moment not avail"))) {
            driveURLTV.setText(String.format("%s/%s", driveURL, folderNameUploadIn));
        }

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
    }
}