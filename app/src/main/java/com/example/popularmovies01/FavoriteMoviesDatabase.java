package com.example.popularmovies01;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.popularmovies01.data.FavoriteMoviesDao;
import com.example.popularmovies01.data.MovieData;

@Database(entities = {MovieData.class}, version = 1, exportSchema = false)
public abstract class FavoriteMoviesDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "favorite_movies";
    private static FavoriteMoviesDatabase sInstance;

    public static FavoriteMoviesDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        FavoriteMoviesDatabase.class, FavoriteMoviesDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract FavoriteMoviesDao favoriteMoviesDao();

}
