package com.example.popularmovies01.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.example.popularmovies01.utils.BackgroundTasksUtils;
import com.example.popularmovies01.FavoriteMoviesDatabase;
import com.example.popularmovies01.callbacks.IInternetConnectionCallback;
import com.example.popularmovies01.callbacks.IMovieDataArrayListCallback;
import com.example.popularmovies01.data.MovieData;

import java.util.ArrayList;
import java.util.List;

public class MainActivityViewModel extends ViewModel implements IMovieDataArrayListCallback {

    private LiveData<List<MovieData>> mFavoriteMovies;            // from database
    private MutableLiveData<ArrayList<MovieData>> mMovieDataList;   // from Rest API
    private BackgroundTasksUtils mMovieListRepo;


    public MainActivityViewModel(FavoriteMoviesDatabase database){
        mMovieListRepo = BackgroundTasksUtils.getInstance();
        mFavoriteMovies = database.favoriteMoviesDao().loadAllFavoriteMovies();
    }

    public LiveData<List<MovieData>> GetFavoriteMovieList()
    {
        return mFavoriteMovies;
    }

    public LiveData<ArrayList<MovieData>> GetMovieList()
    {
        if(mMovieDataList == null)
        {
            mMovieDataList = new MutableLiveData<>();
        }

        return mMovieDataList;
    }

    public void UpdateMovieDataListFromNetwork(int sortType)
    {
        if(mMovieDataList == null)
        {
            mMovieDataList = new MutableLiveData<>();
        }

        mMovieListRepo.getMovieDataList(sortType, this);
    }

    public void CheckInternetConnection(Context context, IInternetConnectionCallback iCallback)
    {
        mMovieListRepo.HasConnection(context, iCallback);
    }

    public boolean IsMovieInFavoriteList(int movieId, FavoriteMoviesDatabase database)
    {
        if(mFavoriteMovies == null ||  mFavoriteMovies.getValue() == null) return false;

        for (MovieData movie : mFavoriteMovies.getValue())
        {
            if(movie.getMovieId() == movieId)
            {
                return true;
            }
        }

        return false;
    }

    // Callback for updating data after data is returned from Network Calls.
    @Override
    public void Callback(ArrayList<MovieData> movieDataList) {
        mMovieDataList.postValue(movieDataList);
    }
}
