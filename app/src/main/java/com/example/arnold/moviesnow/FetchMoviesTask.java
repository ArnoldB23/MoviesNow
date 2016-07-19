package com.example.arnold.moviesnow;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Arnold on 1/10/2016.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieObj>>
{
    private static final String LOG_TAG = "FetchMoviesTask";
    private final String apiKey = "8015a641cd699d78b022a89c90c96da9"; //Insert api key here!!
    private ArrayAdapter<MovieObj> mMovieObjArrayAdapter;
    public static final String TOP_RATED = "Top Rated";
    public static final String POPULAR = "Most Popular";
    public static final String UPCOMING = "Upcoming";
    public static final String NOW_PLAYING = "Now Playing";
    public static final String MY_FAVORITES = "My Favorites";
    public static final int PAGE_LIMIT = 3;

    public FetchMoviesTask (ArrayAdapter<MovieObj> m)
    {
        mMovieObjArrayAdapter = m;
    }

    @Override
    protected void onPostExecute(ArrayList<MovieObj> movies) {



        if (movies == null)
            return;

        //mMovieObjArrayAdapter.setNotifyOnChange(false);
        mMovieObjArrayAdapter.clear();
        mMovieObjArrayAdapter.addAll(movies);
        MovieListSingleton.get(mMovieObjArrayAdapter.getContext()).updateMovieObjArrayListWithFavorites();

        Log.d(LOG_TAG, "movies number: " + String.valueOf(mMovieObjArrayAdapter.getCount()));
        mMovieObjArrayAdapter.notifyDataSetChanged();

    }



    @Override
    protected ArrayList<MovieObj> doInBackground(String... query)
    {


        String specificQuery = query[0].toString();
        Uri.Builder baseUribuilder = new Uri.Builder();
        Uri.Builder searchUriBuilder;
        String resultHttpRequest;
        ArrayList<MovieObj> pageList = new ArrayList<MovieObj>();

        baseUribuilder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie");


        switch (specificQuery)
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
            case MY_FAVORITES:
                return null;

        }

        baseUribuilder.appendQueryParameter("api_key", apiKey);




        for (int i = 1; i <= PAGE_LIMIT; i++)
        {
            searchUriBuilder = baseUribuilder.build().buildUpon();
            searchUriBuilder.appendQueryParameter("page", String.valueOf(i));

            resultHttpRequest = FetchMovieDetailsTask.sendHttpRequest(searchUriBuilder.build().toString());

            try {
                pageList.addAll( getMovieObjListFromJson(resultHttpRequest) );
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG, e.toString());
            }

        }

        return pageList;
    }





    private ArrayList<MovieObj> getMovieObjListFromJson(String movieJsonStr)
            throws JSONException
    {
        ArrayList<MovieObj> movieObjBuffer = new ArrayList<MovieObj>();

        JSONObject movieRootJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieRootJson.getJSONArray("results");

        for(int i = 0; i < movieArray.length(); i++)
        {
            JSONObject movieJson = movieArray.getJSONObject(i);

            MovieObj movie = new MovieObj();


            movie.poster_path = movieJson.getString("poster_path");
            movie.poster_path = movie.poster_path.substring(1);
            movie.original_title = movieJson.getString("original_title");
            movie.overview = movieJson.getString("overview");
            movie.release_date = movieJson.getString("release_date");
            movie.vote_average = movieJson.getString("vote_average");
            movie.id = movieJson.getString("id");
            movie.backdrop_path = movieJson.getString("backdrop_path");
            movie.backdrop_path = movie.backdrop_path.substring(1);


            movieObjBuffer.add(movie);
            //movie.LogCurrentState();
        }

        if (movieObjBuffer.isEmpty())
            return null;

        return movieObjBuffer;
    }

}