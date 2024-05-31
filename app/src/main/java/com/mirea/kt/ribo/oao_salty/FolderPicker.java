package com.mirea.kt.ribo.oao_salty;

import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedFilesRelatedQueue;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedNetworkRelatedQueue;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;

public class FolderPicker extends AppCompatActivity {

    static SharedPreferences sharedPaths;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPaths = getSharedPreferences("PathsData", MODE_PRIVATE);

        //Starting Android's stock filepicker
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, 42);
    }

    //Starting the NotifierService
    EventsNotifierService notifierService = new EventsNotifierService();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If the user have chosen a path using stock filepicker
        if (data != null) {
            final Runnable thread = () -> {

                //Obtaining the chosen path
                Uri uri = data.getData();

                //Asking Android to make this uri usable anywhere at any time outside this class
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                SharedPreferences prefEditor = sharedPaths;

                //If there are no paths saved in the SharedPref
                if (!prefEditor.contains("listOfPaths")) {
                    HashSet<String> allSavedDFPaths = new HashSet<>();
                    allSavedDFPaths.add(uri.toString());

                    //Converting the set to JSON and saving it
                    Gson gson = new Gson();
                    String dataWrappedInString = gson.toJson(allSavedDFPaths);
                    prefEditor.edit().putString("listOfPaths", dataWrappedInString).apply();
                }

                //If there are already some paths saved in the memory
                else {
                    Gson gson = new Gson();

                    String encodedStringedPaths = prefEditor.getString("listOfPaths", "null");
                    Type convertType = new TypeToken<HashSet<String>>() {
                    }.getType();
                    HashSet<String> allSavedDFPaths = gson.fromJson(encodedStringedPaths, convertType);

                    //If the chosen path is not stored in the memory, we save the path,
                    //otherwise doing nothing
                    if (!allSavedDFPaths.contains(uri.toString())) {
                        allSavedDFPaths.add(uri.toString());

                        String dataWrappedInString = gson.toJson(allSavedDFPaths);
                        prefEditor.edit().putString("listOfPaths", dataWrappedInString).apply();
                    }
                }
            };
            new Thread(thread).start();
        }

        //If a filepicker was started, but the user didn't choose any folder
        else {
            if (blockedNetworkRelatedQueue.isEmpty()) {
                blockedFilesRelatedQueue.add("no directories were chosen in the device's memory by the user");
                notifierService.onNotify(getApplicationContext());
            }
        }

        //Going back to the Folder fragment
        finish();
    }
}


