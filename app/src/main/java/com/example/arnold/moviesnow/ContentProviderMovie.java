package com.example.arnold.moviesnow;

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

import com.example.arnold.moviesnow.ContentProviderMovieContract.FavoriteMovieList;

/**
 * Created by Arnold on 3/23/2016.
 */
public class ContentProviderMovie extends ContentProvider {

    private static final int FAVORITE_MOVIE_LIST = 1;
    private static final int FAVORITE_MOVIE_ID = 2;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, "favorite_movie_list", FAVORITE_MOVIE_LIST);
        URI_MATCHER.addURI(ContentProviderMovieContract.AUTHORITY, "favorite_movie_list/#", FAVORITE_MOVIE_ID);

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

        switch (URI_MATCHER.match(uri))
        {
            case FAVORITE_MOVIE_LIST:
                return FavoriteMovieList.CONTENT_TYPE;
            case FAVORITE_MOVIE_ID:
                return FavoriteMovieList.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch (URI_MATCHER.match(uri))
        {
            case FAVORITE_MOVIE_LIST:
                builder.setTables(ContentProviderMovieDbSchema.TBL_FAVORITE_MOVIE_LIST);
                if (TextUtils.isEmpty(sortOrder)){
                    sortOrder = FavoriteMovieList.SORT_ORDER_DEFAULT;
                }

                break;
            case FAVORITE_MOVIE_ID:
                builder.setTables(ContentProviderMovieDbSchema.TBL_FAVORITE_MOVIE_LIST);
                builder.appendWhere(FavoriteMovieList._ID + " = " + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        if ( URI_MATCHER.match(uri) != FAVORITE_MOVIE_LIST )
        {
            throw new IllegalArgumentException("Unsupported URI for insert: " + uri);
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();

        if (URI_MATCHER.match(uri) == FAVORITE_MOVIE_LIST )
        {
            long id = db.insert(ContentProviderMovieDbSchema.TBL_FAVORITE_MOVIE_LIST, null, contentValues);
            return getUriForId(id, uri);
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int deleteCount = 0;

        switch( URI_MATCHER.match(uri))
        {
            case FAVORITE_MOVIE_ID:
                String idStr = uri.getLastPathSegment();
                String where = FavoriteMovieList._ID + " = " + idStr;

                if ( !TextUtils.isEmpty(selection)){
                    where += " AND " + selection;
                }

                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_FAVORITE_MOVIE_LIST,  where, selectionArgs);
                break;

            case FAVORITE_MOVIE_LIST:

                deleteCount = db.delete(ContentProviderMovieDbSchema.TBL_FAVORITE_MOVIE_LIST,  selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI for delete: " + uri);
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

        switch( URI_MATCHER.match(uri))
        {
            case FAVORITE_MOVIE_ID:
                String idStr = uri.getLastPathSegment();
                String where = FavoriteMovieList._ID + " = " + idStr;

                if ( !TextUtils.isEmpty(selection)){
                    where += " AND " + selection;
                }

                updateCount = db.update(ContentProviderMovieDbSchema.TBL_FAVORITE_MOVIE_LIST, contentValues, where, selectionArgs);
                break;

            case FAVORITE_MOVIE_LIST:

                updateCount = db.update(ContentProviderMovieDbSchema.TBL_FAVORITE_MOVIE_LIST, contentValues, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI for update: " + uri);
        }

        if (updateCount > 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateCount;
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
