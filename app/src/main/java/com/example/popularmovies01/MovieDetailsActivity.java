package com.example.popularmovies01;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovies01.adapters.ReviewListAdapter;
import com.example.popularmovies01.adapters.TrailerListAdapter;
import com.example.popularmovies01.callbacks.IUnlockViewCallback;
import com.example.popularmovies01.data.MovieData;
import com.example.popularmovies01.data.ReviewData;
import com.example.popularmovies01.utils.BackgroundTasksUtils;
import com.example.popularmovies01.utils.MovieDataUtils;
import com.example.popularmovies01.viewmodel.MovieDetailsViewModel;

import java.util.ArrayList;

public class MovieDetailsActivity extends AppCompatActivity implements IUnlockViewCallback {

    private ListView mTrailersListView;
    private ListView mReviewsListView;

    private MovieData mMovieData;
    private FavoriteMoviesDatabase mDatabase;
    private Toast mToast;
    private boolean mBlockFavoriteIconTouch;
    private boolean mIsMovieInFavoriteList = false;
    private ScrollView mScrollView;

    private MovieDetailsViewModel mMovieDetailsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mTrailersListView = (ListView) findViewById(R.id.trailers_list_lv);
        mReviewsListView = (ListView) findViewById(R.id.reviews_list_lv);
        mScrollView = (ScrollView) findViewById(R.id.activity_details_sv);

        Intent currentIntent = getIntent();
        int moviePositionInDataList = -1;
        MovieDataUtils movieDataUtils = MovieDataUtils.getInstance();

        if(currentIntent.hasExtra(MainActivity.EXTRA_KEY_MOVIE_POSITION))
        {
            moviePositionInDataList = currentIntent.getIntExtra(MainActivity.EXTRA_KEY_MOVIE_POSITION, -1);
        }

        if(currentIntent.hasExtra(MainActivity.EXTRA_KEY_MOVIE_ID))
        {
            mIsMovieInFavoriteList = currentIntent.getBooleanExtra(MainActivity.EXTRA_KEY_MOVIE_ID, false);
        }

