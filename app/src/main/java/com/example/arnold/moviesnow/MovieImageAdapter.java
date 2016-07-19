package com.example.arnold.moviesnow;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Arnold on 10/29/2015.
 */
public class MovieImageAdapter extends ArrayAdapter<MovieObj>
{
    public static final String LOG_TAG = "MovieImageAdapter";
    public Context mContext;


    public MovieImageAdapter(ArrayList<MovieObj> list, Context c)
    {
        super(c, 0, list);
        mContext = c;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageViewHolder holder;

        if ( convertView == null )
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_image, null);
            holder = new ImageViewHolder();
            holder.mImageView = (ImageView) convertView.findViewById(R.id.list_item_imageView);
            convertView.setTag(holder);
        }

        MovieObj movie = getItem(position);

        String posterPath = constructPosterPath(movie.poster_path);

        //Log.d(LOG_TAG, "posterPath: " + posterPath);

        holder = (ImageViewHolder) convertView.getTag();


        //int measuredHeight = imageView.getMeasuredHeight();
        //int measuredWidth = imageView.getMeasuredWidth();

        //Log.d(LOG_TAG, "measuredHeight: " + measuredHeight + ". measuredWidth" + measuredWidth);

        //Log.d(LOG_TAG, movie.original_title + " No Image!!!");

        //Log.d(LOG_TAG, "getView position = " + position + " movie = " + movie.original_title );

        Picasso.with(mContext).load(posterPath).placeholder(R.drawable.poster_not_available).into(holder.mImageView);

        /*if(measuredHeight == 0)
        {
            GradientDrawable errorDrawable = new GradientDrawable();
            errorDrawable.setShape(GradientDrawable.RECTANGLE);
            errorDrawable.setSize(360,202);
            errorDrawable.setColor(Color.LTGRAY);
            errorDrawable.setStroke(1, Color.BLACK);
        }
        */


        return convertView;
    }

    private static class ImageViewHolder{
        public ImageView mImageView;
    }

    public static String constructPosterPath(String relativePath)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w342")
                .appendPath(relativePath);

        return builder.build().toString();
    }
}