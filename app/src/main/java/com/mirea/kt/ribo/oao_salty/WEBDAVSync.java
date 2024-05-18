package com.mirea.kt.ribo.oao_salty;

import static com.mirea.kt.ribo.oao_salty.FolderPicker.sharedPaths;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
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
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class WEBDAVSync extends ContextWrapper {

    private String login;
    private String password;
    private String driveURL;

    public WEBDAVSync(String login, String password, String driveURL, Context contexter) {
        super(contexter);
        this.login = login;
        this.password = password;
        this.driveURL = driveURL;
    }

    public void syncListWebdav()
    {
        final Runnable thread = ()->{
            Sardine sardine = new OkHttpSardine();
            sardine.setCredentials(this.login, this.password);
            List<DavResource> resources;

            try {
                resources = sardine.list(this.driveURL);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (DavResource res : resources)
            {
                System.out.println(res); // calls the .toString() method.
            }
        };
        new Thread(thread).start();
    }

    public void foldersPathsObtainer()
    {
        Intent folderPickerStart = new Intent(getApplicationContext(), FolderPicker.class);
        folderPickerStart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(folderPickerStart);
    }

    SharedPreferences sharedPaths;
    public void fileUploader()
    {
        final Runnable thread = ()->{

            //Setting up basic connectivity
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .build();

            Sardine sardine = new OkHttpSardine(okHttpClient);
            sardine.setCredentials(login, password);

            //checking whether specific folder exists
            try {
                if (!sardine.exists(driveURL + "/PersonalPhotos"))
                {
                    sardine.createDirectory(driveURL + "/PersonalPhotos");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //Retrieving Uri's from SharedPrefs
            sharedPaths = getSharedPreferences("SharedData", MODE_PRIVATE);
            SharedPreferences prefEditor = sharedPaths;
            String encodedStringedPaths = prefEditor.getString("listOfPaths", "null");

            //Setting up GSON
            Gson gson = new Gson();
            Type convertType = new TypeToken<HashSet<String>>(){}.getType();

            //Getting Uri's and adding them into HashSet
            HashSet<String> allSavedDFPaths = gson.fromJson(encodedStringedPaths, convertType);

            //Listing every file name that exists in the cloud folder
            List<DavResource> alreadyUploadedFilesDAV;
            try {
                alreadyUploadedFilesDAV = sardine.list(driveURL + "/PersonalPhotos");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //If the cloud folder is not empty, uploading only non-uploaded files:
            if (alreadyUploadedFilesDAV != null)
            {

                //Converting List<DavResource> to more useful HashSet<String>
                HashSet<String> alreadyUploadedFilesSTR = new HashSet<>();
                for (DavResource singleDavResource : alreadyUploadedFilesDAV)
                {
                    alreadyUploadedFilesSTR.add(singleDavResource.getName());
                }

                //Uploading files from each file path, retrieved from SharedPrefs
                for (String folderEntity : allSavedDFPaths)
                {

                    //Getting DocumentFileTree
                    Uri uri = Uri.parse(folderEntity);
                    DocumentFile directoryTreeOfFiles = DocumentFile.fromTreeUri(this, uri);

                    //Uploading files (sometimes) one by one
                    for (DocumentFile file : directoryTreeOfFiles.listFiles()) {
                        System.out.println(file.getName());
                        try {
                            if (!sardine.exists(driveURL + "/PersonalPhotos/" + file.getName()))
                            {
                                InputStream fis = DocumentFileUtils.openInputStream(file, this);
                                sardine.put(driveURL + "/PersonalPhotos/" + file.getName(), IOUtils.toByteArray(fis));
                                System.out.println(file.getName() + " - has been uploaded.");
                            }
                            else
                            {
                                System.out.println(file.getName() + " - skipping duplicate");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        };
        new Thread(thread).start();
    }
}