        // Either extra was not found or it was not set properly
        if(movieDataUtils.GetMovieDataList() == null || moviePositionInDataList < 0 || moviePositionInDataList > movieDataUtils.GetNumMoviesInDataList() - 1)
        {
            Toast.makeText(this, R.string.index_out_of_range, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FavoriteMoviesDatabase.getInstance(this);
        mMovieData = movieDataUtils.GetMovieDataAtPosition(moviePositionInDataList);

        mMovieDetailsViewModel = ViewModelProviders.of(this).get(MovieDetailsViewModel.class);

        // Observe review list changes:
        mMovieDetailsViewModel.GetReviewList().observe(this, new Observer<ArrayList<ReviewData>>() {
            @Override
            public void onChanged(@Nullable ArrayList<ReviewData> reviews) {
                if(MovieDataUtils.IsOfflineMode) return;

                ReviewListAdapter reviewListAdapter = new ReviewListAdapter(MovieDetailsActivity.this, reviews);
                mReviewsListView.setAdapter(reviewListAdapter);
            }
        });

        // Observe trailer list changes:
        mMovieDetailsViewModel.GetTrailerList().observe(this, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(@Nullable ArrayList<String> trailerKeys) {
                if(MovieDataUtils.IsOfflineMode) return;

                final TrailerListAdapter trailerListAdapter = new TrailerListAdapter(MovieDetailsActivity.this, trailerKeys);

                mTrailersListView.setAdapter(trailerListAdapter);
                mTrailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String key = trailerListAdapter.getItem(position);
                        Uri youtubeUrl = BackgroundTasksUtils.GetYoutubeTrailerURL(key);

                        Intent openYoutubeVideoIntent = new Intent(Intent.ACTION_VIEW, youtubeUrl);
                        if (openYoutubeVideoIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(openYoutubeVideoIntent);
                        }
                    }
                });
            }
        });

        mMovieDetailsViewModel.RequestMovieTrailerList(mMovieData.getMovieId());
        mMovieDetailsViewModel.RequestMovieReviewList(mMovieData.getMovieId());
        PopulateView();

        if(savedInstanceState != null && savedInstanceState.containsKey(MainActivity.SAVED_STATE_KEY_CURRENT_SCROLL_POSITION)){
            mScrollView.setScrollY(savedInstanceState.getInt(MainActivity.SAVED_STATE_KEY_CURRENT_SCROLL_POSITION));
        }
    }

    private void PopulateView()
    {
        // populate UI elements:
        ImageView moviePoster = (ImageView) findViewById(R.id.details_movie_poster_iv);

        TextView movieTitle = (TextView) findViewById(R.id.value_movie_title_tv);
        TextView releaseDate = (TextView) findViewById(R.id.value_release_date_tv);
        TextView userRating = (TextView) findViewById(R.id.value_user_rating_tv);
        TextView overview = (TextView) findViewById(R.id.value_overview_tv);

        // If any of the data is missing / us null - cancel populating view elements and go back to main activity.
        if(mMovieData != null)
        {
            // If we are connected to the internet -> get image from TheMovieDB API. otherwise set default 'missing' image
            if(!MovieDataUtils.IsOfflineMode) {
                BackgroundTasksUtils.PopulatePosterImageInImageView(mMovieData.getPosterPath(), moviePoster, true);
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    moviePoster.setImageDrawable(getDrawable(R.drawable.image_unavailable));
                }
            }
            moviePoster.setContentDescription(R.string.poster_iv_content_description + mMovieData.getMovieTitle());
            movieTitle.setText(mMovieData.getMovieTitle());
            releaseDate.setText(mMovieData.getReleaseDate());
            userRating.setText(String.valueOf(mMovieData.getUserRating()));
            overview.setText(mMovieData.getOverview());
        }
        else
        {
            Toast.makeText(this, R.string.movie_data_missing, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // This use is a solution inspired from here:
    // 'https://stackoverflow.com/questions/29106471/android-cant-toggle-menuitem-icon'
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {

        MenuItem item = menu.findItem(R.id.add_to_favorite);

        if (mIsMovieInFavoriteList){
            item.setIcon(R.drawable.star_gold);
        }
        else {
            item.setIcon(R.drawable.star_white);
        }
        item.setChecked(mIsMovieInFavoriteList);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // When adding movie to favorite or removing it, we will block touch effect until event handled in database.
        if(itemId == R.id.add_to_favorite)
        {
            if(mBlockFavoriteIconTouch) return true;

            mBlockFavoriteIconTouch = true;

            if (item.isChecked()){
                // Update database: remove from database
                mMovieDetailsViewModel.RemoveFromFavorite(mMovieData, mDatabase, this, item, false);
                return true;
            }
            else {
                // Update database: add to database
                mMovieDetailsViewModel.AddToFavorite(mMovieData, mDatabase, this, item, true);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void UnlockItemCallback(MenuItem item, boolean isChecked)
    {
        mBlockFavoriteIconTouch = false;

        item.setIcon(isChecked ? R.drawable.star_gold : R.drawable.star_white);
        item.setChecked(isChecked);

        if(mToast != null) {
            mToast.cancel();
            mToast = null;
        }

        int messageId = isChecked ? R.string.message_movie_added_to_db : R.string.message_movie_removed_from_db;
        mToast = Toast.makeText(this, getResources().getString(messageId), Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(MainActivity.SAVED_STATE_KEY_CURRENT_SCROLL_POSITION, mScrollView.getScrollY());

        super.onSaveInstanceState(outState);
    }

    // Remove observers
    @Override
    protected void onDestroy() {
        if(mMovieDetailsViewModel.GetTrailerList().hasActiveObservers())
            mMovieDetailsViewModel.GetTrailerList().removeObservers(this);

        if(mMovieDetailsViewModel.GetReviewList().hasActiveObservers())
            mMovieDetailsViewModel.GetReviewList().removeObservers(this);

        super.onDestroy();
    }
}
