package com.example.arnold.moviesnow;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Arnold on 10/11/2016.
 */
public class MovieCursorAdapter extends CursorAdapter{

    public static final String LOG_TAG = "MovieCursorAdapter";
    public Context mContext;

    public MovieCursorAdapter (Context context, Cursor c, int flags){
        super (context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_image, parent, false);

        ImageViewHolder imageViewHolder = new ImageViewHolder();
        imageViewHolder.mImageView = (ImageView)view.findViewById(R.id.list_item_imageView);
        view.setTag(imageViewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageViewHolder imageViewHolder = (ImageViewHolder)view.getTag();

        String poster_path = cursor.getString(MovieGridFragment.POSTER_PATH_COL_INDEX);

        String posterPath = constructPosterPath(poster_path);
        Picasso.with(mContext).load(posterPath).placeholder(R.drawable.poster_not_available).into(imageViewHolder.mImageView);

        //Log.d(LOG_TAG, "bindView: " + posterPath);

    }

    public static class ImageViewHolder{
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
