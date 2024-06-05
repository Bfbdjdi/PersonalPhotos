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
import static com.mirea.kt.ribo.oao_salty.WEBDAVSync.isServiceToRun;

public class SyncFragment extends Fragment {
    public SyncFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //This runnable updates SyncFragment's TextViews every time there is need to do that
    private Handler mHandler = new Handler();
    private Runnable syncCheckerTask = new Runnable() {
        @Override
        public void run() {
            Button togglerSyncButtonTV = rootViewThisLocal.findViewById(R.id.togglerSyncButton);
            TextView previousSyncMomentTV = rootViewThisLocal.findViewById(R.id.previousSyncMomentTV);
            TextView totalFilesSavedTV = rootViewThisLocal.findViewById(R.id.totalFilesSavedTV);

            String theTimeTheUploadSucceeded = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("theLastSuccessfullSyncMoment", getString(R.string.neverSyncedFillerText));
            SharedPreferences totalPathsCounterSP = requireContext().getSharedPreferences("TempPathsCounterData", MODE_PRIVATE);
            Integer totalPathsCounter = totalPathsCounterSP.getInt("totalFilesDownloaded", -0);

            totalFilesSavedTV.setText(totalPathsCounter.toString());
            previousSyncMomentTV.setText(theTimeTheUploadSucceeded);

            //Switching the Sync's button title
            if ((isServiceToRun != null) && (isServiceToRun)){
                togglerSyncButtonTV.setText(R.string.stopSync);
                syncTogglerButton[0] = true;
            }
            else if (!syncTogglerButton[0]) {
                togglerSyncButtonTV.setText(R.string.performSync);
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

        syncFragmentBase();
        return rootViewThisLocal;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncFragmentBase();
        mHandler.post(syncCheckerTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(syncCheckerTask);
    }

    final static Boolean[] syncTogglerButton = {false};

    void syncFragmentBase() {

        TextView driveURLTV = rootViewThisLocal.findViewById(R.id.driveURLTV);
        Button togglerSyncButtonTV = rootViewThisLocal.findViewById(R.id.togglerSyncButton);

        //Setting the Sync's button text if an upload was started by a shortcut
        if (syncTogglerButton[0]) {
            togglerSyncButtonTV.setText(R.string.stopSync);
        } else {
            togglerSyncButtonTV.setText(R.string.performSync);
        }

        String driveURL = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("driveURL", "driveURL not avail");
        String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("folderNameUploadIn",
                "folderNameUploadIn not avail");
        String theTimeTheUploadSucceeded = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("theLastSuccessfullSyncMoment",
                "last sync moment not avail");

        //Displaying the path to the WEBDAV's upload folder
        if ((!driveURL.equals("driveURL not avail")) && (!theTimeTheUploadSucceeded.equals("last sync moment not avail"))) {
            driveURLTV.setText(String.format("%s/%s", driveURL, folderNameUploadIn));
        }

        WEBDAVSync WEBDAVUtil = new WEBDAVSync(requireContext());

        //The Sync button was pressed? Switching its title
        togglerSyncButtonTV.setOnClickListener(item ->
        {
            String serviceControllerCommand;
            syncTogglerButton[0] = !syncTogglerButton[0];

            if (syncTogglerButton[0]) {
                serviceControllerCommand = "startServiceFileUploader";
                togglerSyncButtonTV.setText(R.string.stopSync);
            } else {
                serviceControllerCommand = "stopServiceFileUploader";
                togglerSyncButtonTV.setText(R.string.performSync);
            }

            //Starting uploading files to the WEBDAV server
            WEBDAVUtil.fileUploader(serviceControllerCommand);
        });
    }
}