package com.example.arnold.moviesnow;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Arnold on 10/4/2015.
 */
public class MovieDetailFragment extends Fragment implements FetchMovieDetailsPostExecute {
    public static final String EXTRA_MOVIE = "Do it, just do it!";
    public RecyclerView mTrailerRecyclerView;
    public RecyclerView mReviewRecyclerView;
    public ReviewRecyclerViewAdapter mReviewRecyclerViewAdapter;
    public TrailerRecyclerViewAdapter mTrailerRecyclerViewAdapter;
    public TextView mTrailerSectionTitle;
    public TextView mReviewSectionTitle;
    public MovieObj mTrailersAndReviewsMovieObj;
    public ScrollView mScrollView;
    private static final String LOG_TAG = "MovieDetailFragment";
    public MovieObj movie;
    public int movie_position;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        movie_position = (int) getArguments().getInt(EXTRA_MOVIE);
        //movie = MovieListSingleton.get(getContext()).getMovieObj(movie_position);
        movie = MovieListSingleton.get(getContext()).getMovieObjArrayList().get(movie_position);
        final ContentResolver contentResolver = getContext().getContentResolver();


        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.movie_imageView);
        String posterPath = MovieImageAdapter.constructPosterPath(movie.backdrop_path);
        //Log.d(LOG_TAG, "backdrop_path = " + posterPath);


        GradientDrawable errorDrawable = new GradientDrawable();
        errorDrawable.setShape(GradientDrawable.RECTANGLE);
        errorDrawable.setSize(360, 202);
        errorDrawable.setColor(Color.LTGRAY);
        errorDrawable.setStroke(1, Color.BLACK);

        final String original_title_str = movie.original_title;

        Picasso.with(getActivity()).load(posterPath).error(errorDrawable).into(imageView);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, original_title_str + " showtimes");

                startActivity(intent);
                return true;
            }
        });

        final Button favoriteButton = (Button) rootView.findViewById(R.id.favorite_button);
        if ( movie.favorite == true)
        {
            //Drawable leftDrawable = getResources().getDrawable(R.drawable.ic_favorite_black_24dp);
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_black_24dp, 0, 0, 0);

        }
        else
        {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);
        }

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (movie.favorite == false) {
                    //Drawable leftDrawable = getResources().getDrawable(R.drawable.ic_favorite_black_24dp);
                    favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_black_24dp, 0, 0, 0);
                    movie.favorite = true;

                    ContentValues values = new ContentValues();
                    values.put(MoviesProviderContract.FavoriteMovieList.MOVIE_ID, movie.id);
                    values.put(MoviesProviderContract.FavoriteMovieList.MOVIE_NAME, movie.original_title);

                    contentResolver.insert(MoviesProviderContract.FavoriteMovieList.CONTENT_URI, values);


                } else {
                    favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);
                    movie.favorite = false;

                    String where = MoviesProviderContract.FavoriteMovieList.MOVIE_ID + " = " + movie.id;
                    contentResolver.delete(MoviesProviderContract.FavoriteMovieList.CONTENT_URI, where, null);

                }

            }
        });

        TextView originalTitle = (TextView) rootView.findViewById(R.id.original_title_textView);
        originalTitle.setText(movie.original_title);

        TextView overview = (TextView) rootView.findViewById(R.id.overview_textView);


        overview.setText(movie.overview);

        TextView voteAverage = (TextView) rootView.findViewById(R.id.vote_average_textView);
        voteAverage.setText(movie.vote_average + "/10");

        TextView releaseDate = (TextView) rootView.findViewById(R.id.release_date_textView);
        String dates [] = movie.release_date.split("-");
        String months [] = {"", "January","February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        releaseDate.setText("(" + months[Integer.valueOf(dates[1])] + " " + dates[0] + ")");

        mTrailersAndReviewsMovieObj = new MovieObj();

        mTrailerRecyclerView = (RecyclerView)rootView.findViewById(R.id.trailer_recycler_view);


        mReviewRecyclerView = (RecyclerView)rootView.findViewById(R.id.review_recycler_view);


        mTrailerSectionTitle = (TextView) rootView.findViewById(R.id.trailer_section_title);
        mReviewSectionTitle = (TextView) rootView.findViewById(R.id.review_section_title);

        mTrailerRecyclerViewAdapter = new TrailerRecyclerViewAdapter(getContext(), mTrailersAndReviewsMovieObj.mTrailerInfos);
        //mTrailerRecyclerView.setAdapter(mTrailerRecyclerViewAdapter);
        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTrailerRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        mTrailerRecyclerView.setNestedScrollingEnabled(false);
        //mTrailerRecyclerViewAdapter.mTrailers = (ArrayList<MovieObj.TrailerInfo>) movieObj.mTrailerInfos.clone();

        mTrailerRecyclerView.setAdapter(mTrailerRecyclerViewAdapter);


        mReviewRecyclerViewAdapter = new ReviewRecyclerViewAdapter(mTrailersAndReviewsMovieObj.mUserReviews);

        //mReviewRecyclerView.setAdapter(mReviewRecyclerViewAdapter);
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mReviewRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        mReviewRecyclerView.setNestedScrollingEnabled(false);

        mReviewRecyclerView.setAdapter(mReviewRecyclerViewAdapter);

        mScrollView = (ScrollView)rootView.findViewById(R.id.detail_scrollView);


        ArrayList<MovieObj> movieObjArrayList = new ArrayList<MovieObj>();
        movieObjArrayList.add(movie);


        //if (movie.mUserReviews.isEmpty() && movie.mTrailerInfos.isEmpty())
            new FetchMovieDetailsTask(this).execute(movieObjArrayList);
        //else
            //postExecute(movie);



        return rootView;
    }

    public static Fragment newInstance (int position)
    {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MOVIE, position);

        Fragment f = new MovieDetailFragment();
        f.setArguments(args);

        return f;
    }

    public void postExecute(ArrayList<MovieObj> movieObjs)
    {

        //movieObj.LogCurrentState();
        MovieObj movieObj = movieObjs.get(0);

        if(movieObj.mUserReviews.isEmpty())
        {
            mReviewSectionTitle.setVisibility(View.GONE);
        }
        else{
            mReviewRecyclerViewAdapter.mUserReviews.clear();
            mReviewRecyclerViewAdapter.mUserReviews.addAll( movieObj.mUserReviews );
            mReviewRecyclerViewAdapter.notifyDataSetChanged();

        }

        if (movieObj.mTrailerInfos.isEmpty())
        {
            mTrailerSectionTitle.setVisibility(View.GONE);
        }
        else{

            mTrailerRecyclerViewAdapter.mTrailers.clear();
            mTrailerRecyclerViewAdapter.mTrailers.addAll( movieObj.mTrailerInfos );
            mTrailerRecyclerViewAdapter.notifyDataSetChanged();
        }


    }

    @Override
    public void onResume()
    {
        super.onResume();

        Log.d(LOG_TAG, "onResume ");

        if(mScrollView != null){
            mScrollView.scrollTo(0, 0);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Log.d(LOG_TAG, "onPause ");

        //Intent result = new Intent();
        //result.putExtra(MovieGridFragment.POSITION_EXTRA, movie_position);
        //getActivity().setResult(Activity.RESULT_OK, result);

        //Log.d(LOG_TAG, "onPause: position = " + movie_position);

    }


}
