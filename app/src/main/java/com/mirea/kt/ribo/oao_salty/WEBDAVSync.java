package com.mirea.kt.ribo.oao_salty;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import java.io.IOException;
import java.util.List;

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

    public void fileUploader()
    {
        final Runnable thread = ()->{
            Sardine sardine = new OkHttpSardine();
            sardine.setCredentials(login, password);

            try {
                if (!sardine.exists(driveURL + "/PersonalPhotos"))
                {
                    sardine.createDirectory(driveURL + "/PersonalPhotos");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        new Thread(thread).start();

        Intent folderPickerStart = new Intent(getApplicationContext(), FolderPicker.class);
        folderPickerStart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(folderPickerStart);
    }
}
