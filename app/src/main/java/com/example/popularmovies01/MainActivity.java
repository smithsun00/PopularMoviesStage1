package com.example.popularmovies01;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.popularmovies01.data.MovieData;
import com.example.popularmovies01.utils.MovieDataUtils;
import com.example.popularmovies01.utils.NetworkUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    final static String EXTRA_KEY_MOVIE_POSITION = "position";
    final static String SAVED_DATA_KEY_MOVIE_DATA_LIST = "movieDataList";

    GridView mMovieListGridView;
    Spinner mMovieSearchBySpinner;
    MovieDataArrayAdapter mMovieDataListAdapter;
    LinearLayout mMoviePosterListHolderLayout;
    TextView mErrorMessageView;
    ProgressBar mLoadingProgressBar;

    boolean mInitiatedGetDataRequest;
    boolean mActivityInitByReorientation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoviePosterListHolderLayout = (LinearLayout) findViewById(R.id.movie_poster_list_view);
        mErrorMessageView = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mMovieListGridView = (GridView) findViewById(R.id.movie_list_gv);
        mMovieSearchBySpinner = (Spinner) findViewById(R.id.movie_search_by_spinner);
        // Use of spinner was taken from 'https://www.youtube.com/watch?v=on_OrrX7Nw4&t=91s'
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.action_get_movie_by, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMovieSearchBySpinner.setAdapter(adapter);
        mMovieSearchBySpinner.setOnItemSelectedListener(this);

        if(savedInstanceState == null || !savedInstanceState.containsKey(SAVED_DATA_KEY_MOVIE_DATA_LIST)){
            ShowLoadingBar();
            mActivityInitByReorientation = false;
        }
        else{
            mActivityInitByReorientation = true;
            ArrayList<MovieData> movieListCopy = savedInstanceState.getParcelableArrayList(SAVED_DATA_KEY_MOVIE_DATA_LIST);
            MovieDataUtils.SetMovieDataList(movieListCopy);
        }

        // First checked is internet connectivity, then on complete we continue loading movie data
        new InternetCheck().execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVED_DATA_KEY_MOVIE_DATA_LIST, MovieDataUtils.GetMovieDataList());
        super.onSaveInstanceState(outState);
    }

    /*
        This will be called every time a new item is clicked from the drop down list.
        (not one which is already selected)
        For some reason this is also called at the start of the activity: added a flag to prevent dealing with first call.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // cancel first call because it is called automatically when activity is created.
        if(!mInitiatedGetDataRequest)
        {
            mInitiatedGetDataRequest = true;
            return;
        }
        String searchBy = parent.getItemAtPosition(position).toString();

        ShowLoadingBar();
        // use of 'getString' was taken from 'https://developer.android.com/guide/topics/resources/string-resource'
        if(searchBy.equals(getString(R.string.popular)))
        {
            URL movieDataListUrl = NetworkUtils.GetPopularMovieListURL();
            new FetchMovieDataTask().execute(movieDataListUrl);
        }
        else if (searchBy.equals(getString(R.string.top_rated)))
        {
            URL movieDataListUrl = NetworkUtils.GetTopRatedMovieListURL();
            new FetchMovieDataTask().execute(movieDataListUrl);
        }
    }

    // Do nothing when no item is selected / selected by default.
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class FetchMovieDataTask extends AsyncTask<URL, Void, ArrayList<MovieData>>
    {
        @Override
        protected ArrayList<MovieData> doInBackground(URL... urls)
        {
            URL movieDataListUrl = urls[0];
            if(movieDataListUrl == null)
            {
                return null;
            }

            try
            {
                String movieDataListResponse = NetworkUtils.GetResponseFromHttpUrl(movieDataListUrl);

                ArrayList<MovieData> movieDataList = NetworkUtils.ParseJsonDataIntoMovieData(movieDataListResponse);

                return movieDataList;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<MovieData> movieData)
        {
            if(movieData == null)
            {
                ShowErrorMessage();
                return;
            }

            MovieDataUtils.SetMovieDataList(movieData);
            SetupView();
        }
    }

    private void SetupView()
    {
        mMovieDataListAdapter = new MovieDataArrayAdapter(this, MovieDataUtils.GetMovieDataList());

        mMovieListGridView.setAdapter(mMovieDataListAdapter);
        mMovieListGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = MainActivity.this;
                Intent showMovieDetailsIntent = new Intent(context, MovieDetailsActivity.class);
                showMovieDetailsIntent.putExtra(EXTRA_KEY_MOVIE_POSITION, position);

                startActivity(showMovieDetailsIntent);
            }
        });

        ShowMoviePosterList();
    }

    public class InternetCheck extends AsyncTask<String, Void, Boolean> {

        @Override protected Boolean doInBackground(String... strings) {
            try {
                Socket sock = new Socket();
                int timeout = 1500;
                sock.connect(new InetSocketAddress("8.8.8.8", 53), timeout);
                sock.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override protected void onPostExecute(Boolean hasInternetConnection) {

            if(hasInternetConnection)
            {
                if(!mActivityInitByReorientation) {
                    // Get Data from MovieDB API
                    GetInitialMovieData();
                }
                else
                {
                    // Data is extracted from savedInstanceState, just populate it
                    SetupView();
                }
            }
            else
            {
                ShowNoConnectionErrorMessage();
            }
        }
    }

    private void GetInitialMovieData()
    {
        URL movieDataListUrl = NetworkUtils.GetPopularMovieListURL();

        new FetchMovieDataTask().execute(movieDataListUrl);
    }

    private void ShowNoConnectionErrorMessage()
    {
        ShowErrorMessage();
    }

    private void ShowMoviePosterList()
    {
        mMoviePosterListHolderLayout.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageView.setVisibility(View.INVISIBLE);
    }

    private void ShowLoadingBar()
    {
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mErrorMessageView.setVisibility(View.INVISIBLE);
        mMoviePosterListHolderLayout.setVisibility(View.INVISIBLE);
    }

    private void ShowErrorMessage()
    {
        mErrorMessageView.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mMoviePosterListHolderLayout.setVisibility(View.INVISIBLE);
    }
}
