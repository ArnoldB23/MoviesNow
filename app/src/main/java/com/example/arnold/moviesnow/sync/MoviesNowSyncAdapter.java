package com.example.arnold.moviesnow.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.arnold.moviesnow.BuildConfig;
import com.example.arnold.moviesnow.R;
import com.example.arnold.moviesnow.data.ContentProviderMovie;
import com.example.arnold.moviesnow.data.ContentProviderMovieContract;
import com.example.arnold.moviesnow.data.ContentProviderMovieDbSchema;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Arnold on 9/26/2016.
 */
public class MoviesNowSyncAdapter extends AbstractThreadedSyncAdapter {
    public final static String LOG_TAG = MoviesNowSyncAdapter.class.getSimpleName();

    private static final long SYNC_INTERVAL_SEC = 60 * 180; //3 hours in seconds
    //private static final long SYNC_INTERVAL_MS = SYNC_INTERVAL_SEC * 1000; Todo
    private static final long SYNC_INTERVAL_MS = SYNC_INTERVAL_SEC * 1000;
    private static final long SYNC_FLEXTIME = SYNC_INTERVAL_SEC/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final long SERVER_REQUEST_RATE_LIMIT_MS = 1000 * 10;
    public static final String CURRENTMOVIELISTNAME_PREF = "currentmovielistname";
    public static final String PAGE_NUM_EXTRA = "PAGE_NUM_EXTRA";
    private static final long MOVIE_NUM_LIMIT = 200;
    public static final int FAVORITE_NUM_PER_PAGE = 20;


    public static final String TOP_RATED = "Top Rated";
    public static final String POPULAR = "Most Popular";
    public static final String UPCOMING = "Upcoming";
    public static final String NOW_PLAYING = "Now Playing";
    public static final String MY_FAVORITES = "My Favorites";

    public static final String SERVER_OK = "SERVER OK";
    public static final String SERVER_DOWN = "SERVER_DOWN";
    public static final String SERVER_ERROR = "SERVER_ERROR";
    public static final String SERVER_UNKNOWN = "SERVER_UNKNOWN";
    public Uri mMoviesToListsUriWithListName;
    public static boolean isPageOutdated;
    public int mPageNum;
    public long mCurrentUTC;
    public int mPageLimit;
    public Handler mHandler;

    public String mMovieListName;


