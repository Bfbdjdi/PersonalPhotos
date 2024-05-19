package com.mirea.kt.ribo.oao_salty;

import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedFilesRelatedQueue;

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

        sharedPaths = getSharedPreferences("SharedData", MODE_PRIVATE);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, 42);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null)
        {
            final Runnable thread = ()->{

                Uri uri = data.getData();
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                SharedPreferences prefEditor = sharedPaths;

                if (!prefEditor.contains("listOfPaths"))
                {
                    HashSet<String> allSavedDFPaths = new HashSet<>();
                    allSavedDFPaths.add(uri.toString());

                    Gson gson = new Gson();
                    String dataWrappedInString = gson.toJson(allSavedDFPaths);
                    prefEditor.edit().putString("listOfPaths", dataWrappedInString).apply();
                }
                else
                {
                    Gson gson = new Gson();

                    String encodedStringedPaths = prefEditor.getString("listOfPaths", "null");
                    Type convertType = new TypeToken<HashSet<String>>(){}.getType();
                    HashSet<String> allSavedDFPaths = gson.fromJson(encodedStringedPaths, convertType);

                    if (!allSavedDFPaths.contains(uri.toString()))
                    {
                        allSavedDFPaths.add(uri.toString());

                        String dataWrappedInString = gson.toJson(allSavedDFPaths);
                        prefEditor.edit().putString("listOfPaths", dataWrappedInString).apply();
                    }
                }
            };
            new Thread(thread).start();
        }
        else {
            blockedFilesRelatedQueue.add("no directories were chosen in the device's memory by the user");
        }

        Intent returnToHomeMenu = new Intent(getApplicationContext(), FolderPicker.class);
        returnToHomeMenu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(returnToHomeMenu);
    }
}


