package com.example.arnold.moviesnow.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Arnold on 10/10/2016.
 */
public class MoviesNowSyncService extends Service {
    private static MoviesNowSyncAdapter sSyncAdapter;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate()
    {
        synchronized(sSyncAdapterLock){
            if (sSyncAdapter == null){
                sSyncAdapter = new MoviesNowSyncAdapter(getApplicationContext(), true);
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
