package com.example.arnold.moviesnow.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;


/**
 * Created by Arnold on 3/23/2016.
 */
public class ContentProviderMovie extends ContentProvider {
    private static final String LOG_TAG = "ContentProviderMovie";

    private static final int URI_MATCH_MOVIES = 1;
    private static final int URI_MATCH_MOVIES_ID = 2;
    private static final int URI_MATCH_MOVIES_LISTNAME = 3;
    private static final int URI_MATCH_MOVIELISTS = 4;
    private static final int URI_MATCH_MOVIES_TO_LISTS = 5;
    private static final int URI_MATCH_MOVIES_TO_LISTS_LISTNAME = 6;
    private static final int URI_MATCH_REVIEWS = 7;
    private static final int URI_MATCH_TRAILERS = 8;

    private static final UriMatcher sURI_MATCHER;
    private static final SQLiteQueryBuilder sMoviesWithListNamesQueryBuilder;
    public static final String sMoviesJoinMoviesToListTableString = ContentProviderMovieDbSchema.TBL_MOVIES + " INNER JOIN " +
            ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS +
            " ON " + ContentProviderMovieDbSchema.TBL_MOVIES +
            "." + ContentProviderMovieContract.Movies._ID +
            " = " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS +
            "." + ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID + " INNER JOIN " +
            ContentProviderMovieDbSchema.TBL_MOVIE_LISTS +
            " ON " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS +
            "." + ContentProviderMovieContract.MovieLists._ID +
            " = " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS +
            "." + ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID;


