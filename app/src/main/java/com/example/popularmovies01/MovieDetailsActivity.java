package com.example.popularmovies01;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovies01.data.MovieData;
import com.example.popularmovies01.utils.MovieDataUtils;
import com.example.popularmovies01.utils.NetworkUtils;

public class MovieDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent currentIntent = getIntent();
        int moviePositionInDataList = -1;

        if(currentIntent.hasExtra(MainActivity.EXTRA_KEY_MOVIE_POSITION))
        {
            moviePositionInDataList = currentIntent.getIntExtra(MainActivity.EXTRA_KEY_MOVIE_POSITION, -1);
        }

        // Either extra was not found or it was not set properly
        if(MovieDataUtils.GetMovieDataList() == null || moviePositionInDataList < 0 || moviePositionInDataList > MovieDataUtils.GetNumMoviesInDataList() - 1)
        {
            Toast.makeText(this, R.string.index_out_of_range, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        PopulateView(moviePositionInDataList);
    }

    private void PopulateView(int position)
    {
        MovieData selectedMovieData = MovieDataUtils.GetMovieDataAtPosition(position);

        // populate UI elements:
        ImageView moviePoster = (ImageView) findViewById(R.id.details_movie_poster_iv);

        TextView movieTitle = (TextView) findViewById(R.id.value_movie_title_tv);
        TextView releaseDate = (TextView) findViewById(R.id.value_release_date_tv);
        TextView userRating = (TextView) findViewById(R.id.value_user_rating_tv);
        TextView overview = (TextView) findViewById(R.id.value_overview_tv);

        // If any of the data is missing / us null - cancel populating view elements and go back to main activity.
        if(selectedMovieData != null)
        {
            NetworkUtils.PopulatePosterImageInImageView(selectedMovieData.GetPosterPath(), moviePoster, true);
            moviePoster.setContentDescription(R.string.poster_iv_content_description + selectedMovieData.GetMovieTitle());
            movieTitle.setText(selectedMovieData.GetMovieTitle());
            releaseDate.setText(selectedMovieData.GetReleaseDate());
            userRating.setText(String.valueOf(selectedMovieData.GetUserRating()));
            overview.setText(selectedMovieData.GetOverview());
        }
        else
        {
            Toast.makeText(this, R.string.movie_data_missing, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
