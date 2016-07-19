package com.example.arnold.moviesnow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Stack;


/**
 * Created by Arnold on 10/29/2015.
 */
public class ScreenSlidePagerActivity extends AppCompatActivity {
    public static final String EXTRA_MOVIE_POSITION = "MOVIEPOSITION";
    public static final String EXTRA_STACK = "STACK";
    public static final String LOG_TAG = "ScreenSlidePagerActivit";
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private Stack mMovieListPositionsStack;
    private int position;


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putIntegerArrayList(EXTRA_STACK, new ArrayList(mMovieListPositionsStack));
        savedInstanceState.putInt(EXTRA_MOVIE_POSITION, position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_screen_slide);
        getSupportActionBar().setHomeButtonEnabled(true);

        Log.d(LOG_TAG, "onCreate");

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);


        if (savedInstanceState != null)
        {
            position = savedInstanceState.getInt(EXTRA_MOVIE_POSITION);
        }

        else {
            position = getIntent().getExtras().getInt(EXTRA_MOVIE_POSITION);
        }


        mViewPager.setCurrentItem(position);


        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int ready = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //mMovieListPositionsStack.push(new Integer(position));
                //Log.d(LOG_TAG, "onPageScrolled() Position: " + position);

            }

            @Override
            public void onPageSelected(int pos) {
                //Log.d(LOG_TAG, "onPageSelected() Position: " + pos);

                if (ready > 0) {
                    Log.d(LOG_TAG, "onPageSelected() Push " + pos);
                    mMovieListPositionsStack.push(pos);
                    position = pos;

                }
                ready = 0;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        //Log.d(LOG_TAG, "onPageScrollStateChanged() SCROLL_STATE_DRAGGING");
                        ready = 1;
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        //Log.d(LOG_TAG, "onPageScrollStateChanged() SCROLL_STATE_IDLE");
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        //Log.d(LOG_TAG, "onPageScrollStateChanged() SCROLL_STATE_SETTLING");
                        break;
                }
            }
        });



        mMovieListPositionsStack = new Stack();
        if(savedInstanceState == null)
        {

            //Log.d(LOG_TAG, "onCreate() Push " + getIntent().getExtras().getInt(EXTRA_MOVIE_POSITION));
            mMovieListPositionsStack.push(getIntent().getExtras().getInt(EXTRA_MOVIE_POSITION));
        }
        else{
            mMovieListPositionsStack.addAll(savedInstanceState.getIntegerArrayList(EXTRA_STACK));
        }

    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        Log.d(LOG_TAG, "onRestart");
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void finish()
    {

        Log.d(LOG_TAG, "finish");

        Intent result = new Intent();
        result.putExtra(MovieGridFragment.POSITION_EXTRA, position);
        setResult(Activity.RESULT_OK, result);

        super.finish();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Log.d(LOG_TAG, "onPause");

        Log.d(LOG_TAG, "onPause: position = " + position);

        mPagerAdapter.notifyDataSetChanged();

    }


    @Override
    public void onStop()
    {
        super.onStop();
        Log.d(LOG_TAG, "onStop");

        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");



    }


    @Override
    public void onBackPressed() {


            position = (Integer) mMovieListPositionsStack.pop();
            Log.d(LOG_TAG, "onBackPressed() Pop " + position);


            if (!mMovieListPositionsStack.empty()) {
                position = (Integer) mMovieListPositionsStack.peek();
                Log.d(LOG_TAG, "onBackPressed() Peek " + position);
                mViewPager.setCurrentItem(position);
            }else{
                Log.d(LOG_TAG, "onBackPressed() Exiting " + position);
                super.onBackPressed();
            }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d(LOG_TAG, "onOptionsitemSelected: Home button pressed");
                Intent result = new Intent();
                result.putExtra(MovieGridFragment.POSITION_EXTRA, position);
                setResult(Activity.RESULT_OK, result);

                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{

        public ScreenSlidePagerAdapter(FragmentManager fm)
        {
            super(fm);
        }



        @Override
        public int getCount()
        {
            return MovieListSingleton.get(ScreenSlidePagerActivity.this).getMovieObjArrayList().size();
        }



        @Override
        public Fragment getItem(int position)
        {
            Log.d(LOG_TAG, "getItem() Position " + String.valueOf(position));

            return MovieDetailFragment.newInstance(position);
        }
    }
}
