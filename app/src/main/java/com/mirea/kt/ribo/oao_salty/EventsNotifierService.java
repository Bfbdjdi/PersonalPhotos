package com.mirea.kt.ribo.oao_salty;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedExceptionReasonQueue;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedNetworkRelatedQueue;
import static com.mirea.kt.ribo.oao_salty.BottomActivity.blockedFilesRelatedQueue;
import java.util.Objects;

public class EventsNotifierService extends Service {

    public EventsNotifierService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onNotify(Context contexter)
    {
        String errorNetworkingMessage = blockedNetworkRelatedQueue.poll();
        String errorFileMessage = blockedFilesRelatedQueue.poll();
        String exceptionCodes = blockedExceptionReasonQueue.poll();

        if (errorNetworkingMessage != null) {
            switch (Objects.requireNonNull(errorNetworkingMessage)) {
                case "failed creating PersonalPhotos folder in the WEBDAV. Connectivity issue?":
                    Toast.makeText(getApplicationContext(), "Не создал папку для сохранения в облаке.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Адрес сервера, данные аккаунта верно указаны?", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, exceptionCodes, Toast.LENGTH_LONG).show();
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
                case "some user data is not provided":
                    Toast.makeText(this, "Указаны не все требуемые данные для работы с WEBDAV.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        if (errorFileMessage != null) {
            switch (Objects.requireNonNull(errorFileMessage)) {
                case "no directories were chosen in the device's memory by the user":
                    Toast.makeText(contexter, "Отмена выбора папки.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }
}