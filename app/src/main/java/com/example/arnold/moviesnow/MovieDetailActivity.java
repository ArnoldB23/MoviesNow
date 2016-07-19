package com.example.arnold.moviesnow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Arnold on 10/4/2015.
 */
public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        int position;


        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        //getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        //getSupportActionBar().setTitle("Let's do it!");
        //getSupportActionBar().setHomeButtonEnabled(true);

        if (fragment == null)
        {
            position = (int) getIntent().getExtras().getInt(ScreenSlidePagerActivity.EXTRA_MOVIE_POSITION);
            fragment = MovieDetailFragment.newInstance(position);

            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }



    }



}