    public MoviesNowSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mHandler = new Handler();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
    {
        Log.d(LOG_TAG, "Starting sync");

        ArrayList<String> movieIds = null;
        ArrayList<String> httpRequestResults = new ArrayList<String>();
        mMovieListName = getCurrentMovieList(getContext());
        mMoviesToListsUriWithListName = ContentProviderMovieContract.MoviesToLists.buildMoviesToListsUriWithListName(mMovieListName);
        String resultHttpRequest;
        Uri.Builder baseUribuilder = new Uri.Builder();
        Uri.Builder searchUriBuilder;

        baseUribuilder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie");

        mPageNum = extras.getInt(PAGE_NUM_EXTRA, 1);
        mPageLimit = mPageNum + 0;
        //int mPageLimit = mPageNum;
        mCurrentUTC = System.currentTimeMillis();

        switch (mMovieListName)
        {
            case TOP_RATED:
                baseUribuilder.appendPath("top_rated");
                break;
            case POPULAR:
                baseUribuilder.appendPath("popular");
                break;
            case UPCOMING:
                baseUribuilder.appendPath("upcoming");
                break;
            case NOW_PLAYING:
                baseUribuilder.appendPath("now_playing");
                break;
            case MY_FAVORITES:{

                //String orderby = ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM + " ASC ";



                return;
            }


        }

        baseUribuilder.appendQueryParameter("api_key", BuildConfig.MY_MOVIE_DB_API_KEY);


        for (int i = mPageNum, retry = 0; i <= mPageLimit ; i++)
        {
            searchUriBuilder = baseUribuilder.build().buildUpon();
            searchUriBuilder.appendQueryParameter("page", String.valueOf(i));

            resultHttpRequest = sendHttpRequest(searchUriBuilder.build().toString());

            if (null != resultHttpRequest)
            {
                try{



                    movieIds = parseAndInsertMovieData(resultHttpRequest, i);



                    String selection = ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM + " = ? " +
                            " AND " + ContentProviderMovieContract.MoviesToLists.COL_UTC_TIMESTAMP + " < ?";

                    String [] selectionArgs = new String [] {String.valueOf(i), String.valueOf(mCurrentUTC)};
                    int deletedRows = getContext().getContentResolver().delete(mMoviesToListsUriWithListName, selection, selectionArgs);
                    Log.d(LOG_TAG, "Deleting old records: deleteRows = " + deletedRows);
                }catch(JSONException e){
                    Log.e(LOG_TAG, " Error parseInsertMovieData", e);
                    return;
                }

                retry = 0;
            }

            else if (retry < 3){
                i--;
                retry++;
                try {
                    Thread.sleep(SERVER_REQUEST_RATE_LIMIT_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(LOG_TAG, "Server request rate limit of 40 requests per 10 seconds reached.\n Delaying sync by " + SERVER_REQUEST_RATE_LIMIT_MS/1000 + " seconds.");

            }
        }


        if (movieIds != null &&!movieIds.isEmpty())
        {

            for (int i=0, retry = 0; i < movieIds.size(); i++)
            {
                String movieId = movieIds.get(i);

                Uri.Builder movieTrailerReviewUriBuilder = new Uri.Builder();

                movieTrailerReviewUriBuilder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(movieId)
                        .appendQueryParameter("api_key", BuildConfig.MY_MOVIE_DB_API_KEY)
                        .appendQueryParameter("append_to_response", "trailers,reviews");


                String Url = movieTrailerReviewUriBuilder.build().toString();

                resultHttpRequest = sendHttpRequest(Url);
                if(resultHttpRequest != null)
                {
                    try{
                        parseAndInsertMovieTrailersAndReviews(resultHttpRequest);
                    }catch(JSONException e){
                        Log.e(LOG_TAG, " Error parseAndInsertMovieTrailersAndReviews", e);
                        return;
                    }

                    retry = 0;
                }

                else if (retry < 3)
                {
                    i--;
                    retry++;
                    try {
                        Thread.sleep(SERVER_REQUEST_RATE_LIMIT_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(LOG_TAG, "Server request rate limit of 40 requests per 10 seconds reached.\n Delaying sync by " + SERVER_REQUEST_RATE_LIMIT_MS/1000 + " seconds.");
                }



            }


            int deletedRows = getContext().getContentResolver().delete(ContentProviderMovieContract.Movies.CONTENT_URI, ContentProviderMovieContract.MOVIES_WITHOUT_lISTNAMES_WHERE_CLAUSE, null);
            Log.d(LOG_TAG, "Deleting isolated records: deleteRows = " + deletedRows);


            /*

            String selection = ContentProviderMovieContract.Movies._ID + " NOT IN ( " +
            " SELECT " + ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies._ID +
            " FROM " + ContentProviderMovie.sMoviesJoinMoviesToListTableString +
            " WHERE " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME +
            " IS NOT ? AND " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM +
            " IS NOT ? ORDER BY " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_UTC_TIMESTAMP +
            " DESC LIMIT ? )";
                        String [] selectionArgs = new String [] { mMovieListName, String.valueOf(mPageNum), String.valueOf(MOVIE_NUM_LIMIT) };
            deletedRows = getContext().getContentResolver().delete(ContentProviderMovieContract.Movies.CONTENT_URI, selection, selectionArgs);
            Log.d(LOG_TAG, "Deleting oldest records: deleteRows = " + deletedRows);
            */

            String selection = ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID +  " NOT IN ( " +
                    " SELECT " +  ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies._ID +
                    " FROM " + ContentProviderMovie.sMoviesJoinMoviesToListTableString +
                    " WHERE " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME +
                    " = ? ORDER BY " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_UTC_TIMESTAMP +
                    " DESC LIMIT ? ) ";


            Log.d(LOG_TAG, "Deleting oldest records, selection = " + selection);

            String [] selectionArgs = new String [] {  mMovieListName, String.valueOf(MOVIE_NUM_LIMIT) };
            deletedRows = getContext().getContentResolver().delete(mMoviesToListsUriWithListName, selection, selectionArgs);
            Log.d(LOG_TAG, "Deleting oldest records: deleteRows = " + deletedRows);



            Cursor totalMovies = getContext().getContentResolver().query(ContentProviderMovieContract.Movies.CONTENT_URI, null, null, null, null);
            Log.d(LOG_TAG, "Total number of movies = " + totalMovies.getCount());
            totalMovies.close();




            //updateMovieListToCurrentTime(getContext(), mMovieListName);
        }

        getContext().getContentResolver().notifyChange(mMoviesToListsUriWithListName, null);

    }

    private ArrayList<String> parseAndInsertMovieData(String movieJsonStr, int pageNum)
            throws JSONException
    {

        JSONObject movieRootJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieRootJson.getJSONArray("results");
        String totalPages = movieRootJson.getString("total_pages");

        ContentValues cv_list = new ContentValues();
        cv_list.put(ContentProviderMovieContract.MovieLists.COL_TOTAL_PAGES, totalPages);
        String where = ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = \"" + mMovieListName + "\"";
        getContext().getContentResolver().update(ContentProviderMovieContract.MovieLists.CONTENT_URI, cv_list, where, null);


        ArrayList<ContentValues> movieObjBuffer = new ArrayList<ContentValues>(movieArray.length());
        ArrayList<String> movieIds = new ArrayList<String>(movieArray.length());

        ArrayList<ContentValues> movies_to_lists_cvs = new ArrayList<ContentValues>(movieArray.length());


        for(int i = 0; i < movieArray.length(); i++)
        {
            JSONObject movieJson = movieArray.getJSONObject(i);


            String poster_path = movieJson.getString("poster_path");
            poster_path = poster_path.substring(1);
            String original_title = movieJson.getString("original_title");
            String overview = movieJson.getString("overview");
            String release_date = movieJson.getString("release_date");
            String vote_average = movieJson.getString("vote_average");
            String id = movieJson.getString("id");
            String backdrop_path = movieJson.getString("backdrop_path");
            backdrop_path = backdrop_path.substring(1);

            ContentValues cv_movie = new ContentValues();
            cv_movie.put(ContentProviderMovieContract.Movies.COL_POSTER_PATH, poster_path);
            cv_movie.put(ContentProviderMovieContract.Movies.COL_ORIGINAL_TITLE, original_title);
            cv_movie.put(ContentProviderMovieContract.Movies.COL_OVERVIEW, overview);
            cv_movie.put(ContentProviderMovieContract.Movies.COL_RELEASE_DATE, release_date);
            cv_movie.put(ContentProviderMovieContract.Movies.COL_VOTE_AVERAGE, vote_average);
            cv_movie.put(ContentProviderMovieContract.Movies._ID, id);
            cv_movie.put(ContentProviderMovieContract.Movies.COL_BACKDROP_PATH, backdrop_path);


            ContentValues cv_movies_to_list = new ContentValues();
            cv_movies_to_list.put(ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID, id);
            cv_movies_to_list.put(ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM, pageNum);
            cv_movies_to_list.put(ContentProviderMovieContract.MoviesToLists.COL_UTC_TIMESTAMP, mCurrentUTC);
            movies_to_lists_cvs.add(cv_movies_to_list);

            Log.d(LOG_TAG, "parseAndInsertMovieData: " + original_title);
            movieObjBuffer.add(cv_movie);
            movieIds.add(id);


        }



        String currentMovieList = getCurrentMovieList(getContext());

        if (! movieObjBuffer.isEmpty())
        {
            ContentValues [] contentValuesArray = new ContentValues[movieObjBuffer.size()];
            movieObjBuffer.toArray(contentValuesArray);

            ContentValues [] movies_to_lists_array = new ContentValues[movies_to_lists_cvs.size()];
            movies_to_lists_cvs.toArray(movies_to_lists_array);


            int insertedRows = getContext().getContentResolver().bulkInsert(ContentProviderMovieContract.Movies.CONTENT_URI, contentValuesArray);
            Log.d(LOG_TAG, "parseAndInsertMovieData: Movie insertedRows = " + insertedRows);

            insertedRows = getContext().getContentResolver().bulkInsert(mMoviesToListsUriWithListName, movies_to_lists_array);
            Log.d(LOG_TAG, "parseAndInsertMovieData: Movies_To_List insertedRows = " + insertedRows);

            return movieIds;
        }

        return null;
    }

    private void parseAndInsertMovieTrailersAndReviews (String movieJsonStr) throws JSONException
    {

        JSONObject rootJsonObject = new JSONObject(movieJsonStr);

        JSONObject trailersJsonObject = rootJsonObject.getJSONObject("trailers");
        JSONArray trailersJsonArray = trailersJsonObject.getJSONArray("youtube");

        String movieId = rootJsonObject.getString("id");


        ArrayList <ContentValues> trailerCV = new ArrayList<ContentValues>(trailersJsonArray.length());

        ContentValues[] contentValuesArray = new ContentValues[trailersJsonArray.length()];



        for (int j = 0; j < trailersJsonArray.length(); j++ )
        {
            JSONObject trailerJsonObject = trailersJsonArray.getJSONObject(j);

            String trailer_name = trailerJsonObject.getString("name");
            String trailer_url = trailerJsonObject.getString("source");
            trailer_url = "https://www.youtube.com/watch?v=" + trailer_url;

            ContentValues cv = new ContentValues();
            cv.put(ContentProviderMovieContract.Trailers.COL_TRAILER_NAME, trailer_name);
            cv.put(ContentProviderMovieContract.Trailers.COL_TRAILER_URL, trailer_url);
            cv.put(ContentProviderMovieContract.Trailers.COL_MOVIE_ID, movieId);

            /*
            Log.d(LOG_TAG, "parseAndInsertMovieTrailersAndReviews: trailers = " + cv.getAsString(ContentProviderMovieContract.Trailers.COL_TRAILER_NAME));
            Log.d(LOG_TAG, "parseAndInsertMovieTrailersAndReviews: trailers = " + cv.getAsString(ContentProviderMovieContract.Trailers.COL_TRAILER_URL));
            */

            trailerCV.add(cv);
        }

        trailerCV.toArray(contentValuesArray);
        String where = ContentProviderMovieContract.Trailers.COL_MOVIE_ID + " = " + movieId;
        getContext().getContentResolver().delete(ContentProviderMovieContract.Trailers.CONTENT_URI, where, null);
        getContext().getContentResolver().bulkInsert(ContentProviderMovieContract.Trailers.CONTENT_URI, contentValuesArray);


        JSONObject reviewsJsonObject = rootJsonObject.getJSONObject("reviews");
        JSONArray reviewsJsonArray = reviewsJsonObject.getJSONArray("results");

        ArrayList <ContentValues> reviewCV = new ArrayList<ContentValues>(reviewsJsonArray.length());
        contentValuesArray = new ContentValues[reviewsJsonArray.length()];
        reviewCV.toArray(contentValuesArray);

        for (int k = 0; k < reviewsJsonArray.length(); k++)
        {
            JSONObject reviewJsonObject = reviewsJsonArray.getJSONObject(k);

            ContentValues cv = new ContentValues();

            cv.put(ContentProviderMovieContract.Reviews.COL_REVIEW_COMMENT, reviewJsonObject.getString("content") );
            cv.put(ContentProviderMovieContract.Reviews.COL_REVIEW_USERNAME, reviewJsonObject.getString("author") );
            cv.put(ContentProviderMovieContract.Reviews.COL_MOVIE_ID, movieId);

            /*
            Log.d(LOG_TAG, "parseAndInsertMovieTrailersAndReviews: reviews = " + cv.getAsString(ContentProviderMovieContract.Reviews.COL_REVIEW_COMMENT));
            Log.d(LOG_TAG, "parseAndInsertMovieTrailersAndReviews: reviews = " + cv.getAsString(ContentProviderMovieContract.Reviews.COL_REVIEW_USERNAME));
            */

            reviewCV.add(cv);
        }

        reviewCV.toArray(contentValuesArray);
        where = ContentProviderMovieContract.Reviews.COL_MOVIE_ID + " = " + movieId;
        int deletedRows = getContext().getContentResolver().delete(ContentProviderMovieContract.Reviews.CONTENT_URI, where, null);

        Log.d(LOG_TAG, "parseAndInsertMovieTrailersAndReviews: deleteRows = " + deletedRows);


        int insertedRows = getContext().getContentResolver().bulkInsert(ContentProviderMovieContract.Reviews.CONTENT_URI, contentValuesArray);

        Log.d(LOG_TAG, "parseAndInsertMovieTrailersAndReviews: insertedRows = " + insertedRows);
    }


    public static String sendHttpRequest (String myUrl)
    {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String receivedJsonStr = null;

        try{

            URL url = new URL(myUrl);


            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();



            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            receivedJsonStr = buffer.toString();
            Log.d(LOG_TAG, "Received JSON String: " + receivedJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;

        }
        catch(Exception e)
        {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        }


        finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }


        return receivedJsonStr;
    }

    public static String getCurrentMovieList(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(CURRENTMOVIELISTNAME_PREF, NOW_PLAYING);
    }

    public static void setCurrentMovieList(Context context, String newCurrentListName)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (newCurrentListName.equals(TOP_RATED) || newCurrentListName.equals(POPULAR) || newCurrentListName.equals(UPCOMING)
                || newCurrentListName.equals(NOW_PLAYING) || newCurrentListName.equals(MY_FAVORITES)){
            prefs.edit().putString(CURRENTMOVIELISTNAME_PREF, newCurrentListName).apply();

            return;
        }

        Log.d(LOG_TAG,"setCurrentMovieList: Invalid list name = " + newCurrentListName);


    }

    public void deleteOldDataPastLimit ()
    {

    }

    public static boolean isPageNumOutdated(final Context context, final String MovieListName, final String pageNum)
    {

        Thread queryThread = new Thread(new Runnable() {
            public void run() {

                String selection = ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID + " = (SELECT " +
                        ContentProviderMovieContract.MovieLists._ID + " FROM " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS +
                        " WHERE " + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = ?)" +
                        " AND " + ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM + " = ?";

                long currentUTC = System.currentTimeMillis();

                Cursor cursor = context.getContentResolver().query(ContentProviderMovieContract.MoviesToLists.CONTENT_URI,
                        new String [] {ContentProviderMovieContract.MoviesToLists.COL_UTC_TIMESTAMP},
                        selection, new String []{MovieListName, pageNum}, ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID  + " LIMIT 1 ");

                Log.d(LOG_TAG, "isPageNumOutdated, selection: " + selection);

                if (cursor.moveToFirst())
                {
                    Long page_timestamp = cursor.getLong(0);

                    Log.d(LOG_TAG, "isPageNumOutdated, page_timestamp = " + page_timestamp);

                    if (currentUTC - page_timestamp >= SYNC_INTERVAL_MS)
                    {
                        Log.d(LOG_TAG, "isPageNumOutdated, OUTDATED!");
                        isPageOutdated = true;
                        return;
                    }

                    Log.d(LOG_TAG, "isPageNumOutdated, is NOT OUTDATED");
                    isPageOutdated = false;

                    return;
                }

                Log.d(LOG_TAG, "isPageNumOutdated, OUTDATED!");
                isPageOutdated = true;
            }
        });

        queryThread.start();

        try {
            queryThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (isPageOutdated)
        {
            return true;
        }

        return false;

    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        Bundle bundle = new Bundle();
        bundle.putInt(PAGE_NUM_EXTRA, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(bundle).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, int pageNum) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putInt(PAGE_NUM_EXTRA, pageNum);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        configurePeriodicSync(context, (int) SYNC_INTERVAL_SEC, (int)SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        //syncImmediately(context,1); Todo
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }



}
