package com.mirea.kt.ribo.oao_salty;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.preference.PreferenceManager;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class WEBDAVSync extends ContextWrapper {

    private String login = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userWEBDAVLogin", "login not avail");
    private String password = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userWEBDAVPassword", "password not avail");

    private String driveURL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("driveURL", "driveURL not avail");

    private String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("folderNameUploadIn", "folderNameUploadIn not avail");

    public WEBDAVSync(Context contexter) {
        super(contexter);
    }

    public void syncListWebdav() {

        final Runnable thread = () -> {
            Sardine sardine = new OkHttpSardine();
            sardine.setCredentials(this.login, this.password);
            List<DavResource> resources;

            try {
                resources = sardine.list(this.driveURL);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (DavResource res : resources) {
                System.out.println(res); // calls the .toString() method.
            }
        };
        new Thread(thread).start();
    }

    public void foldersPathsObtainer() {
        Intent folderPickerStart = new Intent(getApplicationContext(), FolderPicker.class);
        startActivity(folderPickerStart);
    }

    static Boolean isServiceToRun;

    public void fileUploader(String action) {
        Intent serviceTogglerIntent = new Intent(this, FileUploadService.class);
        if (Objects.equals(action, "startServiceFileUploader")) {
            serviceTogglerIntent.setAction("serviceFileUploaderStart");
            System.out.println(action);
            isServiceToRun = true;
            this.startService(serviceTogglerIntent);
        }
        if (Objects.equals(action, "stopServiceFileUploader")) {
            serviceTogglerIntent.setAction("serviceFileUploaderStop");
            System.out.println(action);
            isServiceToRun = false;
            this.startService(serviceTogglerIntent);
        }
    }

    /*public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/
}
