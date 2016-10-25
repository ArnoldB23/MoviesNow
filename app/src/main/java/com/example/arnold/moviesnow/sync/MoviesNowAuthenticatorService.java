package com.example.arnold.moviesnow.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Arnold on 9/22/2016.
 */
public class MoviesNowAuthenticatorService extends Service {

    private MoviesNowAuthenticator mMoviesNowAuthenticator;

    @Override
    public void onCreate() {
        mMoviesNowAuthenticator = new MoviesNowAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent){
        return mMoviesNowAuthenticator.getIBinder();
    }
}
