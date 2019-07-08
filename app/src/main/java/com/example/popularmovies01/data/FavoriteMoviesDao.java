package com.example.popularmovies01.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface FavoriteMoviesDao {
    @Query("SELECT * FROM movies")
    LiveData<List<MovieData>> loadAllFavoriteMovies();

    @Insert
    void insertMovie(MovieData favoriteMovie);

    @Delete
    void deleteMovie(MovieData favoriteMovie);
}
