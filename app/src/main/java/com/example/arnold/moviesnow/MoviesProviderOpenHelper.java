package com.example.arnold.moviesnow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Arnold on 4/1/2016.
 */
public class MoviesProviderOpenHelper extends SQLiteOpenHelper {

    private static final  String NAME = DbSchema.DB_NAME;
    private static final int VERSION = 1;

    public MoviesProviderOpenHelper(Context c)
    {
        super(c, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DbSchema.DDL_CREATE_TBL_FAVORITE_MOVIE_LIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(DbSchema.DDL_DROP_TBL_FAVORITE_MOVIE_LIST);
        onCreate(sqLiteDatabase);
    }
}
