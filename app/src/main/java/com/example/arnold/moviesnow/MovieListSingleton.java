package com.example.arnold.moviesnow;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Arnold on 10/29/2015.
 */
public class MovieListSingleton {
    private Context mContext;
    private ArrayList<MovieObj> mMovieObjArrayList;

    private String mCurrentList;
    private static MovieListSingleton sMovieListSingleton;

    private MovieListSingleton(Context c)
    {
        mContext = c;
        mMovieObjArrayList = new ArrayList<MovieObj>();

    }

    public static MovieListSingleton get (Context c)
    {
        if (sMovieListSingleton == null)
        {
            sMovieListSingleton = new MovieListSingleton(c);
        }

        return sMovieListSingleton;
    }

    public ArrayList<MovieObj> getMovieObjArrayList()
    {
        return mMovieObjArrayList;
    }

    public MovieObj getMovieObj(int position)
    {
        if (position >= 0 && position < mMovieObjArrayList.size())
            return mMovieObjArrayList.get(position);
        else
            return null;
    }

    public MovieObj getMovieObj(String id)
    {
        for(int i = 0; i < mMovieObjArrayList.size(); i++)
        {
            if( mMovieObjArrayList.get(i).id.equals(id))
            {
                return mMovieObjArrayList.get(i);
            }
        }

        return null;
    }

    public void putMovieObj(MovieObj movie)
    {
        if (movie.id != null && !movie.id.isEmpty()){
            mMovieObjArrayList.add(movie);
        }
    }


    public void removeMovieObj(MovieObj movie)
    {
        for(int i = 0; i < mMovieObjArrayList.size(); i++)
        {
            if( mMovieObjArrayList.get(i).id.equals(movie.id))
            {
                mMovieObjArrayList.remove(i);
            }
        }
    }

    public void removeNonFavoritesFromMovieObjArrayList()
    {
        for(int i = 0; i < mMovieObjArrayList.size(); i++)
        {
            if( mMovieObjArrayList.get(i).favorite == false)
            {
                mMovieObjArrayList.remove(i);
            }
        }
    }

    public ArrayList<MovieObj> getFavoritesFromMovieObjArrayList()
    {
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] projection = new String[]{ContentProviderMovieContract.FavoriteMovieList.MOVIE_ID};
        Cursor cursor = contentResolver.query(ContentProviderMovieContract.FavoriteMovieList.CONTENT_URI, projection, null, null, null);

        ArrayList<MovieObj> favoriteMovieObjs = new ArrayList<MovieObj>();

        if (cursor.moveToFirst()){

            MovieObj favoriteMovieObj;

            do{
                String movieId = cursor.getString(0);

                favoriteMovieObj = getMovieObj(movieId);
                if(favoriteMovieObj != null && favoriteMovieObj.favorite == true){
                    favoriteMovieObjs.add(favoriteMovieObj);
                }

            }while(cursor.moveToNext());
        }

        cursor.close();

        return favoriteMovieObjs;
    }




    public void updateMovieObjArrayListWithFavorites()
    {
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] projection = new String[]{ContentProviderMovieContract.FavoriteMovieList.MOVIE_ID};
        Cursor cursor = contentResolver.query(ContentProviderMovieContract.FavoriteMovieList.CONTENT_URI, projection, null, null, null);

        if (cursor.moveToFirst()){

            MovieObj favoriteMovieObj;

            do{
                String movieId = cursor.getString(0);

                favoriteMovieObj = getMovieObj(movieId);
                if(favoriteMovieObj != null){
                    favoriteMovieObj.favorite = true;
                }

            }while(cursor.moveToNext());
        }

        cursor.close();
    }




    public String getCurrentListName() {
        return mCurrentList;
    }

    public void setCurrentListName(String currentList) {
        mCurrentList = currentList;
    }
}
