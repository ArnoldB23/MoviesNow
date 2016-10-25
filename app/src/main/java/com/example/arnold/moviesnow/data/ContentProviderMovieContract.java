package com.example.arnold.moviesnow.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Arnold on 3/23/2016.
 */
public class ContentProviderMovieContract {

    public static final String AUTHORITY = "com.example.arnold.moviesnow";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIELISTS = "movielists";
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_MOVIES_TO_LISTS = "movies_to_lists";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_TRAILERS = "trailers";

    public static final String MOVIES_WITHOUT_lISTNAMES_WHERE_CLAUSE = Movies._ID + " IN " +
            " (SELECT " + ContentProviderMovieDbSchema.TBL_MOVIES + "." + Movies._ID +
            " FROM " + ContentProviderMovieDbSchema.TBL_MOVIES +
            " LEFT JOIN " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS +
            " ON " + ContentProviderMovieDbSchema.TBL_MOVIES +
            "." + Movies._ID +
            " = " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS +
            "." + MoviesToLists.COL_MOVIE_ID +
            " WHERE " + ContentProviderMovieDbSchema.TBL_MOVIES_TO_LISTS + "." + MoviesToLists.COL_MOVIE_ID + " IS NULL)";

    public static final class MovieLists implements  BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIELISTS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIELISTS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIELISTS;

        public static final String COL_MOVIELIST_NAME = "movielist_name";
        public static final String COL_TOTAL_PAGES = "total_pages";


        public static final String SORT_ORDER_DEFAULT = BaseColumns._ID + " ASC";
    }

    public static final class Movies implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIES;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIES;

        public static final String COL_ORIGINAL_TITLE = "original_title";
        public static final String COL_POSTER_PATH = "poster_path";
        public static final String COL_OVERVIEW = "overview";
        public static final String COL_VOTE_AVERAGE = "vote_average";
        public static final String COL_RELEASE_DATE = "release_date";
        public static final String COL_BACKDROP_PATH = "backdrop_path";


        public static final String SORT_ORDER_DEFAULT = BaseColumns._ID + " ASC";

        public static Uri buildMovieUriWithID(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

        public static Uri buildMovieUriWithListName(String listname)
        {
            return  CONTENT_URI.buildUpon().appendPath(listname).build();
        }
    }

    public static final class MoviesToLists implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_TO_LISTS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIES_TO_LISTS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIES_TO_LISTS;

        public static final String COL_MOVIELIST_ID = "movielist_id";
        public static final String COL_MOVIE_ID = "movie_id";
        public static final String COL_PAGE_NUM = "page_num";
        public static final String COL_UTC_TIMESTAMP = "utc_timestamp";

        public static Uri buildMoviesToListsUriWithListName(String listname)
        {
            return CONTENT_URI.buildUpon().appendPath(listname).build();
        }

    }

    public static final class Trailers implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_TRAILERS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_TRAILERS;

        public static final String COL_MOVIE_ID = "movie_id";
        public static final String COL_TRAILER_NAME = "trailer_name";
        public static final String COL_TRAILER_URL = "trailer_url";

        public static final String SORT_ORDER_DEFAULT = BaseColumns._ID + " ASC";
    }

    public static final class Reviews implements  BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_REVIEWS;

        public static final String COL_MOVIE_ID = "movie_id";
        public static final String COL_REVIEW_USERNAME = "review_username";
        public static final String COL_REVIEW_COMMENT = "review_comment";

        public static final String SORT_ORDER_DEFAULT = BaseColumns._ID + " ASC";
    }

}
