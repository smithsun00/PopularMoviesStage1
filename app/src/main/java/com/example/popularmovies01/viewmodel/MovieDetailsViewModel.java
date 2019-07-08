package com.example.popularmovies01.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.view.MenuItem;

import com.example.popularmovies01.AppExecutors;
import com.example.popularmovies01.utils.BackgroundTasksUtils;
import com.example.popularmovies01.FavoriteMoviesDatabase;
import com.example.popularmovies01.callbacks.IUnlockViewCallback;
import com.example.popularmovies01.data.MovieData;
import com.example.popularmovies01.data.ReviewData;

import java.net.URL;
import java.util.ArrayList;

public class MovieDetailsViewModel extends ViewModel {

    private MutableLiveData<ArrayList<String>> mTrailers = new MutableLiveData<>();
    private MutableLiveData<ArrayList<ReviewData>> mReviews = new MutableLiveData<>();

    private BackgroundTasksUtils backgroundTasksRepo;

    public MovieDetailsViewModel()
    {
        backgroundTasksRepo = BackgroundTasksUtils.getInstance();
    }

    public LiveData<ArrayList<String>> GetTrailerList()
    {
        return mTrailers;
    }

    public LiveData<ArrayList<ReviewData>> GetReviewList()
    {
        return mReviews;
    }


    public void RequestMovieTrailerList(final int movieId)
    {
        AppExecutors executors = AppExecutors.getInstance();

        executors.networkIO().execute(new Runnable() {
            @Override
            public void run() {
                URL url = BackgroundTasksUtils.GetMovieTrailersURL(movieId);

                try {
                    String trailerDataResponse = backgroundTasksRepo.GetResponseFromHttpUrl(url);

                    ArrayList<String> trailerList = BackgroundTasksUtils.ParseJsonDataIntoTrailerData(trailerDataResponse);
                    mTrailers.postValue(trailerList);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void RequestMovieReviewList(final int movieId)
    {
        AppExecutors executors = AppExecutors.getInstance();

        executors.networkIO().execute(new Runnable() {
            @Override
            public void run() {
                URL url = BackgroundTasksUtils.GetMovieReviewsURL(movieId);

                try {
                    String reviewDataResponse = backgroundTasksRepo.GetResponseFromHttpUrl(url);

                    ArrayList<ReviewData> reviewList = BackgroundTasksUtils.ParseJsonDataIntoReviewData(reviewDataResponse);
                    mReviews.postValue(reviewList);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    public void AddToFavorite(final MovieData movieData, final FavoriteMoviesDatabase database, final IUnlockViewCallback iCallback, final MenuItem item, final boolean isChecked)
    {
        final AppExecutors executors = AppExecutors.getInstance();
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                database.favoriteMoviesDao().insertMovie(movieData);

                executors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(iCallback != null)
                            iCallback.UnlockItemCallback(item, isChecked);
                    }
                });
            }
        });
    }

    public void RemoveFromFavorite(final MovieData movieData, final FavoriteMoviesDatabase database, final IUnlockViewCallback iCallback, final MenuItem item, final boolean isChecked)
    {
        final AppExecutors executors = AppExecutors.getInstance();
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                database.favoriteMoviesDao().deleteMovie(movieData);

                executors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(iCallback != null)
                            iCallback.UnlockItemCallback(item, isChecked);
                    }
                });
            }
        });
    }
}
