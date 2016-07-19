package com.example.arnold.moviesnow;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Arnold on 3/11/2016.
 */
public class FetchMovieDetailsTask extends AsyncTask<ArrayList <MovieObj>, Void, ArrayList<MovieObj>> {

    private final static String LOG_TAG = "FetchMovieDetailsTask";
    private FetchMovieDetailsPostExecute mCallback;
    private final String apiKey = "8015a641cd699d78b022a89c90c96da9"; //Insert api key here!!



    public FetchMovieDetailsTask (FetchMovieDetailsPostExecute callback)
    {
        mCallback = callback;
    }

    @Override
    protected ArrayList<MovieObj> doInBackground (ArrayList <MovieObj>... movies)
    {
        ArrayList <MovieObj> originalListMovieObj = movies[0];

        MovieObj resultMovieObj = null;
        String resultUrlRequest = null;

        ArrayList <MovieObj> dataFilledMovieObj = new ArrayList<MovieObj>();

        for ( int i = 0; i < originalListMovieObj.size() ; i++)
        {

            Uri.Builder UriBuilder = new Uri.Builder();

            UriBuilder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(originalListMovieObj.get(i).id)
                    .appendQueryParameter("api_key", apiKey)
                    .appendQueryParameter("append_to_response", "trailers,reviews");


            String Url = UriBuilder.build().toString();
            Log.d(LOG_TAG, "Review URL: " + Url);


            resultUrlRequest = sendHttpRequest(Url);


            try{
                resultMovieObj = createMovieObjFromJson(resultUrlRequest);
                dataFilledMovieObj.add(resultMovieObj);
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG, "JSON Parsing", e);
            }
        }



        return dataFilledMovieObj;


    }



    @Override
    protected void onPostExecute (ArrayList <MovieObj> movies)
    {
        //movieObj.LogCurrentState();
        mCallback.postExecute(movies);
    }

    public static String sendHttpRequest(String myUrl)
    {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String receivedJsonStr = null;

        try{

            URL url = new URL(myUrl);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();



            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            receivedJsonStr = buffer.toString();
            //Log.d(LOG_TAG, "Received JSON String: " + receivedJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }


        return receivedJsonStr;
    }


    private MovieObj createMovieObjFromJson(String movieJsonStr) throws JSONException
    {
        MovieObj movieObj = new MovieObj();

        JSONObject rootJsonObject = new JSONObject(movieJsonStr);

        movieObj.poster_path = rootJsonObject.getString("poster_path");
        movieObj.poster_path = movieObj.poster_path.substring(1);
        movieObj.original_title = rootJsonObject.getString("original_title");
        movieObj.overview = rootJsonObject.getString("overview");
        movieObj.release_date = rootJsonObject.getString("release_date");
        movieObj.vote_average = rootJsonObject.getString("vote_average");
        movieObj.id = rootJsonObject.getString("id");
        movieObj.backdrop_path = rootJsonObject.getString("backdrop_path");
        movieObj.backdrop_path = movieObj.backdrop_path.substring(1);


        JSONObject trailersJsonObject = rootJsonObject.getJSONObject("trailers");
        JSONArray trailersJsonArray = trailersJsonObject.getJSONArray("youtube");

        for (int j = 0; j < trailersJsonArray.length(); j++ )
        {
            JSONObject trailerJsonObject = trailersJsonArray.getJSONObject(j);
            MovieObj.TrailerInfo trailer = movieObj.new TrailerInfo();
            trailer.trailer_name = trailerJsonObject.getString("name");
            trailer.trailer_url = trailerJsonObject.getString("source");
            trailer.trailer_url = "https://www.youtube.com/watch?v=" + trailer.trailer_url;

            movieObj.mTrailerInfos.add(trailer);
        }

        JSONObject reviewsJsonObject = rootJsonObject.getJSONObject("reviews");
        JSONArray reviewsJsonArray = reviewsJsonObject.getJSONArray("results");

        for (int k = 0; k < reviewsJsonArray.length(); k++)
        {
            JSONObject reviewJsonObject = reviewsJsonArray.getJSONObject(k);
            MovieObj.UserReview review = movieObj.new UserReview();

            review.review_text = reviewJsonObject.getString("content");
            review.user_name = reviewJsonObject.getString("author");

            movieObj.mUserReviews.add(review);
        }




        return movieObj;
    }
}
