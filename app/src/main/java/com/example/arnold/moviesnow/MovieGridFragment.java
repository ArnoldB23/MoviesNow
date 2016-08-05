package com.example.arnold.moviesnow;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by Arnold on 9/28/2015.
 */
public class MovieGridFragment extends Fragment implements FetchMovieDetailsPostExecute{
    private ArrayList<MovieObj> mMovieList;
    public MovieImageAdapter mMovieImageAdapter;
    public GridView mGridView;
    public static final String LOG_TAG = "MovieGridFragment";
    public Spinner mCategorySpinner;

    public static final String CONFIG_CHANGE = "MovieGridFragment_Config_Change";
    public static final String SCROLL_STATE ="Scroll_state";
    private boolean mIsConfigChange = false;

    private final double FRICTION_SCALE_FACTOR = 1;
    private Parcelable mScrollState;
    public static final String POSITION_EXTRA = "POSITION_EXTRA";
    public static final int POSITION_REQUEST_CODE = 12;


    @Override
    public void onCreate(Bundle onSavedInstanceState)
    {
        super.onCreate(onSavedInstanceState);

        if (onSavedInstanceState != null)
        {
            Log.d(LOG_TAG, "onCreate: Recovering SavedInstanceState");
            mIsConfigChange = onSavedInstanceState.getBoolean(MovieGridFragment.CONFIG_CHANGE);
            mScrollState = onSavedInstanceState.getParcelable(SCROLL_STATE);
        }
        setHasOptionsMenu(true);
        //setRetainInstance(true);


        Log.d(LOG_TAG, "onCreate");

    }

    @Override
    public void onPause()
    {
        super.onPause();



    }



    @Override
    public  void onResume()
    {

        super.onResume();


        Log.d(LOG_TAG, "onResume");

        if(mMovieImageAdapter != null && MovieListSingleton.get(getContext()).getCurrentListName() != null){



            if( MovieListSingleton.get(getContext()).getCurrentListName().equals(FetchMoviesTask.MY_FAVORITES)){

                MovieListSingleton.get(getContext()).removeNonFavoritesFromMovieObjArrayList();
                //mMovieImageAdapter.clear();
                //getFavoriteMovieList();

                //mMovieImageAdapter.addAll();


                Log.d(LOG_TAG, "onResume: My Favorites list && NotifiedDataSet" );
            }

            else {
                //MovieListSingleton.get(getContext()).updateMovieObjArrayListWithFavorites();
            }

            mMovieImageAdapter.notifyDataSetChanged();
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        mMovieList = MovieListSingleton.get(getActivity()).getMovieObjArrayList();
        mMovieImageAdapter = new MovieImageAdapter(mMovieList, getActivity());


        mGridView = (GridView) rootView.findViewById(R.id.gridView_movies);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(getActivity(), ScreenSlidePagerActivity.class);
                i.putExtra(ScreenSlidePagerActivity.EXTRA_MOVIE_POSITION, position);

                startActivityForResult(i, POSITION_REQUEST_CODE);
            }
        });

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });

        mGridView.setFriction((float) (ViewConfiguration.getScrollFriction() * FRICTION_SCALE_FACTOR));

        Log.d(LOG_TAG, "onCreateView");

        mGridView.setAdapter(mMovieImageAdapter);



        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Override this method in the activity that hosts the Fragment and call super
        // in order to receive the result inside onActivityResult from the fragment.
        //super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOG_TAG, "onActivityResult: requestCode = " + requestCode + " resultCode = " + resultCode);

        if (requestCode == POSITION_REQUEST_CODE)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                int pos = data.getExtras().getInt(POSITION_EXTRA);
                Log.d(LOG_TAG, "onActivityResult: " + "position = " + pos);

                mGridView.smoothScrollToPosition(pos);

            }
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem menuItemCategorySpinner = menu.findItem(R.id.action_category_spinner);
        mCategorySpinner = (Spinner)menuItemCategorySpinner.getActionView();


        Log.d(LOG_TAG, "onCreateOptionsMenu");

        if (mCategorySpinner != null)
        {

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.movie_category_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCategorySpinner.setAdapter(adapter);



            if(MovieListSingleton.get(getActivity()).getCurrentListName() == null)
            {
                MovieListSingleton.get(getActivity()).setCurrentListName(FetchMoviesTask.NOW_PLAYING);
            }

            int position = adapter.getPosition(MovieListSingleton.get(getActivity()).getCurrentListName());
            mCategorySpinner.setSelection(position);


            mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    String currentItem;
                    if(mIsConfigChange)
                    {
                        currentItem = MovieListSingleton.get(getActivity()).getCurrentListName();

                    }
                    else{

                        currentItem = adapterView.getItemAtPosition(i).toString();
                        MovieListSingleton.get(getActivity()).setCurrentListName(currentItem);

                    }

                    if(currentItem.equals(FetchMoviesTask.MY_FAVORITES))
                    {
                        mMovieImageAdapter.clear();
                        //mMovieImageAdapter.addAll(mMyFavoriteMovieList);
                        //mMovieImageAdapter.notifyDataSetChanged();

                        getFavoriteMovieList();
                    }
                    else{

                        if( mIsConfigChange)
                        {
                            mIsConfigChange = false;
                        }
                        else{
                            new FetchMoviesTask(mMovieImageAdapter).execute(currentItem);
                            mGridView.setSelection(0);
                            Log.d(LOG_TAG, "Spinner selected " + currentItem);
                        }


                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


        }

    }

    public void getFavoriteMovieList()
    {
        ContentResolver contentResolver = getContext().getContentResolver();
        String [] projection = new String[]{ContentProviderMovieContract.FavoriteMovieList.MOVIE_ID, ContentProviderMovieContract.FavoriteMovieList.MOVIE_NAME};
        Cursor cursor = contentResolver.query(ContentProviderMovieContract.FavoriteMovieList.CONTENT_URI, projection, null, null,null);

        ArrayList<MovieObj> favoriteMovieObjs = new ArrayList<MovieObj>();

        if (cursor.moveToFirst())
        {
            do{
                String movieId = cursor.getString(0);
                String movie_name = cursor.getString(1);

                MovieObj favoriteMovieObj = new MovieObj();
                favoriteMovieObj.id = movieId;
                Log.d(LOG_TAG, "Spinner 'My Favorites' selected: " + movie_name + " " + favoriteMovieObj.id);

                favoriteMovieObjs.add(favoriteMovieObj);

            }while(cursor.moveToNext());

            cursor.close();

            new FetchMovieDetailsTask(MovieGridFragment.this).execute(favoriteMovieObjs);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_category_spinner) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        outState.putBoolean(MovieGridFragment.CONFIG_CHANGE, true);
        Log.d(LOG_TAG, "onSaveInstanceState");
        mScrollState = mGridView.onSaveInstanceState();
        outState.putParcelable(SCROLL_STATE,mScrollState);
    }


    @Override
    public void postExecute(ArrayList <MovieObj> movies) {



        for(int i = 0; i < movies.size(); i++){

            movies.get(i).favorite = true;


        }
        mMovieImageAdapter.clear();
        mMovieImageAdapter.addAll(movies);
        mMovieImageAdapter.notifyDataSetChanged();
        //MovieListSingleton.get(getContext()).updateMovieObjArrayListWithFavorites();

        Log.d(LOG_TAG, "postExecute: ");
    }
}