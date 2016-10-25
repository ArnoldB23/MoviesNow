package com.example.arnold.moviesnow;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Arnold on 3/14/2016.
 */
public class ReviewRecyclerViewAdapter extends CursorRecyclerViewAdapter<ReviewRecyclerViewAdapter.ReviewViewHolder> {




    public ReviewRecyclerViewAdapter (Context context, Cursor cursor)
    {
        super(context,cursor);
    }

    @Override
    public ReviewViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list_item, null);

        ReviewViewHolder reviewViewHolder = new ReviewViewHolder(rootView);
        return reviewViewHolder;
    }

    @Override
    public void onBindViewHolder (ReviewViewHolder holder, Cursor cursor)
    {
        holder.mReviewText.setText(cursor.getString(MovieDetailFragment.MOVIE_REVIEW_LOADER_COL_INDEX_USERNAME));
        holder.mReviewName.setText(cursor.getString(MovieDetailFragment.MOVIE_REVIEW_LOADER_COL_INDEX_COMMENT));
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
