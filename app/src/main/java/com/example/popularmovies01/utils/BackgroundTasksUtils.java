package com.example.popularmovies01.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.example.popularmovies01.AppExecutors;
import com.example.popularmovies01.R;
import com.example.popularmovies01.callbacks.IInternetConnectionCallback;
import com.example.popularmovies01.callbacks.IMovieDataArrayListCallback;
import com.example.popularmovies01.data.MovieData;
import com.example.popularmovies01.data.ReviewData;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class BackgroundTasksUtils {

    private final static String BASE_POSTER_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final static String DEFAULT_POSTER_IMAGE_SIZE_SMALL = "w342";
    private final static String DEFAULT_POSTER_IMAGE_SIZE_BIG = "w500";

    private final static String BASE_MOVIE_DATA_URL = "http://api.themoviedb.org/3/movie";
    private final static String BASE_YOUTUBE_URL = "https://www.youtube.com/watch";
    private final static String YOUTUBE_KEY_SEARCH = "v";
    private final static String MOVIE_LIST_TYPE_POPULAR = "popular";
    private final static String MOVIE_TRAILERS = "videos";
    private final static String MOVIE_REVIEWS = "reviews";
    private final static String MOVIE_LIST_TYPE_TOP_RATED = "top_rated";
    private final static String PARAMETER_API_KEY = "api_key";
    private final static String API_KEY = "INSERT YOUR KEY HERE!";   // taken from 'themoviedb.org'

    private static BackgroundTasksUtils instance;

    public static BackgroundTasksUtils getInstance()
    {
        if(instance == null){
            instance = new BackgroundTasksUtils();
        }
        return instance;
    }

    public void getMovieDataList(int sortType, IMovieDataArrayListCallback callback)
    {
        // Check if has internet connection:
         requestMovieDataFromRestAPI(sortType, callback);
    }

    private void requestMovieDataFromRestAPI(final int sortType, final IMovieDataArrayListCallback iCallback)
    {
        final AppExecutors executors = AppExecutors.getInstance();

        executors.networkIO().execute(new Runnable() {
            @Override
            public void run() {
                URL url = null;

                if(sortType == R.string.top_rated)
                {
                    url = GetTopRatedMovieListURL();
                }
                else if(sortType == R.string.popular)
                {
                    url = GetPopularMovieListURL();
                }

                ArrayList<MovieData> movieList = null;

                try {
                    String movieDataListResponse = GetResponseFromHttpUrl(url);

                    final ArrayList<MovieData> movieDataList = ParseJsonDataIntoMovieData(movieDataListResponse);

                    // Check if ViewModel was not destroyed together with its Activity
                    executors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if(iCallback != null)
                                iCallback.Callback(movieDataList);
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public String GetResponseFromHttpUrl(URL url) throws IOException
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

    // Parsing of Json response:
    private static ArrayList<MovieData> ParseJsonDataIntoMovieData(String jsonResponse)
    {
        try
        {
            JSONObject movieDataResponseContent = new JSONObject(jsonResponse);

            JSONArray movieDataList = movieDataResponseContent.getJSONArray("results");

            int numMoviesInDataList = movieDataList.length();
            ArrayList<MovieData> movieListData = new ArrayList<MovieData>();
            for (int i = 0; i < numMoviesInDataList; i++)
            {
                JSONObject movieData = movieDataList.getJSONObject(i);
                int movieId = movieData.getInt("id");
                String movieTitle = movieData.getString("title");
                String moviePosterPath = movieData.getString("poster_path");
                String movieOverview = movieData.getString("overview");
                double userRating = movieData.getDouble("vote_average");
                String releaseData = movieData.getString("release_date");

                movieListData.add(new MovieData(movieId, movieTitle, moviePosterPath, movieOverview, userRating, releaseData));
            }

            return movieListData;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> ParseJsonDataIntoTrailerData(String jsonResponse)
    {
        try
        {
            JSONObject trailerDataResponseContent = new JSONObject(jsonResponse);

            JSONArray trailerDataList = trailerDataResponseContent.getJSONArray("results");

            int numTrailersInDataList = trailerDataList.length();
            ArrayList<String> trailerKeyList = new ArrayList<String>();
            for (int i = 0; i < numTrailersInDataList; i++)
            {
                JSONObject trailerData = trailerDataList.getJSONObject(i);
                String trailerKey = trailerData.getString("key");

                trailerKeyList.add(trailerKey);
            }

            return trailerKeyList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<ReviewData> ParseJsonDataIntoReviewData(String jsonResponse)
    {
        try
        {
            JSONObject reviewDataResponseContent = new JSONObject(jsonResponse);

            JSONArray reviewDataList = reviewDataResponseContent.getJSONArray("results");

            int numReviewsInDataList = reviewDataList.length();
            ArrayList<ReviewData> reviewList = new ArrayList<ReviewData>();
            for (int i = 0; i < numReviewsInDataList; i++)
            {
                JSONObject trailerData = reviewDataList.getJSONObject(i);
                String reviewAuthor = trailerData.getString("author");
                String reviewContent = trailerData.getString("content");

                reviewList.add(new ReviewData(reviewAuthor, reviewContent));
            }

            return reviewList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Movie Poster Images:
    public static void PopulatePosterImageInImageView(String posterUrl, ImageView imageView, boolean sizeBig)
    {
        Picasso.get().load(GetMoviePosterPath(posterUrl, sizeBig)).into(imageView);
    }

    private static String GetMoviePosterPath(String movieId, boolean bigSize)
    {
        String sizeKey = bigSize ? DEFAULT_POSTER_IMAGE_SIZE_BIG : DEFAULT_POSTER_IMAGE_SIZE_SMALL;

        return BASE_POSTER_IMAGE_URL + sizeKey + movieId;
    }

    // URLs:

    private static URL GetPopularMovieListURL()
    {
        Uri getPopularMovieListUri = Uri.parse(BASE_MOVIE_DATA_URL).buildUpon()
                .appendPath(MOVIE_LIST_TYPE_POPULAR)
                .appendQueryParameter(PARAMETER_API_KEY, API_KEY)
                .build();

        return TryGetURLFromUri(getPopularMovieListUri);
    }

    // Get trailer list for a movie, given a movie id.
    public static URL GetMovieTrailersURL(int movieId)
    {
        Uri getMovieTrailerListUri = Uri.parse(BASE_MOVIE_DATA_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(MOVIE_TRAILERS)
                .appendQueryParameter(PARAMETER_API_KEY, API_KEY)
                .build();

        return TryGetURLFromUri(getMovieTrailerListUri);
    }

    // Get trailer list for a movie, given a movie id.
    public static URL GetMovieReviewsURL(int movieId)
    {
        Uri getMovieTrailerListUri = Uri.parse(BASE_MOVIE_DATA_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(MOVIE_REVIEWS)
                .appendQueryParameter(PARAMETER_API_KEY, API_KEY)
                .build();

        return TryGetURLFromUri(getMovieTrailerListUri);
    }

    private static URL GetTopRatedMovieListURL()
    {
        Uri getTopRatedMoviesUri = Uri.parse(BASE_MOVIE_DATA_URL).buildUpon()
                .appendPath(MOVIE_LIST_TYPE_TOP_RATED)
                .appendQueryParameter(PARAMETER_API_KEY, API_KEY)
                .build();

        return TryGetURLFromUri(getTopRatedMoviesUri);
    }

    public static Uri GetYoutubeTrailerURL(String trailerKey)
    {
        return Uri.parse(BASE_YOUTUBE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_KEY_SEARCH, trailerKey)
                .build();
    }

    private static URL TryGetURLFromUri(Uri uri)
    {
        URL url = null;

        try
        {
            url = new URL(uri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }


    // Check for internet connectivity:
    public void HasConnection(final Context context, final IInternetConnectionCallback iCallback) {
        final AppExecutors executors = AppExecutors.getInstance();

        executors.networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket sock = new Socket();
                    int timeout = 1500;
                    sock.connect(new InetSocketAddress("8.8.8.8", 53), timeout);
                    sock.close();

                    executors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if(iCallback != null)
                                iCallback.InternetConnectionCallback(context.getResources().getBoolean(R.bool.has_internet));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    executors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if(iCallback != null)
                                iCallback.InternetConnectionCallback(context.getResources().getBoolean(R.bool.no_internet));
                        }
                    });
                }
            }
        });
    }
}
