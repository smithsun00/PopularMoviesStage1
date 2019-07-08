package com.example.popularmovies01.utils;

import com.example.popularmovies01.data.MovieData;

import java.util.ArrayList;

public class MovieDataUtils {

    public static boolean IsOfflineMode = false;

    private static MovieDataUtils instance;

    private static ArrayList<MovieData> MovieDataList;

    public static MovieDataUtils getInstance()
    {
        if(instance == null)
        {
            instance = new MovieDataUtils();
        }

        return instance;
    }

    public void SetMovieDataList(ArrayList<MovieData> movieData)
    {
        if(movieData == null) return;

        MovieDataList = null;
        MovieDataList = movieData;
    }

    public ArrayList<MovieData> GetMovieDataList()
    {
        return MovieDataList;
    }

    public MovieData GetMovieDataAtPosition(int position)
    {
        if(position >= MovieDataList.size()) return null;

        return MovieDataList.get(position);
    }

    public int GetNumMoviesInDataList()
    {
        return MovieDataList != null ? MovieDataList.size() : 0;
    }
}
