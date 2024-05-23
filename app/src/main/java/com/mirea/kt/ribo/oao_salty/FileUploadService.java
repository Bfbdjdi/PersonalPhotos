package com.mirea.kt.ribo.oao_salty;

import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedExceptionReasonQueue;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedNetworkRelatedQueue;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import com.anggrayudi.storage.file.DocumentFileUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class FileUploadService extends Service {
    public FileUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    SharedPreferences sharedPaths;
    EventsNotifierService notifierService = new EventsNotifierService();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if (Objects.equals(intent.getAction(), "serviceFileUploaderStart")) {
            Log.i("ServiceRuntimeInfo", "Received toStart Foreground Intent");
            String login = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userWEBDAVLogin", "login not avail");
            String password = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userWEBDAVPassword", "password not avail");
            String driveURL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("driveURL", "driveURL not avail");
            String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("folderNameUploadIn", "folderNameUploadIn not avail");

            final Runnable thread = () -> {

                if ((!driveURL.equals("driveURL not avail")) && (!login.equals("login not avail")) &&
                        (!password.equals("password not avail")) && (!folderNameUploadIn.equals("folderNameUploadIn not avail"))) {

                    //Setting up basic connectivity
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.MINUTES)
                            .readTimeout(5, TimeUnit.MINUTES)
                            .build();

                    Sardine sardine = new OkHttpSardine(okHttpClient);
                    sardine.setCredentials(login, password);

                    //checking whether specific folder exists
                    try {
                        if (!sardine.exists(driveURL + "/" + folderNameUploadIn)) {
                            sardine.createDirectory(driveURL + "/" + folderNameUploadIn);
                        }
                    } catch (IOException | IllegalArgumentException e) {
                        if (blockedNetworkRelatedQueue.isEmpty()) {
                            blockedNetworkRelatedQueue.add("failed creating PersonalPhotos folder in the WEBDAV. Connectivity issue?");
                            notifierService.onNotify(getApplicationContext());
                        }
                        if (blockedNetworkRelatedQueue.isEmpty()) {
                            blockedExceptionReasonQueue.add(e.getMessage());
                            notifierService.onNotify(getApplicationContext());

                        }
                        System.out.println("failed creating PersonalPhotos folder in the WEBDAV. Connectivity issue?");
                        System.out.println("Exception has occured: \n" + e.getMessage());
                        return;
                    }

                    //Retrieving Uri's from SharedPrefs
                    sharedPaths = getSharedPreferences("PathsData", MODE_PRIVATE);
                    SharedPreferences prefReader = sharedPaths;
                    String encodedStringedPaths = prefReader.getString("listOfPaths", "null");

                    //Setting up GSON
                    Gson gson = new Gson();
                    Type convertType = new TypeToken<HashSet<String>>() {}.getType();

                    //Getting Uri's and adding them into HashSet
                    HashSet<String> allSavedDFPaths = gson.fromJson(encodedStringedPaths, convertType);

                    //!!! если нет путей, то HashSet = null !!!

                    //Listing every file name that exists in the cloud folder
                    List<DavResource> alreadyUploadedFilesDAV = null;
                    try {
                        alreadyUploadedFilesDAV = sardine.list(driveURL + "/" + folderNameUploadIn);
                    } catch (IOException e) {
                        System.out.println("failed listing files in the WEBDAV");
                        blockedNetworkRelatedQueue.add("failed listing files in the WEBDAV");
                        notifierService.onNotify(getApplicationContext());
                    }

                    //If the cloud folder is not empty, uploading only non-uploaded files:
                    if (alreadyUploadedFilesDAV != null) {

                        //Converting List<DavResource> to more useful HashSet<String>
                        HashSet<String> alreadyUploadedFilesSTR = new HashSet<>();
                        for (DavResource singleDavResource : alreadyUploadedFilesDAV) {
                            alreadyUploadedFilesSTR.add(singleDavResource.getName());
                        }

                        //Uploading files from each file path, retrieved from SharedPrefs
                        for (String folderEntity : allSavedDFPaths) {

                            //Getting DocumentFileTree
                            Uri uri = Uri.parse(folderEntity);
                            DocumentFile directoryTreeOfFiles = DocumentFile.fromTreeUri(this, uri);

                            //Uploading files (sometimes) one by one
                            for (DocumentFile file : directoryTreeOfFiles.listFiles()) {
                                System.out.println(file.getName());
                                try {
                                    if (!sardine.exists(driveURL + "/" + folderNameUploadIn + "/" + file.getName())) {
                                        InputStream fis = DocumentFileUtils.openInputStream(file, this);
                                        sardine.put(driveURL + "/" + folderNameUploadIn + "/" + file.getName(), IOUtils.toByteArray(fis));
                                        System.out.println(file.getName() + " - has been uploaded.");
                                    } else {
                                        System.out.println(file.getName() + " - skipping duplicate");
                                    }
                                } catch (IOException e) {
                                    blockedNetworkRelatedQueue.add("failed to upload a file to the WEBDAV");
                                    System.out.println("failed to upload a file to the WEBDAV");
                                    notifierService.onNotify(getApplicationContext());
                                }
                            }
                        }
                    }

                } else {
                    if (blockedNetworkRelatedQueue.isEmpty()) {
                        blockedNetworkRelatedQueue.add("some user data is not provided");
                        notifierService.onNotify(getApplicationContext());
                    }
                }
            };
            new Thread(thread).start();
        }
        else if (Objects.equals(intent.getAction(), "serviceFileUploaderStop")) {
            Log.i("ServiceRuntimeInfo", "Received toStop Foreground Intent");

            stopForeground(true);
            stopSelfResult(startId);
        }
        return START_REDELIVER_INTENT;
    }
}