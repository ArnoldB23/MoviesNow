package com.example.arnold.moviesnow;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arnold on 9/28/2015.
 */
public class MovieObj implements Serializable{
    public String original_title;
    public String poster_path;
    public String overview;
    public String vote_average;
    public String release_date;
    public String backdrop_path;
    public String id;
    public ArrayList<TrailerInfo> mTrailerInfos;
    public ArrayList<UserReview> mUserReviews;
    private final String LOG_TAG = "MovieObj";
    public boolean favorite;

    public List<String> trailer_url_list;

    public MovieObj()
    {
        mTrailerInfos = new ArrayList<TrailerInfo>();
        mUserReviews = new ArrayList<UserReview>();
        favorite = false;
    }


    public List<UserReview> user_review_list;

    public class TrailerInfo
    {
        public String trailer_name;
        public String trailer_url;
    }

    public class UserReview
    {
        public String review_text;
        public String user_name;
    }

    public void LogCurrentState()
    {
        Log.d(LOG_TAG, "original_title: " + original_title
                + "\nposter_path: " + poster_path
                + "\noverview: " + overview
                + "\nvote_average: " + vote_average
                + "\nrelease_date: " + release_date
                + "\nbackdrop_path: " + backdrop_path
                + "\nid: " + id);

        for (int i = 0; i < mTrailerInfos.size(); i++)
        {
            Log.d(LOG_TAG, "TrailerInfo: " + mTrailerInfos.get(i).trailer_url + "\n" + mTrailerInfos.get(i).trailer_name);
        }

        for (int i = 0; i < mUserReviews.size(); i++)
        {
            Log.d(LOG_TAG, "UserReviews: " + mUserReviews.get(i).user_name + "\n" + mUserReviews.get(i).review_text);
        }
    }


    @Override
    public String toString()
    {
        return original_title;
    }
}
