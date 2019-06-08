package com.example.popularmovies01;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.popularmovies01.data.MovieData;
import com.example.popularmovies01.utils.MovieDataUtils;
import com.example.popularmovies01.utils.NetworkUtils;

import java.util.ArrayList;

// Since we are using ArrayAdapter, we will not be extending RecycleView
public class MovieDataArrayAdapter extends ArrayAdapter<MovieData> {

    private ArrayList<MovieData> mMovieListData;

    private MovieDataAdapterOnClickHandler mClickHandler;

    public interface MovieDataAdapterOnClickHandler{
        void onClick(MovieData movieData);
    }

    public MovieDataArrayAdapter(Activity context, ArrayList<MovieData> movieDataList) {
        super(context, 0, movieDataList);
        mMovieListData = movieDataList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieData movieData = MovieDataUtils.GetMovieDataAtPosition(position);
        @SuppressLint("ViewHolder") View rootView = LayoutInflater.from(getContext()).inflate(R.layout.movie_thumbnail, parent, false);

        if(movieData != null) {
            ImageView moviePosterView = (ImageView) rootView.findViewById(R.id.movie_thumbnail_iv);
            moviePosterView.setContentDescription(R.string.poster_iv_content_description + movieData.GetMovieTitle());

            // .with does not seem to work. Replaced with usage from 'https://square.github.io/picasso/'
            if (moviePosterView.getDrawable() == null) {
                NetworkUtils.PopulatePosterImageInImageView(movieData.GetPosterPath(), moviePosterView, false);
            }
        }

        return rootView;
    }

    @Override
    public int getCount() {
        return mMovieListData != null ? mMovieListData.size() : 0;
    }

    @Override
    public MovieData getItem(int position) {
        return mMovieListData.get(position);
    }
}
