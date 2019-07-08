package com.example.popularmovies01.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.popularmovies01.FavoriteMoviesDatabase;

public class MainActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final FavoriteMoviesDatabase mDb;

    public MainActivityViewModelFactory(FavoriteMoviesDatabase database) {
        mDb = database;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MainActivityViewModel(mDb);
    }
}