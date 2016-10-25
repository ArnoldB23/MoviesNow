package com.example.arnold.moviesnow;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.arnold.moviesnow.data.ContentProviderMovieContract;
import com.example.arnold.moviesnow.data.ContentProviderMovieDbSchema;
import com.example.arnold.moviesnow.sync.MoviesNowSyncAdapter;
import com.squareup.picasso.Picasso;

/**
 * Created by Arnold on 10/4/2015.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = "MovieDetailFragment";

    public RecyclerView mTrailerRecyclerView;
    public RecyclerView mReviewRecyclerView;
    public ReviewRecyclerViewAdapter mReviewRecyclerViewAdapter;
    public TrailerRecyclerViewAdapter mTrailerRecyclerViewAdapter;
    public TextView mTrailerSectionTitle;
    public TextView mReviewSectionTitle;
    public ScrollView mScrollView;
    public ImageView mBackdropImageView;
    public Button mFavoriteButton;
    public TextView mOverviewTextView;
    public TextView mOriginalTitleTextView;
    public TextView mOriginalTitle;
    public TextView mVoteAverage;
    public TextView mReleaseDate;


    public String mMovieId;
    public boolean mIsFavorite;
    private String mCurrentListName;
    private String mPageNum;
    public Context mContext;

    public final int MOVIE_DETAIL_LOADER_ID = 0;
    public final int MOVIE_TRAILERS_LOADER_ID = 1;
    public final int MOVIE_REVIEWS_LOADER_ID = 2;
    public final int MOVIE_FAVORITE_LOADER_ID = 3;


    public static final String [] MOVIE_DETAIL_LOADER_COLUMNS = {
                ContentProviderMovieContract.Movies._ID,
                ContentProviderMovieContract.Movies.COL_ORIGINAL_TITLE,
                ContentProviderMovieContract.Movies.COL_BACKDROP_PATH,
                ContentProviderMovieContract.Movies.COL_OVERVIEW,
                ContentProviderMovieContract.Movies.COL_VOTE_AVERAGE,
                ContentProviderMovieContract.Movies.COL_RELEASE_DATE};

    // These MOVIE_DETAIL_LOADER_COL_INDEX_... are mapped to MOVIE_DETAIL_LOADER_COLUMNS
    public static final int MOVIE_DETAIL_LOADER_COL_INDEX_MOVIEID = 0;
    public static final int MOVIE_DETAIL_LOADER_COL_INDEX_ORIGINAL_TITLE = 1;
    public static final int MOVIE_DETAIL_LOADER_COL_INDEX_BACKDROP_PATH = 2;
    public static final int MOVIE_DETAIL_LOADER_COL_INDEX_OVERVIEW = 3;
    public static final int MOVIE_DETAIL_LOADER_COL_INDEX_VOTE_AVERAGE= 4;
    public static final int MOVIE_DETAIL_LOADER_COL_INDEX_RELEASE_DATE = 5;


    public static final String [] MOVIE_TRAILER_LOADER_COLUMNS = {
            ContentProviderMovieContract.Trailers._ID,
            ContentProviderMovieContract.Trailers.COL_TRAILER_NAME,
            ContentProviderMovieContract.Trailers.COL_TRAILER_URL
    };

    // These MOVIE_TRAILER_LOADER_COL_INDEX_... are mapped to MOVIE_TRAILER_LOADER_COLUMNS
    public static final int MOVIE_TRAILER_LOADER_COL_INDEX_TRAILER_ID = 0;
    public static final int MOVIE_TRAILER_LOADER_COL_INDEX_TRAILER_NAME = 1;
    public static final int MOVIE_TRAILER_LOADER_COL_INDEX_TRAILER_URL = 2;

    public static final String [] MOVIE_REVIEW_LOADER_COLUMNS = {
            ContentProviderMovieContract.Reviews._ID,
            ContentProviderMovieContract.Reviews.COL_REVIEW_USERNAME,
            ContentProviderMovieContract.Reviews.COL_REVIEW_COMMENT
    };

    public static final int MOVIE_REVIEW_LOADER_COL_INDEX_REVIEW_ID = 0;
    public static final int MOVIE_REVIEW_LOADER_COL_INDEX_USERNAME = 1;
    public static final int MOVIE_REVIEW_LOADER_COL_INDEX_COMMENT = 2;

    @Override
    public void onCreate(Bundle onSaveInstance)
    {
        super.onCreate(onSaveInstance);

        mContext = getContext();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mMovieId = getArguments().getString(ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies._ID);
        mCurrentListName = getArguments().getString(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mBackdropImageView = (ImageView) rootView.findViewById(R.id.movie_imageView);
        mFavoriteButton = (Button) rootView.findViewById(R.id.favorite_button);
        mOriginalTitle = (TextView) rootView.findViewById(R.id.original_title_textView);
        mOverviewTextView = (TextView) rootView.findViewById(R.id.overview_textView);
        mVoteAverage = (TextView) rootView.findViewById(R.id.vote_average_textView);
        mReleaseDate = (TextView) rootView.findViewById(R.id.release_date_textView);
        mTrailerRecyclerView = (RecyclerView)rootView.findViewById(R.id.trailer_recycler_view);
        mReviewRecyclerView = (RecyclerView)rootView.findViewById(R.id.review_recycler_view);
        mTrailerSectionTitle = (TextView) rootView.findViewById(R.id.trailer_section_title);
        mReviewSectionTitle = (TextView) rootView.findViewById(R.id.review_section_title);
        mScrollView = (ScrollView)rootView.findViewById(R.id.detail_scrollView);

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ContentResolver contentResolver = getActivity().getContentResolver();

                if (mIsFavorite == false) {
                    mFavoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_black_24dp, 0, 0, 0);


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Uri favoriteMovieListUri = ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(MoviesNowSyncAdapter.MY_FAVORITES);
                            Cursor favorites = contentResolver.query(favoriteMovieListUri, null, null, null, null);

                            int totalNumFavorites = favorites.getCount();
                            int page = totalNumFavorites/MoviesNowSyncAdapter.FAVORITE_NUM_PER_PAGE + 1;
                            ContentValues values = new ContentValues();
                            values.put(ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM, page);
                            values.put(ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID, mMovieId);

                            contentResolver.insert(favoriteMovieListUri, values);
                        }
                    }).start();


                } else {
                    mFavoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String where = ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID + " = ?";
                            //contentResolver.delete(ContentProviderMovieContract.Movies.buildMovieUriWithID(Long.valueOf(mMovieId)), null, null);
                            int numdel = contentResolver.delete(ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(MoviesNowSyncAdapter.MY_FAVORITES), where, new String [] {mMovieId});
                            Log.d(LOG_TAG, "Deleted " + numdel + " from Favorites");

                            MovieGridFragment.reorderFavoriteList(mContext);
                        }
                    }).start();
                }

                mIsFavorite = !mIsFavorite;

            }
        });

        mBackdropImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOriginalTitle.getText().toString().isEmpty())
                    return false;


                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, mOriginalTitle.getText().toString() + " showtimes");

                startActivity(intent);
                return true;
            }
        });

        mReviewRecyclerViewAdapter = new ReviewRecyclerViewAdapter(mContext, null);
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mReviewRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));
        mReviewRecyclerView.setNestedScrollingEnabled(false);
        mReviewRecyclerView.setAdapter(mReviewRecyclerViewAdapter);

        mTrailerRecyclerViewAdapter = new TrailerRecyclerViewAdapter(mContext, null);
        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mTrailerRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));
        mTrailerRecyclerView.setNestedScrollingEnabled(false);
        mTrailerRecyclerView.setAdapter(mTrailerRecyclerViewAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER_ID, null, this);
        getLoaderManager().initLoader(MOVIE_TRAILERS_LOADER_ID, null, this);
        getLoaderManager().initLoader(MOVIE_REVIEWS_LOADER_ID, null, this);
        getLoaderManager().initLoader(MOVIE_FAVORITE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public static Fragment newInstance ()
    {
        Fragment f = new MovieDetailFragment();

        return f;
    }




    @Override
    public void onResume()
    {
        super.onResume();

        Log.d(LOG_TAG, "onResume ");

        if(mScrollView != null){
            mScrollView.scrollTo(0, 0);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Log.d(LOG_TAG, "onPause ");

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        CursorLoader cursorLoader = null;
        String where = null;

        switch(id){
            case MOVIE_DETAIL_LOADER_ID:
                where = ContentProviderMovieContract.Movies._ID + " = " + mMovieId;
                cursorLoader = new CursorLoader(mContext, ContentProviderMovieContract.Movies.CONTENT_URI, MOVIE_DETAIL_LOADER_COLUMNS, where, null, null);
                break;
            case MOVIE_REVIEWS_LOADER_ID:
                where = ContentProviderMovieContract.Reviews.COL_MOVIE_ID + " = " + mMovieId;
                cursorLoader = new CursorLoader(mContext, ContentProviderMovieContract.Reviews.CONTENT_URI, MOVIE_REVIEW_LOADER_COLUMNS, where, null, null);
                break;
            case MOVIE_TRAILERS_LOADER_ID:
                where = ContentProviderMovieContract.Trailers.COL_MOVIE_ID + " = " + mMovieId;
                cursorLoader = new CursorLoader(mContext, ContentProviderMovieContract.Trailers.CONTENT_URI, MOVIE_TRAILER_LOADER_COLUMNS, where, null, null);
                break;
            case MOVIE_FAVORITE_LOADER_ID:
                where = ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = ? AND " +
                        ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies._ID + " = ?";
                cursorLoader = new CursorLoader(mContext,
                        ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(MoviesNowSyncAdapter.MY_FAVORITES),
                        new String [] {ContentProviderMovieContract.Movies.COL_ORIGINAL_TITLE, ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM},
                        where, new String []{MoviesNowSyncAdapter.MY_FAVORITES, mMovieId}, null);
                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch(loader.getId()){
            case MOVIE_DETAIL_LOADER_ID:

                if (data != null && data.moveToFirst())
                {
                    GradientDrawable errorDrawable = new GradientDrawable();
                    errorDrawable.setShape(GradientDrawable.RECTANGLE);
                    errorDrawable.setSize(360, 202);
                    errorDrawable.setColor(Color.LTGRAY);
                    errorDrawable.setStroke(1, Color.BLACK);

                    String backdropPath = MovieCursorAdapter.constructPosterPath(data.getString(MOVIE_DETAIL_LOADER_COL_INDEX_BACKDROP_PATH));

                    Picasso.with(getActivity()).load(backdropPath).error(errorDrawable).into(mBackdropImageView);

                    mOriginalTitle.setText(data.getString(MOVIE_DETAIL_LOADER_COL_INDEX_ORIGINAL_TITLE));

                    mVoteAverage.setText(data.getString(MOVIE_DETAIL_LOADER_COL_INDEX_VOTE_AVERAGE) + "/10");

                    String dates [] = data.getString(MOVIE_DETAIL_LOADER_COL_INDEX_RELEASE_DATE).split("-");
                    String months [] = {"", "January","February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                    mReleaseDate.setText("(" + months[Integer.valueOf(dates[1])] + " " + dates[0] + ")");


                    mOverviewTextView.setText(data.getString(MOVIE_DETAIL_LOADER_COL_INDEX_OVERVIEW));
                }

                break;
            case MOVIE_REVIEWS_LOADER_ID:
                mReviewRecyclerViewAdapter.swapCursor(data);

                break;
            case MOVIE_TRAILERS_LOADER_ID:
                mTrailerRecyclerViewAdapter.swapCursor(data);
                break;

            case MOVIE_FAVORITE_LOADER_ID:

                if (data.moveToFirst())
                {

                    mFavoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_black_24dp, 0, 0, 0);
                    mIsFavorite = true;
                }
                else {

                    mFavoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);
                    mIsFavorite = false;
                }

                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch(loader.getId()){
            case MOVIE_DETAIL_LOADER_ID:
                break;
            case MOVIE_REVIEWS_LOADER_ID:
                mReviewRecyclerViewAdapter.swapCursor(null);
                break;
            case MOVIE_TRAILERS_LOADER_ID:
                mTrailerRecyclerViewAdapter.swapCursor(null);
                break;
        }

    }
}
