package com.mirea.kt.ribo.oao_salty;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public class BottomActivity extends AppCompatActivity {

    public BottomActivity() {
    }

    private void replaceFragment(Fragment fragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    static ArrayBlockingQueue<String> blockedNetworkRelatedQueue = new ArrayBlockingQueue<>(1, true);
    static ArrayBlockingQueue<String> blockedFilesRelatedQueue = new ArrayBlockingQueue<>(1, true);

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);

        replaceFragment(new SyncFragment());

        String errorNetworkingMessage = blockedNetworkRelatedQueue.poll();
        String errorFileMessage = blockedFilesRelatedQueue.poll();

        if (errorNetworkingMessage != null)
        {
            switch (Objects.requireNonNull(errorNetworkingMessage))
            {
                case "failed creating PersonalPhotos folder in the WEBDAV":
                    Toast.makeText(this, "Не создал папку PP в облаке.", Toast.LENGTH_SHORT).show();
                    break;
                case "failed listing files in the WEBDAV":
                    Toast.makeText(this, "Не узнал о файлах в папке PP облака.", Toast.LENGTH_SHORT).show();
                    break;
                case "failed to upload a file to the WEBDAV":
                    Toast.makeText(this, "Не смог загрузить файл в облако.", Toast.LENGTH_SHORT).show();
                    break;
                case "was connected, but then was suddenly disconnected":
                    Toast.makeText(this, "Связь с сервером WEBDAV потеряна.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        if (errorFileMessage != null)
        {
            switch (Objects.requireNonNull(errorFileMessage))
            {
                case "no directories were chosen in the device's memory by the user":
                    Toast.makeText(this, "Отмена выбора папки.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            switch (item.getItemId())
            {
                case R.id.navigation_folders:
                    replaceFragment(new FoldersFragment());
                    return true;
                case R.id.navigation_sync:
                    replaceFragment(new SyncFragment());
                    return true;
                case R.id.navigation_settings:
                    replaceFragment(new SettingsFragment());
                    return true;
            }
            return false;
        });
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
    }
}