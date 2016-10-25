package com.example.arnold.moviesnow;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.arnold.moviesnow.data.ContentProviderMovieContract;


/**
 * Created by Arnold on 10/29/2015.
 */
public class ScreenSlidePagerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final String EXTRA_MOVIE_POSITION = "MOVIEPOSITION";
    public static final String EXTRA_MOVIES_URI = "MOVIES_URI";
    public static final String EXTRA_PAGENUM = "PAGENUM";
    public static final String LOG_TAG = "ScreenSlidePagerActivit";
    private ViewPager mViewPager;
    public  CursorPagerAdapter mCursorPagerAdapter;
    private int mPosition;
    private Uri mMoviesUri;
    private boolean firstLoad = true;
    private int mPageNum;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(EXTRA_MOVIE_POSITION, mPosition);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_screen_slide);
        getSupportActionBar().setHomeButtonEnabled(true);

        Log.d(LOG_TAG, "onCreate");


        if (savedInstanceState != null)
        {
            mPosition = savedInstanceState.getInt(EXTRA_MOVIE_POSITION);
        }

        else {
            mPosition = getIntent().getExtras().getInt(EXTRA_MOVIE_POSITION);
            mMoviesUri = getIntent().getExtras().getParcelable(EXTRA_MOVIES_URI);
            mPageNum = getIntent().getExtras().getInt(EXTRA_PAGENUM);
        }

        Log.d(LOG_TAG, "onCreate, mPosition = " + mPosition);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mCursorPagerAdapter = new CursorPagerAdapter(getSupportFragmentManager(), MovieDetailFragment.class, MovieGridFragment.GRID_MOVIE_COLUMNS, null);
        mViewPager.setAdapter(mCursorPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int ready = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //mMovieListPositionsStack.push(new Integer(mPosition));
                //Log.d(LOG_TAG, "onPageScrolled() Position: " + mPosition);

            }

            @Override
            public void onPageSelected(int pos) {
                Log.d(LOG_TAG, "onPageSelected() Position: " + pos);

                if (ready > 0) {
                    Log.d(LOG_TAG, "onPageSelected() Push " + pos);
                    mPosition = pos;

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


        getSupportLoaderManager().initLoader(0, null, this);

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
        result.putExtra(MovieGridFragment.POSITION_EXTRA, mPosition);
        setResult(Activity.RESULT_OK, result);

        super.finish();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Log.d(LOG_TAG, "onPause");



    }


    @Override
    public void onStop()
    {
        super.onStop();
        Log.d(LOG_TAG, "onStop");

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");



    }


    @Override
    public void onBackPressed() {


        Log.d(LOG_TAG, "onBackPressed() Exiting " + mPosition);
        super.onBackPressed();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d(LOG_TAG, "onOptionsitemSelected: Home button pressed");
                Intent result = new Intent();
                result.putExtra(MovieGridFragment.POSITION_EXTRA, mPosition);
                setResult(Activity.RESULT_OK, result);

                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        String selection = ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM + " = " + mPageNum;

        return new CursorLoader(getApplicationContext(),
                mMoviesUri,
                MovieGridFragment.GRID_MOVIE_COLUMNS,
                selection,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {



        mCursorPagerAdapter.swapCursor(data);

        if (data != null && firstLoad == true)
        {
            Log.d(LOG_TAG, "onLoadFinished: Setting mViewPager to " + mPosition);
            mViewPager.setCurrentItem(mPosition);
            firstLoad = false;
        }



    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorPagerAdapter.swapCursor(null);
    }


}
