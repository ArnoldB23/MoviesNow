package com.example.arnold.moviesnow;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arnold.moviesnow.data.ContentProviderMovieContract;
import com.example.arnold.moviesnow.data.ContentProviderMovieDbSchema;
import com.example.arnold.moviesnow.sync.MoviesNowSyncAdapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * Created by Arnold on 9/28/2015.
 */
public class MovieGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final String LOG_TAG = "MovieGridFragment";

    public SwipeRefreshLayout mSwipeRefreshLayout;
    public GridViewWithHeaderAndFooter mGridView;
    public Spinner mCategorySpinner;
    public TextView mEmptyView;
    public MovieCursorAdapter mMovieCursorAdapter;
    public TextView mFooterTextView;


    public Handler mGridHandler;
    public HashMap<String, Integer> mPageToListMap;
    public boolean isDoneLoading;
    public Context mContext;

    public static final String CONFIG_CHANGE = "MovieGridFragment_Config_Change";
    public static final String SCROLL_STATE ="Scroll_state";
    public static final String CURRENT_PAGENUM_PREF = "CURRENT_PAGENUM_PREF";

    private final double FRICTION_SCALE_FACTOR = 1;
    private Parcelable mScrollState;
    public static final String POSITION_EXTRA = "POSITION_EXTRA";
    public static final int POSITION_REQUEST_CODE = 12;

    public static final int  MOVIEGRID_LOADER = 286;


    public static final String [] GRID_MOVIE_COLUMNS = {
            ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists._ID,
            ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies._ID,
            ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies.COL_ORIGINAL_TITLE,
            ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME,
            ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies.COL_POSTER_PATH,


    };

    //Poster path column index are based on GRID_MOVIE_COLUMNS
    public static final int MOVIES_TO_LISTS_ID_COL_INDEX = 0;
    public static final int MOVIE_ID_COL_INDEX = 1;
    public static final int MOVIE_ORIGINAL_TITLE_COL_INDEX = 2;
    public static final int MOVIE_LISTNAME = 3;
    public static final int POSTER_PATH_COL_INDEX = 4;



    @Override
    public void onCreate(Bundle onSavedInstanceState)
    {
        super.onCreate(onSavedInstanceState);

        if (onSavedInstanceState != null)
        {
            Log.d(LOG_TAG, "onCreate: Recovering SavedInstanceState");
            //mScrollState = onSavedInstanceState.getParcelable(SCROLL_STATE);
        }
        setHasOptionsMenu(true);
        //setRetainInstance(true);

        mPageToListMap = new HashMap<String, Integer>();
        mPageToListMap.put(MoviesNowSyncAdapter.TOP_RATED, 1);
        mPageToListMap.put(MoviesNowSyncAdapter.POPULAR, 1);
        mPageToListMap.put(MoviesNowSyncAdapter.UPCOMING, 1);
        mPageToListMap.put(MoviesNowSyncAdapter.NOW_PLAYING, 1);
        mPageToListMap.put(MoviesNowSyncAdapter.MY_FAVORITES, 1);

        Log.d(LOG_TAG, "onCreate");

        mContext = getContext();

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

    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mGridHandler = new Handler();

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mEmptyView = (TextView)rootView.findViewById(R.id.gridView_empty);


        //SwipeRefresh will go up one page by syncing if needed and restarting the loader.
        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                String currentList = MoviesNowSyncAdapter.getCurrentMovieList(mContext);

                int currentPage =  mPageToListMap.get(currentList);
                int upPage = currentPage;

                if (currentPage > 1)
                {
                    upPage--;
                    mPageToListMap.put(currentList, upPage);
                }


                if(MoviesNowSyncAdapter.isPageNumOutdated(mContext, currentList, String.valueOf(upPage)) )
                {
                    MoviesNowSyncAdapter.syncImmediately(mContext, upPage);
                }else
                {
                    Log.d(LOG_TAG, "Swipe up, is NOT OUTDATED");
                }

                getLoaderManager().restartLoader(MOVIEGRID_LOADER, null, MovieGridFragment.this);


                if (currentPage > 1)
                {
                    mGridView.setSelection(19);
                }else{
                    mGridView.setSelection(0);
                }



                final int currentPageFinal = currentPage;

                isDoneLoading = false;


                mGridHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            if(!isDoneLoading){
                                // re run the verification after 1 second
                                mGridHandler.postDelayed(this, 500);
                            }else{
                                // stop the animation after the data is fully loaded
                                mSwipeRefreshLayout.setRefreshing(false);

                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        });


        // DataObserver for mMovieCursorAdapter to update empty view
        mMovieCursorAdapter = new MovieCursorAdapter(mContext, null, 0);
        mMovieCursorAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();


                if (mMovieCursorAdapter.isEmpty()) {

                    mEmptyView.setVisibility(View.VISIBLE);
                    mFooterTextView.setVisibility(View.GONE);
                } else {

                    mEmptyView.setVisibility(View.GONE);
                    mFooterTextView.setVisibility(View.VISIBLE);
                }


            }
        });

        mGridView = (GridViewWithHeaderAndFooter) rootView.findViewById(R.id.gridView_movies);

        //mGridView.setEmptyView(mEmptyView);



        //mFooterTextView loads more movies from current list on click
        mFooterTextView = (TextView) inflater.inflate(R.layout.layout_footer, container, false);
        mFooterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String currentList = MoviesNowSyncAdapter.getCurrentMovieList(mContext);

                int currentPage = mPageToListMap.get(currentList);
                currentPage++;
                mPageToListMap.put(currentList, currentPage);

                if (MoviesNowSyncAdapter.isPageNumOutdated(mContext, currentList, String.valueOf(currentPage))) {
                    MoviesNowSyncAdapter.syncImmediately(mContext, currentPage);
                }


                getLoaderManager().restartLoader(MOVIEGRID_LOADER, null, MovieGridFragment.this);
                mGridView.setSelection(0);
            }
        });


        mGridView.addFooterView(mFooterTextView);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String currentListName = MoviesNowSyncAdapter.getCurrentMovieList(mContext);

                Uri moviesUri = ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(currentListName);

                Intent i = new Intent(getActivity(), ScreenSlidePagerActivity.class);
                i.putExtra(ScreenSlidePagerActivity.EXTRA_MOVIE_POSITION, position);
                i.putExtra(ScreenSlidePagerActivity.EXTRA_MOVIES_URI, moviesUri);
                i.putExtra(ScreenSlidePagerActivity.EXTRA_PAGENUM, mPageToListMap.get(currentListName));


                Log.d(LOG_TAG, "Gridview OnItemClick, position: " + position);
                startActivityForResult(i, POSITION_REQUEST_CODE);
            }
        });

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {


                /*
                * Add or Remove Movie to or from My Favorite list on Long click.
                 */

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Cursor movieCursor = (Cursor) parent.getAdapter().getItem(position);
                        ContentResolver cr = mContext.getContentResolver();

                        if (movieCursor != null) {
                            String movieId = movieCursor.getString(MOVIE_ID_COL_INDEX);

                            String where = ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = ? AND " +
                                    ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies._ID + " = ?";

                            Uri moviesToListWithListnameURI = ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(MoviesNowSyncAdapter.MY_FAVORITES);

                            Cursor isFavoriteSearch = cr.query(moviesToListWithListnameURI,
                                    new String[]{ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID},
                                    where, new String[]{MoviesNowSyncAdapter.MY_FAVORITES, movieId}, null);

                            if (isFavoriteSearch.moveToFirst()) {
                                String selection = ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID + " = ?";
                                int numdel = cr.delete(ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(MoviesNowSyncAdapter.MY_FAVORITES), selection, new String[]{movieId});

                                Log.d(LOG_TAG, "Deleted " + numdel + " from Favorites");
                                mGridHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext, "Removed from My Favorites!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                reorderFavoriteList(mContext);
                            } else {

                                Uri favoriteMovieListUri = ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(MoviesNowSyncAdapter.MY_FAVORITES);

                                Cursor favorites = cr.query(favoriteMovieListUri, null, null, null, null);

                                ContentValues values = new ContentValues();
                                values.put(ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID, movieId);
                                values.put(ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM, favorites.getCount() / MoviesNowSyncAdapter.FAVORITE_NUM_PER_PAGE + 1);

                                cr.insert(favoriteMovieListUri, values);


                                mGridHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext, "Added to My Favorites!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                            return;
                        }

                        Log.d(LOG_TAG, "Gridview Longclick, Cannot add movie to favorites!");

                    }
                }).start();

                return true;
            }
        });




        mGridView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (mGridView != null && mGridView.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = mGridView.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = mGridView.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                mSwipeRefreshLayout.setEnabled(enable);
            }
        });

        mGridView.setFriction((float) (ViewConfiguration.getScrollFriction() * FRICTION_SCALE_FACTOR));
        mGridView.setAdapter(mMovieCursorAdapter);

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



        ArrayAdapter<CharSequence> adapter = new ArrayAdapter (mContext, android.R.layout.simple_spinner_item);
        adapter.add(MoviesNowSyncAdapter.TOP_RATED);
        adapter.add(MoviesNowSyncAdapter.POPULAR);
        adapter.add(MoviesNowSyncAdapter.UPCOMING);
        adapter.add(MoviesNowSyncAdapter.NOW_PLAYING);
        adapter.add(MoviesNowSyncAdapter.MY_FAVORITES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);



        //Set spinner to current list name
        int position = adapter.getPosition(MoviesNowSyncAdapter.getCurrentMovieList(mContext));
        mCategorySpinner.setSelection(position);


        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                String currentItem = adapterView.getItemAtPosition(i).toString();;
                int currentPageNum = mPageToListMap.get(currentItem);

                MoviesNowSyncAdapter.setCurrentMovieList(mContext, currentItem);

                Log.d(LOG_TAG, "mCategorySpinner item selected, " + currentItem);



                 if(MoviesNowSyncAdapter.isPageNumOutdated(mContext, currentItem, String.valueOf(currentPageNum)) )
                {
                    //Log.d(LOG_TAG, "mCategorySpinner item selected, " + currentItem + " is outdated!");

                    MoviesNowSyncAdapter.syncImmediately(mContext, currentPageNum );


                }else
                {
                    //Log.d(LOG_TAG, "Spinner, is NOT OUTDATED");
                }

                getLoaderManager().restartLoader(MOVIEGRID_LOADER, null, MovieGridFragment.this);
                mGridView.setSelection(0);



            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




    }

    /**
     * Reorder movies in My Favorites list to create pages of MoviesNowSyncAdapter.FAVORITE_NUM_PER_PAGE number of movies
     */
    public static void reorderFavoriteList(final Context context)
    {

        Uri favoritesUri = ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(MoviesNowSyncAdapter.MY_FAVORITES);

        Cursor favorites = context.getContentResolver().query(favoritesUri,
                new String [] {ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID, ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID, ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM},
                null, null, null);

        String where = ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID + " = ? AND "
                + ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID + " = ? ";

        int page = 1;
        int item = 1;
        String movieId, listId;


        while (favorites.moveToNext())
        {
            if (item%MoviesNowSyncAdapter.FAVORITE_NUM_PER_PAGE == 0)
            {
                page++;
            }


            if (!favorites.getString(2).equals(page)){
                movieId = favorites.getString(0);
                listId = favorites.getString(1);

                ContentValues cr = new ContentValues();
                cr.put(ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM, page);

                //Log.d(LOG_TAG, "My Favorites renumbering pages: " + page + ", item = " + item);

                context.getContentResolver().update(ContentProviderMovieContract.MoviesToLists.CONTENT_URI, cr, where, new String[]{movieId, listId});
            }

            item++;
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
        Iterator it = mPageToListMap.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();

            outState.putInt((String)pair.getKey(), (Integer)pair.getValue());
        }

        Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putParcelable(SCROLL_STATE, mGridView.onSaveInstanceState());

    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


        if (savedInstanceState != null)
        {
            Iterator it = mPageToListMap.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();

                String movieList = (String) pair.getKey();
                int pageNum = savedInstanceState.getInt(movieList);

                pair.setValue(pageNum);
            }


            mScrollState = savedInstanceState.getParcelable(SCROLL_STATE);

        }


    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String currentListName = MoviesNowSyncAdapter.getCurrentMovieList(mContext);
        int currentPage = mPageToListMap.get(currentListName);

        Uri moviesUri = ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(currentListName);

        String selection = ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM + " = " + currentPage;


        return new CursorLoader(getActivity(),
                moviesUri,
                GRID_MOVIE_COLUMNS,
                selection,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        int count = data.getCount();
        Log.d(LOG_TAG, "onLoadFinished: " + MoviesNowSyncAdapter.getCurrentMovieList(mContext) +  ", count = " + count);

        isDoneLoading = true;

        mMovieCursorAdapter.swapCursor(data);

        if (mScrollState != null)
        {
            mGridView.onRestoreInstanceState(mScrollState);

            mScrollState = null;
        }




    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoadReset...");


        mMovieCursorAdapter.swapCursor(null);
    }

}