package com.example.arnold.moviesnow;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Arnold on 1/11/2016.
 */
public class TrailerRecyclerViewAdapter  extends RecyclerView.Adapter<TrailerRecyclerViewAdapter.TrailerViewHolder>{

    private final String LOG_TAG = "TrailerRecyclerViewAdap";
    public ArrayList<MovieObj.TrailerInfo> mTrailers;
    private Context mContext;

    public TrailerRecyclerViewAdapter(Context context, ArrayList<MovieObj.TrailerInfo> trailers)
    {
        mContext = context;
        mTrailers = trailers;

    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_list_item, parent, false);

        TrailerViewHolder trailerHolder = new TrailerViewHolder( mContext, rootView );

        if (rootView == null)
            Log.d(LOG_TAG, "onCreateViewHolder: rootView is null!" );

        return trailerHolder;
    }

    @Override
    public void onBindViewHolder (TrailerViewHolder holder, int position)
    {
        holder.mUrl = mTrailers.get(position).trailer_url;
        holder.mTrailerNameTextView.setText(mTrailers.get(position).trailer_name);

        //Log.d(LOG_TAG, "onBindViewHolder: holder.mTrailerNameTextView= " + holder.mTrailerNameTextView.getText());
    }

    @Override
    public int getItemCount()
    {
        return mTrailers.size();
    }


    public void updateTrailerInfo(ArrayList<MovieObj.TrailerInfo> trailers)
    {
        mTrailers.clear();
        mTrailers.addAll(trailers);
        notifyDataSetChanged();
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
