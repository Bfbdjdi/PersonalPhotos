package com.mirea.kt.ribo.oao_salty;

import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import static com.mirea.kt.ribo.oao_salty.WEBDAVSync.isServiceToRun;

public class OnHomeScreenShortcut extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefReader = getSharedPreferences("UserData", MODE_PRIVATE);
        String userPassword = prefReader.getString("userPassword", "null");

        //If the user is already auth-ed in the app
        if (!userPassword.equals("null"))
        {
            //Stating the Notifier service
            Intent serviceTogglerIntent = new Intent(this, FileUploadService.class);
            serviceTogglerIntent.setAction("serviceFileUploaderStart");
            Intent notifierService = new Intent(this, EventsNotifierService.class);

            //Allowing the FileUploader to actually upload files
            isServiceToRun = true;

            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(this, R.string.shortcutBackgroundDownloadStart, Toast.LENGTH_SHORT).show();
            });

            this.startService(notifierService);
            this.startService(serviceTogglerIntent);
        }
        //If the user is not auth-ed, asking him/her to authorise and not allowing him/her upload files
        else
        {
            Toast.makeText(this, R.string.shortcutNoAuthWarning, Toast.LENGTH_LONG).show();
        }

        //Finishing all app's activities, while services keep working
        finishAffinity();
    }
}
