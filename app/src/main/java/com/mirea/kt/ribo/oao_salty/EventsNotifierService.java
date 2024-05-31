package com.mirea.kt.ribo.oao_salty;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedNetworkRelatedQueue;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedFilesRelatedQueue;
import static com.mirea.kt.ribo.oao_salty.SyncFragment.syncTogglerButton;
import static com.mirea.kt.ribo.oao_salty.WEBDAVSync.isServiceToRun;

import java.util.Objects;

public class EventsNotifierService extends Service {

    public EventsNotifierService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onNotify(Context contexter) {

        //Getting all the errors occurred previously
        String errorNetworkingMessage = blockedNetworkRelatedQueue.poll();
        String errorFileMessage = blockedFilesRelatedQueue.poll();

        //Network-related problems
        if (errorNetworkingMessage != null) {

            //Switching inside main thread to be able to toast
            new Handler(Looper.getMainLooper()).post(() -> {
                switch (Objects.requireNonNull(errorNetworkingMessage)) {
                    case "failed creating PersonalPhotos folder in the WEBDAV. Connectivity issue?":
                        Toast.makeText(contexter, R.string.failedCreatingRequiredFolder1, Toast.LENGTH_SHORT).show();
                        Toast.makeText(contexter, R.string.failedCreatingRequiredFolder2, Toast.LENGTH_SHORT).show();
                        Toast.makeText(contexter, R.string.failedCreatingRequiredFolder3, Toast.LENGTH_LONG).show();
                        break;
                    case "failed listing files in the WEBDAV":
                        Toast.makeText(contexter, R.string.failedListingFilesInTheWEBDAV, Toast.LENGTH_SHORT).show();
                        break;
                    case "failed to upload a file to the WEBDAV":
                        Toast.makeText(contexter, R.string.failedUploadFileToTheWEBDAV, Toast.LENGTH_SHORT).show();
                        break;
                    case "was connected, but then was suddenly disconnected":
                        Toast.makeText(contexter, R.string.lostConnectionWEBDAV, Toast.LENGTH_SHORT).show();
                        break;
                    case "some user data is not provided":
                        Toast.makeText(contexter, R.string.someWEBDAVUserDataNotProvided, Toast.LENGTH_SHORT).show();
                        break;
                }
            });

            //Changing SyncButton's title to "Sync"
            isServiceToRun = false;
            syncTogglerButton[0] = false;
        }

        //Files-related problems
        if (errorFileMessage != null) {
            //Switching inside main thread to be able to toast
            new Handler(Looper.getMainLooper()).post(() -> {
                switch (Objects.requireNonNull(errorFileMessage)) {
                    case "no directories were chosen in the device's memory by the user":
                        Toast.makeText(contexter, R.string.cancelledFolderPicker, Toast.LENGTH_SHORT).show();
                        break;
                    case "no on-device directories were chosen to work with":
                        Toast.makeText(contexter, R.string.noDirsOnDeviceAreChosen, Toast.LENGTH_SHORT).show();
                        break;
                    case "literally no files to upload":
                        Toast.makeText(contexter, R.string.nothingToUpload, Toast.LENGTH_SHORT).show();
                        break;
                }
            });
            isServiceToRun = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }
}