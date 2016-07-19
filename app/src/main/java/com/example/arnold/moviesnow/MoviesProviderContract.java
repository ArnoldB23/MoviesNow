package com.example.arnold.moviesnow;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Arnold on 3/23/2016.
 */
public class MoviesProviderContract  {

    public static final String AUTHORITY = "com.example.arnold.moviesnow.favoritemovies";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class FavoriteMovieList implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MoviesProviderContract.CONTENT_URI, "favorite_movie_list");

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/movies.favorite_movie_list";

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/movies.favorite_movie_list";

        public static final String MOVIE_NAME = "movie_name";
        public static final String MOVIE_ID = "movie_id";

        public static final String SORT_ORDER_DEFAULT = BaseColumns._ID + " ASC";
    }

}