    static {
        sURI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        sURI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, ContentProviderMovieContract.PATH_MOVIES, URI_MATCH_MOVIES);
        sURI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, ContentProviderMovieContract.PATH_MOVIES + "/#", URI_MATCH_MOVIES_ID);
        sURI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, ContentProviderMovieContract.PATH_MOVIES + "/*", URI_MATCH_MOVIES_LISTNAME);
        sURI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, ContentProviderMovieContract.PATH_MOVIELISTS, URI_MATCH_MOVIELISTS);
        sURI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, ContentProviderMovieContract.PATH_MOVIES_TO_LISTS, URI_MATCH_MOVIES_TO_LISTS);
        sURI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, ContentProviderMovieContract.PATH_MOVIES_TO_LISTS + "/*", URI_MATCH_MOVIES_TO_LISTS_LISTNAME);
        sURI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, ContentProviderMovieContract.PATH_REVIEWS, URI_MATCH_REVIEWS);
        sURI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, ContentProviderMovieContract.PATH_TRAILERS, URI_MATCH_TRAILERS);

        sMoviesWithListNamesQueryBuilder = new SQLiteQueryBuilder();




        // SELECT * FROM Movies INNER JOIN MoviesToLists ON Movies._ID = MoviesToLists.Movie_ID INNER JOIN MovieLists ON MovieLists._ID = MoviesToLists.MovieListID
        sMoviesWithListNamesQueryBuilder.setTables(sMoviesJoinMoviesToListTableString);



    }

    private ContentProviderMovieOpenHelper mHelper = null;

    @Override
    public boolean onCreate() {

        mHelper = new ContentProviderMovieOpenHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (sURI_MATCHER.match(uri))
        {
            case URI_MATCH_MOVIES:
                return ContentProviderMovieContract.Movies.CONTENT_TYPE;
            case URI_MATCH_MOVIES_ID:
                return ContentProviderMovieContract.Movies.CONTENT_ITEM_TYPE;
            case URI_MATCH_MOVIES_LISTNAME:
                return ContentProviderMovieContract.Movies.CONTENT_TYPE;
            case URI_MATCH_MOVIELISTS:
                return ContentProviderMovieContract.MovieLists.CONTENT_TYPE;
            case URI_MATCH_MOVIES_TO_LISTS:
                return ContentProviderMovieContract.MoviesToLists.CONTENT_TYPE;
            case URI_MATCH_MOVIES_TO_LISTS_LISTNAME:
                return ContentProviderMovieContract.MoviesToLists.CONTENT_TYPE;
            case URI_MATCH_REVIEWS:
                return ContentProviderMovieContract.Reviews.CONTENT_TYPE;
            case URI_MATCH_TRAILERS:
                return ContentProviderMovieContract.Trailers.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        String where;
        Cursor cursor;

        switch (sURI_MATCHER.match(uri))
        {
            case URI_MATCH_MOVIES:
                builder.setTables(ContentProviderMovieDbSchema.TBL_MOVIES);
                if (TextUtils.isEmpty(sortOrder)){
                    sortOrder = ContentProviderMovieContract.Movies.SORT_ORDER_DEFAULT;
                }

                cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case URI_MATCH_MOVIES_ID:
                builder.setTables(ContentProviderMovieDbSchema.TBL_MOVIES);

                where = ContentProviderMovieContract.Movies._ID + " = ?";

                cursor = builder.query(db, projection, where, new String[] {uri.getLastPathSegment()}, null, null, sortOrder);
                break;

            case URI_MATCH_MOVIELISTS:
                builder.setTables(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS);
                cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case URI_MATCH_MOVIES_TO_LISTS:
                builder.setTables(ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS);
                cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case URI_MATCH_MOVIES_TO_LISTS_LISTNAME:

                Log.d(LOG_TAG, "Query, URI_MATCH_MOVIES_TO_LISTS...");

                String listname = uri.getLastPathSegment();
                where = ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = \"" + listname + "\" ";

                if ( !TextUtils.isEmpty(selection)){
                    where += " AND " + selection;
                }


                cursor = sMoviesWithListNamesQueryBuilder.query(db, projection, where, selectionArgs, null, null, sortOrder);


                Log.d(LOG_TAG, "Query, URI_MATCH_MOVIES_TO_LISTS count = " + cursor.getCount());

                break;
            case URI_MATCH_TRAILERS:
                builder.setTables(ContentProviderMovieDbSchema.TBL_TRAILERS);
                cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case URI_MATCH_REVIEWS:
                builder.setTables(ContentProviderMovieDbSchema.TBL_REVIEWS);
                cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }


        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sURI_MATCHER.match(uri);
        final SQLiteDatabase db = mHelper.getReadableDatabase();
        long id = 0;

        switch (sURI_MATCHER.match(uri))
        {
            case URI_MATCH_MOVIES: {
                id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIES, null, contentValues);
                break;
            }

            case URI_MATCH_MOVIES_ID: {

                contentValues.put(ContentProviderMovieContract.Movies._ID, uri.getLastPathSegment());
                id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIES, null, contentValues);
                break;
            }
            case URI_MATCH_MOVIELISTS: {
                id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, null, contentValues);
                break;
            }
            case URI_MATCH_MOVIES_TO_LISTS: {
                id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS, null, contentValues);
                break;
            }
            case URI_MATCH_MOVIES_TO_LISTS_LISTNAME: {

                String listname = uri.getLastPathSegment();
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS);
                String where = ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = ?";
                Cursor cursor = builder.query(db,new String []{ContentProviderMovieContract.MovieLists._ID}, where, new String []{listname},null,null,null);
                String listID;

                if (! cursor.moveToFirst() )
                {
                    return null;
                }

                listID = cursor.getString(0);
                contentValues.put(ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID, listID);

                id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS, null, contentValues);
                break;
            }
            case URI_MATCH_TRAILERS: {
                id = db.insert(ContentProviderMovieDbSchema.TBL_TRAILERS, null, contentValues);
                break;
            }
            case URI_MATCH_REVIEWS: {
                id = db.insert(ContentProviderMovieDbSchema.TBL_REVIEWS, null, contentValues);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (id > 0 )
        {
            return getUriForId(id, uri);
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int deleteCount = 0;

        switch (sURI_MATCHER.match(uri))
        {
            case URI_MATCH_MOVIES: {
                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_MOVIES, selection, selectionArgs);
                break;
            }

            case URI_MATCH_MOVIES_ID:{
                String where = ContentProviderMovieContract.Movies._ID + " = ?";

                if ( !TextUtils.isEmpty(selection)){
                    where += " AND " + selection;
                }

                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_MOVIES, where, new String [] {uri.getLastPathSegment()} );
                break;
            }

            case URI_MATCH_MOVIES_LISTNAME:{

                /*
                String where = ContentProviderMovieContract.Movies._ID + " IN "
                        + "(SELECT " + ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies._ID
                        + " FROM " + sMoviesJoinMoviesToListTableString
                        + " WHERE " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = \"" + uri.getLastPathSegment()
                        + "\") ";
                */

                String where = ContentProviderMovieContract.Movies._ID + " = "
                        + " (SELECT "
                        + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID
                        + " FROM " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS
                        + " INNER JOIN " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS
                        + " ON " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists._ID
                        + " = " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID
                        + " WHERE " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = \"" + uri.getLastPathSegment() + "\") ";




                if ( !TextUtils.isEmpty(selection)){
                    where += " AND " + selection;
                }

                //Log.d(LOG_TAG, "delete URI_MATCH_MOVIES_TO_LISTS_LISTNAME, where = " + where);

                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_MOVIES, where, selectionArgs);
                break;
            }
            case URI_MATCH_MOVIELISTS: {
                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, selection, selectionArgs);
                break;
            }
            case URI_MATCH_MOVIES_TO_LISTS: {
                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS, selection, selectionArgs);
                break;
            }

            case URI_MATCH_MOVIES_TO_LISTS_LISTNAME: {

                /*
                String where = ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID + " IN "
                        + "(SELECT " + ContentProviderMovieDbSchema.TBL_MOVIES + "." + ContentProviderMovieContract.Movies._ID
                        + " FROM " + sMoviesJoinMoviesToListTableString
                        + " WHERE " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = \"" + uri.getLastPathSegment()
                        + "\") ";

                */

                String where = ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID + " = "
                        + " (SELECT "
                        + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID
                        + " FROM " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS
                        + " INNER JOIN " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS
                        + " ON " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists._ID
                        + " = " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID
                        + " WHERE " + ContentProviderMovieDbSchema.TBL_MOVIE_LISTS + "." + ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = \"" + uri.getLastPathSegment() + "\") ";


                if ( !TextUtils.isEmpty(selection)){
                    where += " AND " + selection;
                }

                Log.d(LOG_TAG, "delete URI_MATCH_MOVIES_TO_LISTS_LISTNAME: DELETE FROM " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + " where " + where);

                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS, where, selectionArgs);

                break;
            }
            case URI_MATCH_TRAILERS: {
                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_TRAILERS, selection, selectionArgs);
                break;
            }
            case URI_MATCH_REVIEWS: {
                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_REVIEWS, selection, selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }



        if (deleteCount > 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int updateCount = 0;

        switch (sURI_MATCHER.match(uri))
        {
            case URI_MATCH_MOVIES: {
                updateCount = db.update(ContentProviderMovieDbSchema.TBL_MOVIES, contentValues, selection, selectionArgs);
                break;
            }

            case URI_MATCH_MOVIES_ID:{
                String where = ContentProviderMovieContract.Movies._ID + " = ?";

                updateCount = db.update(ContentProviderMovieDbSchema.TBL_MOVIES, contentValues, where, new String[]{uri.getLastPathSegment()});
                break;
            }
            case URI_MATCH_MOVIELISTS: {
                updateCount = db.update(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, contentValues, selection, selectionArgs);
                break;
            }
            case URI_MATCH_MOVIES_TO_LISTS: {
                updateCount = db.update(ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS, contentValues, selection, selectionArgs);
                break;
            }
            case URI_MATCH_TRAILERS: {
                updateCount = db.update(ContentProviderMovieDbSchema.TBL_TRAILERS, contentValues, selection, selectionArgs);
                break;
            }
            case URI_MATCH_REVIEWS: {
                updateCount = db.update(ContentProviderMovieDbSchema.TBL_REVIEWS, contentValues, selection, selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (updateCount > 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int match = sURI_MATCHER.match(uri);
        int returnCount = 0;

        switch (match) {
            case URI_MATCH_MOVIES: {
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIES, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            case URI_MATCH_MOVIELISTS: {
                db.beginTransaction();

                try {
                    for (ContentValues value : values){
                        long _id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            }
            case URI_MATCH_MOVIES_TO_LISTS: {
                db.beginTransaction();

                try {
                    for (ContentValues value : values){

                        long _id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            }

            case URI_MATCH_MOVIES_TO_LISTS_LISTNAME: {
                db.beginTransaction();

                //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_MOVIES_TO_LISTS_LISTNAME");

                try{
                    String listname = uri.getLastPathSegment();
                    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                    builder.setTables(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS);
                    String where = ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + " = ?";
                    Cursor cursor = builder.query(db,new String []{ContentProviderMovieContract.MovieLists._ID}, where, new String []{listname},null,null,null);
                    String listID;



                    if (! cursor.moveToFirst() )
                    {
                        Log.d(LOG_TAG, "bulkInsert, URI_MATCH_MOVIES_TO_LISTS_LISTNAME, cannot find listID for " + listname);
                        return 0;
                    }

                    listID = cursor.getString(0);

                    //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_MOVIES_TO_LISTS_LISTNAME, listID = " + listID);

                    for (ContentValues value : values){

                        value.put(ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID, listID);
                        long _id = db.insert(ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS, null, value);
                        //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_MOVIES_TO_LISTS_LISTNAME, after movies_To_lists insert movieId = " + _id);
                        if (_id != -1){
                            returnCount++;
                        }


                    }

                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }


            case URI_MATCH_REVIEWS: {
                db.beginTransaction();

                //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_REVIEWS...");

                try {
                    for (ContentValues value : values){
                        long _id = db.insert(ContentProviderMovieDbSchema.TBL_REVIEWS, null, value);
                        //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_REVIEWS reviews: " + value.getAsString(ContentProviderMovieContract.Reviews.COL_REVIEW_USERNAME));
                        //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_REVIEWS reviews: " + value.getAsString(ContentProviderMovieContract.Reviews.COL_REVIEW_COMMENT));
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            }

            case URI_MATCH_TRAILERS: {
                db.beginTransaction();
                //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_TRAILERS...");
                try {
                    for (ContentValues value : values){

                        //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_TRAILERS: trailers = " + value.getAsString(ContentProviderMovieContract.Trailers.COL_TRAILER_NAME));
                        //Log.d(LOG_TAG, "bulkInsert, URI_MATCH_TRAILERS: trailers = " + value.getAsString(ContentProviderMovieContract.Trailers.COL_TRAILER_URL));
                        long _id = db.insert(ContentProviderMovieDbSchema.TBL_TRAILERS, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            }


            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);

            // notify all listeners of changes:
            getContext().
                    getContentResolver().
                    notifyChange(itemUri, null);

            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException(
                "Problem while inserting into uri: " + uri);
    }

}
