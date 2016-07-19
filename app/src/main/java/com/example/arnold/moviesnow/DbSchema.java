package com.example.arnold.moviesnow;

import android.provider.BaseColumns;

/**
 * Created by Arnold on 4/1/2016.
 */
public interface DbSchema {

    String DB_NAME = "moviesprovider.db";

    String TBL_FAVORITE_MOVIE_LIST = "Favorite_Movie_List";

    String COL_ID = BaseColumns._ID;
    String COL_MOVIE_NAME = "movie_name";
    String COL_MOVIE_ID = "movie_id";

    String DDL_CREATE_TBL_FAVORITE_MOVIE_LIST =
            "CREATE TABLE " +  TBL_FAVORITE_MOVIE_LIST +  " ( " +
                     COL_ID +             "      INTEGER PRIMARY KEY AUTOINCREMENT, \n" +
                     COL_MOVIE_NAME + "      TEXT,\n" +
                     COL_MOVIE_ID +       "      TEXT UNIQUE NOT NULL\n" +
                     ")";
    String DDL_DROP_TBL_FAVORITE_MOVIE_LIST = "DROP TABLE IF EXISTS " + TBL_FAVORITE_MOVIE_LIST;

    String DML_WHERE_MOVIE_ID_CLAUSE = COL_MOVIE_ID + " = ?";

    String DEFAULT_TBL_FAVORITE_MOVIE_LIST_SORT_ORDER = COL_ID  + " ASC";


}
