package com.example.popularmovies01.utils;

import com.example.popularmovies01.data.MovieData;

import java.util.ArrayList;

public class MovieDataUtils {

    private static ArrayList<MovieData> MovieDataList;


    public static void SetMovieDataList(ArrayList<MovieData> movieData)
    {
        MovieDataList = null;
        MovieDataList = movieData;
    }

    public static ArrayList<MovieData> GetMovieDataList()
    {
        return MovieDataList;
    }

    public static MovieData GetMovieDataAtPosition(int position)
    {
        if(position > MovieDataList.size()) return null;

        return MovieDataList.get(position);
    }

    public static int GetNumMoviesInDataList()
    {
        return MovieDataList != null ? MovieDataList.size() : 0;
    }

}
