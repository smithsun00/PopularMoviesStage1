package com.example.popularmovies01.utils;

import android.net.Uri;
import android.widget.ImageView;

import com.example.popularmovies01.data.MovieData;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class NetworkUtils {

    private final static String BASE_POSTER_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final static String DEFAULT_POSTER_IMAGE_SIZE_SMALL = "w342";
    private final static String DEFAULT_POSTER_IMAGE_SIZE_BIG = "w500";

    private final static String BASE_POPULAR_MOVIE_LIST_URL = "http://api.themoviedb.org/3/movie";
    private final static String MOVIE_LIST_TYPE_POPULAR = "popular";
    private final static String MOVIE_LIST_TYPE_TOP_RATED = "top_rated";
    private final static String PARAMETER_API_KEY = "api_key";
    private final static String API_KEY = "INSERT YOUR API KEY HERE";   // taken from 'themoviedb.org'

    public static ArrayList<MovieData> ParseJsonDataIntoMovieData(String jsonResponse)
    {
        try
        {
            JSONObject movieDataResponseContent = new JSONObject(jsonResponse);

            JSONArray movieDataList = movieDataResponseContent.getJSONArray("results");

            int numMoviesInDataList = movieDataList.length();
            ArrayList<MovieData> popularMovieListData = new ArrayList<MovieData>();
            for (int i = 0; i < numMoviesInDataList; i++)
            {
                JSONObject movieData = movieDataList.getJSONObject(i);
                String movieTitle = movieData.getString("title");
                String moviePosterPath = movieData.getString("poster_path");
                String movieOverview = movieData.getString("overview");
                double userRating = movieData.getDouble("vote_average");
                String releaseData = movieData.getString("release_date");

                popularMovieListData.add(new MovieData(movieTitle, moviePosterPath, movieOverview, userRating, releaseData));
            }

            return popularMovieListData;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static String GetMoviePosterPath(String movieId, boolean bigSize)
    {
        String sizeKey = bigSize ? DEFAULT_POSTER_IMAGE_SIZE_BIG : DEFAULT_POSTER_IMAGE_SIZE_SMALL;

        return BASE_POSTER_IMAGE_URL + sizeKey + movieId;
    }

    public static URL GetPopularMovieListURL()
    {
        Uri builtUri = Uri.parse(BASE_POPULAR_MOVIE_LIST_URL).buildUpon()
                .appendPath(MOVIE_LIST_TYPE_POPULAR)
                .appendQueryParameter(PARAMETER_API_KEY, API_KEY)
                .build();

        URL url = null;

        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    // .with does not seem to work. Replaced with usage from 'https://square.github.io/picasso/'
    public static void PopulatePosterImageInImageView(String posterUrl, ImageView imageView, boolean sizeBig)
    {
        Picasso.get().load(GetMoviePosterPath(posterUrl, sizeBig)).into(imageView);
    }

    public static URL GetTopRatedMovieListURL()
    {
        Uri builtUri = Uri.parse(BASE_POPULAR_MOVIE_LIST_URL).buildUpon()
                .appendPath(MOVIE_LIST_TYPE_TOP_RATED)
                .appendQueryParameter(PARAMETER_API_KEY, API_KEY)
                .build();

        URL url = null;

        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public static String GetResponseFromHttpUrl(URL url) throws IOException
    {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try
        {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();

            if(hasInput)
            {
                return scanner.next();
            }
            else
            {
                return null;
            }
        }
        finally
        {
            urlConnection.disconnect();
        }
    }
}
