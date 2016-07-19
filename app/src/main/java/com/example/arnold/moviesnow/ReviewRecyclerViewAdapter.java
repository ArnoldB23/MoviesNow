package com.example.arnold.moviesnow;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Arnold on 3/14/2016.
 */
public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder> {

    public ArrayList<MovieObj.UserReview> mUserReviews;


    public ReviewRecyclerViewAdapter (ArrayList<MovieObj.UserReview> userReviews)
    {
        mUserReviews = userReviews;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list_item, null);

        ReviewViewHolder reviewViewHolder = new ReviewViewHolder(rootView);
        return reviewViewHolder;
    }

    @Override
    public void onBindViewHolder (ReviewViewHolder holder, int position)
    {
        holder.mReviewText.setText(mUserReviews.get(position).review_text);
        holder.mReviewName.setText(mUserReviews.get(position).user_name);
    }

    @Override
    public int getItemCount()
    {
        return mUserReviews.size();
    }


    public class ReviewViewHolder extends RecyclerView.ViewHolder{

        public TextView mReviewName;
        public TextView mReviewText;


        public ReviewViewHolder (View itemView)
        {
            super(itemView);
            mReviewName = (TextView)itemView.findViewById(R.id.review_name);
            mReviewText = (TextView) itemView.findViewById(R.id.review_text);
        }

    }
}
