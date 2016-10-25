package com.example.arnold.moviesnow;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by Arnold on 1/11/2016.
 */
public class TrailerRecyclerViewAdapter  extends CursorRecyclerViewAdapter<TrailerRecyclerViewAdapter.TrailerViewHolder>{

    private final String LOG_TAG = "TrailerRecyclerViewAdap";

    private Context mContext;

    public TrailerRecyclerViewAdapter(Context context, Cursor cursor)
    {

        super(context,cursor);
        mContext = context;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.trailer_list_item, parent, false);

        TrailerViewHolder trailerHolder = new TrailerViewHolder( mContext, rootView );


        return trailerHolder;
    }

    @Override
    public void onBindViewHolder (TrailerViewHolder holder, Cursor cursor)
    {
        holder.mUrl = cursor.getString(MovieDetailFragment.MOVIE_TRAILER_LOADER_COL_INDEX_TRAILER_URL);
        holder.mTrailerNameTextView.setText(cursor.getString(MovieDetailFragment.MOVIE_TRAILER_LOADER_COL_INDEX_TRAILER_NAME));

        //Log.d(LOG_TAG, "onBindViewHolder: holder.mTrailerNameTextView= " + holder.mTrailerNameTextView.getText());
    }


    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final String LOG_TAG = "TrailerViewHolder";
        public TextView mTrailerNameTextView;
        public Context mContext;
        public String mUrl;

        public TrailerViewHolder(Context context, View itemView)
        {
            super(itemView);
            mContext = context;
            mTrailerNameTextView = (TextView) itemView.findViewById(R.id.trailer_name);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Uri weblink = Uri.parse(mUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, weblink);


            //Log.d(LOG_TAG, "weblink is " + weblink);

            if(intent.resolveActivity(mContext.getPackageManager()) != null){
                mContext.startActivity(intent);
               // Log.d(LOG_TAG, "Starting ACTION_VIEW activity");
            }

        }
    }
}
