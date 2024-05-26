package com.mirea.kt.ribo.oao_salty;

import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedExceptionReasonQueue;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedFilesRelatedQueue;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedNetworkRelatedQueue;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.howManyFilesToUploadRecFromFUS;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.howManyFilesWereAlreadyUploadedRecFromFUS;
import static com.mirea.kt.ribo.oao_salty.SyncFragment.syncTogglerButton;
import static com.mirea.kt.ribo.oao_salty.WEBDAVSync.isServiceToRun;

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

import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashSet;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Counter of files that were successfully uploaded. The variable is created here
        //as to make it possible for the service to save the counter while going through
        //self-stop proccess
        AtomicInteger howManyNewFilesWereUploadedNow = new AtomicInteger();

        if (Objects.equals(intent.getAction(), "serviceFileUploaderStart")) {
            Log.i("ServiceRuntimeInfo", "Received toStart Foreground Intent");

            String login = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userWEBDAVLogin", "login not avail");
            String password = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userWEBDAVPassword", "password not avail");
            String driveURL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("driveURL", "driveURL not avail");
            String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("folderNameUploadIn", "folderNameUploadIn not avail");

            Runnable thread = () -> {

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
                        Log.e("WEBDAVerror", "failed creating PersonalPhotos folder in the WEBDAV. Connectivity issue?\nException has occurred: \n" + e.getMessage());
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

                    boolean connectedOK = false;

                    //Checking whether a connection to the WEBDAV is established
                    try {
                        sardine.list(driveURL + "/" + folderNameUploadIn);
                        connectedOK = true;
                    } catch (IOException e) {
                        Log.e("WEBDAVerror", "failed listing files in the WEBDAV");
                        blockedNetworkRelatedQueue.add("failed listing files in the WEBDAV");
                        notifierService.onNotify(getApplicationContext());
                    }

                    //If the cloud folder is not empty, uploading only non-uploaded files:
                    if ((allSavedDFPaths != null) && (connectedOK)) {

                        int howManyFilesToUpload = 0;
                        //Counting all the files to upload. As we need to know the exact number of all
                        //files from every directory, we need to iterate through the latter in total twice
                        for (String folderEntity : allSavedDFPaths) {

                            //Getting DocumentFileTree
                            Uri uri = Uri.parse(folderEntity);
                            DocumentFile directoryTreeOfFiles = DocumentFile.fromTreeUri(this, uri);

                            //Uploading files (sometimes) one by one
                            for (DocumentFile file : directoryTreeOfFiles.listFiles()) {

                                if (file.isFile()) {
                                    howManyFilesToUpload++;
                                }
                            }
                            howManyFilesToUploadRecFromFUS = howManyFilesToUpload;
                        }

                        //Uploading files from each file path, retrieved from SharedPrefs
                        if (!allSavedDFPaths.isEmpty()) {

                            howManyFilesWereAlreadyUploadedRecFromFUS = 0;

                            boolean areThereAnyFilesToUpload = false;
                            for (String folderEntity : allSavedDFPaths) {

                                //Getting DocumentFileTree
                                Uri uri = Uri.parse(folderEntity);
                                DocumentFile directoryTreeOfFiles = DocumentFile.fromTreeUri(this, uri);

                                //Uploading files (sometimes) one by one
                                for (DocumentFile file : directoryTreeOfFiles.listFiles()) {

                                    if (!isServiceToRun) {
                                        Log.i("WEBDAVinfo", "Stopping service after the user's command");
                                        break;
                                    }

                                    areThereAnyFilesToUpload = true;

                                    if (file.isFile()) {
                                        try {
                                            if (!sardine.exists(driveURL + "/" + folderNameUploadIn + "/" + file.getName())) {
                                                InputStream fis = DocumentFileUtils.openInputStream(file, this);
                                                sardine.put(driveURL + "/" + folderNameUploadIn + "/" + file.getName(), IOUtils.toByteArray(fis));
                                                Log.i("WEBDAVupload", file.getName() + " - has been uploaded.");

                                                howManyNewFilesWereUploadedNow.getAndIncrement();
                                                howManyFilesWereAlreadyUploadedRecFromFUS++;
                                            } else {
                                                Log.i("WEBDAVupload", file.getName() + " - skipping duplicate");

                                                howManyFilesWereAlreadyUploadedRecFromFUS++;
                                            }
                                        } catch (IOException e) {
                                            if (blockedNetworkRelatedQueue.isEmpty()) {
                                                blockedNetworkRelatedQueue.add("failed to upload a file to the WEBDAV");
                                                notifierService.onNotify(getApplicationContext());
                                            }
                                            Log.e("WEBDAVerror", "failed to upload a file to the WEBDAV");
                                        }
                                    }
                                }
                            }
                            if (!areThereAnyFilesToUpload) {
                                Log.i("WEBDAVupload", "literally no files to upload");

                                blockedFilesRelatedQueue.add("literally no files to upload");
                                notifierService.onNotify(getApplicationContext());
                            }
                        }
                        else
                        {
                            Log.i("WEBDAVupload", "literally no files to upload");

                            blockedFilesRelatedQueue.add("literally no files to upload");
                            notifierService.onNotify(getApplicationContext());
                        }
                        SharedPreferences totalPathsCounter = getApplicationContext().getSharedPreferences("TempPathsCounterData", MODE_PRIVATE);
                        int howManyFilesWereEverUploaded = totalPathsCounter.getInt("totalFilesDownloaded", 0);
                        totalPathsCounter.edit().putInt("totalFilesDownloaded", howManyFilesWereEverUploaded + howManyNewFilesWereUploadedNow.get()).apply();

                        howManyFilesWereAlreadyUploadedRecFromFUS = 0;
                        //if all the files were successfully uploaded, we don't this variable to contain
                        //counter related to them
                        howManyNewFilesWereUploadedNow.set(0);

                    } else {
                        Log.i("WEBDAVupload", "no on-device directories were chosen to work with");

                        blockedFilesRelatedQueue.add("no on-device directories were chosen to work with");
                        notifierService.onNotify(getApplicationContext());
                    }
                    Calendar now = Calendar.getInstance();
                    String theTimeTheUploadSucceeded = String.format("%02d.%02d.%02d, %02d:%02d",
                            now.get(Calendar.DATE), now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR),
                            now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));

                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("theLastSuccessfullSyncMoment", theTimeTheUploadSucceeded).apply();
                } else {
                    if (blockedNetworkRelatedQueue.isEmpty()) {
                        blockedNetworkRelatedQueue.add("some user data is not provided");
                        notifierService.onNotify(getApplicationContext());
                    }
                }
                Log.i("ServiceRuntimeInfo", "Stopped as task is completed");

                syncTogglerButton[0] = false;

            };
            new Thread(thread).start();

            stopForeground(true);
            stopSelfResult(startId);

        } else if (Objects.equals(intent.getAction(), "serviceFileUploaderStop")) {
            Log.i("ServiceRuntimeInfo", "Received toStop Foreground Intent");

            syncTogglerButton[0] = false;

            stopForeground(true);
            stopSelf();
        }
        return START_REDELIVER_INTENT;
    }
}