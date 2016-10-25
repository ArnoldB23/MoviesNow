package com.example.arnold.moviesnow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.arnold.moviesnow.sync.MoviesNowSyncAdapter;

/**
 * Created by Arnold on 4/1/2016.
 */
public class ContentProviderMovieOpenHelper extends SQLiteOpenHelper {

    private static final  String NAME = ContentProviderMovieDbSchema.DB_NAME;
    private static final int VERSION = 1;

    public ContentProviderMovieOpenHelper(Context c)
    {
        super(c, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_CREATE_TBL_MOVIES);
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_CREATE_TBL_MOVIE_LISTS);
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_CREATE_TBL_MOVIES_TO_LISTS);
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_CREATE_TBL_REVIEWS);
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_CREATE_TBL_TRAILERS);

        ContentValues cv = new ContentValues();
        cv.put(ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME, MoviesNowSyncAdapter.TOP_RATED);
        sqLiteDatabase.insert(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, null, cv);
        cv.put(ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME, MoviesNowSyncAdapter.NOW_PLAYING);
        sqLiteDatabase.insert(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, null, cv);
        cv.put(ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME, MoviesNowSyncAdapter.MY_FAVORITES);
        sqLiteDatabase.insert(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, null, cv);
        cv.put(ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME, MoviesNowSyncAdapter.POPULAR);
        sqLiteDatabase.insert(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, null, cv);
        cv.put(ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME, MoviesNowSyncAdapter.UPCOMING);
        sqLiteDatabase.insert(ContentProviderMovieDbSchema.TBL_MOVIE_LISTS, null, cv);


    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_DROP_TBL_MOVIES);
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_DROP_TBL_MOVIE_LISTS);
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_DROP_TBL_MOVIES_TO_LISTS);
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_DROP_TBL_REVIEWS);
        sqLiteDatabase.execSQL(ContentProviderMovieDbSchema.DDL_DROP_TBL_TRAILERS);
        onCreate(sqLiteDatabase);
    }
}
