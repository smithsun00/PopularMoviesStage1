package com.example.popularmovies01.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.popularmovies01.utils.BackgroundTasksUtils;
import com.example.popularmovies01.R;
import com.example.popularmovies01.data.MovieData;
import com.example.popularmovies01.utils.MovieDataUtils;

public class MovieListRecycleAdapter extends RecyclerView.Adapter<MovieListRecycleAdapter.MovieDataViewHolder> {

    private static int viewHolderCount = 0;
    private int mNumberMovies;

    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public MovieListRecycleAdapter(int numMovies, ListItemClickListener listener)
    {
        mOnClickListener = listener;
        mNumberMovies = numMovies;
        viewHolderCount = 0;
    }

    @NonNull
    @Override
    public MovieDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_thumbnail;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        MovieDataViewHolder viewHolder = new MovieDataViewHolder(view);

        MovieData movieData = MovieDataUtils.getInstance().GetMovieDataAtPosition(viewHolderCount);
        viewHolderCount++;

        // Fail safe: make sure index is not out of range:
        if(viewHolderCount >= MovieDataUtils.getInstance().GetNumMoviesInDataList())
        {
            viewHolderCount = 0;
        }

        if(!MovieDataUtils.IsOfflineMode) {
            viewHolder.moviePosterImageView.setVisibility(View.VISIBLE);
            viewHolder.offlinePosterReplacementTextView.setVisibility(View.INVISIBLE);
            BackgroundTasksUtils.PopulatePosterImageInImageView(movieData.getPosterPath(), viewHolder.moviePosterImageView, true);
        }else{
            // offline - load default image
            viewHolder.moviePosterImageView.setVisibility(View.INVISIBLE);
            viewHolder.offlinePosterReplacementTextView.setVisibility(View.VISIBLE);
            viewHolder.offlinePosterReplacementTextView.setText(movieData.getMovieTitle());
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieDataViewHolder movieDataViewHolder, int position) {
        // get movie data from movieDataRepository
        MovieData movieData = MovieDataUtils.getInstance().GetMovieDataAtPosition(position);
        movieDataViewHolder.bind(movieData);
    }

    @Override
    public int getItemCount() {
        return mNumberMovies;
    }

    class MovieDataViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        // Will display the position in the list, ie 0 through getItemCount() - 1
        ImageView moviePosterImageView;
        // Will display which ViewHolder is displaying this data
        TextView offlinePosterReplacementTextView;

        public MovieDataViewHolder(View itemView) {
            super(itemView);

            moviePosterImageView = (ImageView) itemView.findViewById(R.id.movie_thumbnail_iv);
            offlinePosterReplacementTextView = (TextView) itemView.findViewById(R.id.offline_mode_title_tv);

            itemView.setOnClickListener(this);
        }

        void bind(MovieData movieData) {
            if(movieData != null) {
                moviePosterImageView.setContentDescription(R.string.poster_iv_content_description + movieData.getMovieTitle());

                if(!MovieDataUtils.IsOfflineMode) {
                    moviePosterImageView.setVisibility(View.VISIBLE);
                    offlinePosterReplacementTextView.setVisibility(View.INVISIBLE);
                    BackgroundTasksUtils.PopulatePosterImageInImageView(movieData.getPosterPath(), moviePosterImageView, true);
                }else{
                    // offline - load default image
                    moviePosterImageView.setVisibility(View.INVISIBLE);
                    offlinePosterReplacementTextView.setVisibility(View.VISIBLE);
                    offlinePosterReplacementTextView.setText(movieData.getMovieTitle());
                }
            }
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}


