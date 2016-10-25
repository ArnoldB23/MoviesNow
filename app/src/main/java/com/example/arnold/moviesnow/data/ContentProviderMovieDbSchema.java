/**
 * Created by Arnold on 4/1/2016.
 *
 */

package com.example.arnold.moviesnow.data;

public interface ContentProviderMovieDbSchema {

    String DB_NAME = "moviesprovider.db";

    String TBL_MOVIES = "Movies";
    String TBL_MOVIE_LISTS = "Movie_Lists";
    String TBL_MOVIES_TO_LISTS = "Movies_to_Lists";
    String TBL_REVIEWS = "Reviews";
    String TBL_TRAILERS = "Trailers";


    String DDL_CREATE_TBL_MOVIES =
            "CREATE TABLE " +  TBL_MOVIES +  " ( " +
                    ContentProviderMovieContract.Movies._ID + "      INTEGER PRIMARY KEY ON CONFLICT IGNORE, " +
                    ContentProviderMovieContract.Movies.COL_ORIGINAL_TITLE + "      TEXT, " +
                    ContentProviderMovieContract.Movies.COL_BACKDROP_PATH +  "      TEXT, " +
                    ContentProviderMovieContract.Movies.COL_OVERVIEW +       "      TEXT, " +
                    ContentProviderMovieContract.Movies.COL_POSTER_PATH +    "      TEXT, " +
                    ContentProviderMovieContract.Movies.COL_RELEASE_DATE +   "      TEXT, " +
                    ContentProviderMovieContract.Movies.COL_VOTE_AVERAGE +   "      TEXT" +
                     ")";

    String DDL_CREATE_TBL_MOVIE_LISTS =
            "CREATE TABLE " +  TBL_MOVIE_LISTS +  " ( " +
                    ContentProviderMovieContract.MovieLists._ID + "      INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ContentProviderMovieContract.MovieLists.COL_MOVIELIST_NAME + "      TEXT," +
                    ContentProviderMovieContract.MovieLists.COL_TOTAL_PAGES +    "      INTEGER" +
                    ")";

    String DDL_CREATE_TBL_MOVIES_TO_LISTS =
            "CREATE TABLE " +  TBL_MOVIES_TO_LISTS +  " ( " +
                    ContentProviderMovieContract.MoviesToLists._ID + "      INTEGER, " +
                    ContentProviderMovieContract.MoviesToLists.COL_PAGE_NUM + " TEXT," +
                    ContentProviderMovieContract.MoviesToLists.COL_UTC_TIMESTAMP + " TEXT," +
                    ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID + " INTEGER, " +
                    ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID + " INTEGER, " +
                    " FOREIGN KEY ( " + ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID + " ) REFERENCES " +
                    TBL_MOVIES + " ( " + ContentProviderMovieContract.Movies._ID + " ) ON DELETE CASCADE, " +
                    " FOREIGN KEY ( " + ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID + " ) REFERENCES " +
                    TBL_MOVIE_LISTS + " ( " + ContentProviderMovieContract.MovieLists._ID + " ) ON DELETE CASCADE, " +
                    " PRIMARY KEY ( " + ContentProviderMovieContract.MoviesToLists.COL_MOVIE_ID +
                    ", " + ContentProviderMovieContract.MoviesToLists.COL_MOVIELIST_ID + " ) " +
                    " ON CONFLICT REPLACE " +
                    ")";

    String DDL_CREATE_TBL_REVIEWS =
            "CREATE TABLE " +  TBL_REVIEWS +  " ( " +
                    ContentProviderMovieContract.Reviews._ID + "      INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ContentProviderMovieContract.Reviews.COL_REVIEW_COMMENT + "     TEXT NOT NULL, " +
                    ContentProviderMovieContract.Reviews.COL_REVIEW_USERNAME + "    TEXT NOT NULL, " +
                    ContentProviderMovieContract.Reviews.COL_MOVIE_ID + " INTEGER, " +
                    " FOREIGN KEY ( " + ContentProviderMovieContract.Reviews.COL_MOVIE_ID + " ) REFERENCES " +
                    TBL_MOVIES + " ( " + ContentProviderMovieContract.Movies._ID + " ) ON DELETE CASCADE " +
                    ")";

    String DDL_CREATE_TBL_TRAILERS =
            "CREATE TABLE " +  TBL_TRAILERS +  " ( " +
                    ContentProviderMovieContract.Trailers._ID + "      INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ContentProviderMovieContract.Trailers.COL_TRAILER_NAME + "      TEXT NOT NULL, " +
                    ContentProviderMovieContract.Trailers.COL_TRAILER_URL + "      TEXT NOT NULL, " +
                    ContentProviderMovieContract.Trailers.COL_MOVIE_ID + " INTEGER, " +
                    " FOREIGN KEY ( " + ContentProviderMovieContract.Trailers.COL_MOVIE_ID + " ) REFERENCES " +
                    TBL_MOVIES + " ( " + ContentProviderMovieContract.Movies._ID + " ) ON DELETE CASCADE " +
                    ")";

    String DDL_DROP_TBL_MOVIES = "DROP TABLE IF EXISTS " + TBL_MOVIES;
    String DDL_DROP_TBL_MOVIE_LISTS = "DROP TABLE IF EXISTS " + TBL_MOVIE_LISTS;
    String DDL_DROP_TBL_MOVIES_TO_LISTS = "DROP TABLE IF EXISTS " + TBL_MOVIES_TO_LISTS;
    String DDL_DROP_TBL_REVIEWS = "DROP TABLE IF EXISTS " + TBL_REVIEWS;
    String DDL_DROP_TBL_TRAILERS = "DROP TABLE IF EXISTS " + TBL_TRAILERS;

    String DML_WHERE_MOVIE_ID_CLAUSE = ContentProviderMovieContract.Movies._ID + " = ?";

    String DEFAULT_TBL_FAVORITE_MOVIE_LIST_SORT_ORDER = ContentProviderMovieContract.Movies._ID  + " ASC";



}
