package com.example.popularmovies01;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovies01.adapters.MovieListRecycleAdapter;
import com.example.popularmovies01.callbacks.IInternetConnectionCallback;
import com.example.popularmovies01.data.MovieData;
import com.example.popularmovies01.utils.MovieDataUtils;
import com.example.popularmovies01.viewmodel.MainActivityViewModel;
import com.example.popularmovies01.viewmodel.MainActivityViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        IInternetConnectionCallback, MovieListRecycleAdapter.ListItemClickListener {

    private MainActivityViewModel mMainActivityViewModel;

    final static String EXTRA_KEY_MOVIE_POSITION = "position";
    final static String EXTRA_KEY_MOVIE_ID = "id";
    final static String SAVED_STATE_KEY_CURRENT_SCROLL_POSITION = "scroll_y";
    private final static String SAVED_STATE_KEY_CURRENT_LIST_TYPE = "list_type";
    private final static String PREFIX_SORT_BY = "Sort by ";

    private LinearLayout mRecyclerViewWrapper;
    private RecyclerView mMovieListRecyclerView;

    private MovieDataUtils mMovieDataRepository;

    private TextView mErrorMessageView;
    private ProgressBar mLoadingProgressBar;
    private MenuItem mSortByMenuItem;

    private int mCurrentShownMovieListType;
    private float mScrollListPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMovieDataRepository = MovieDataUtils.getInstance();

        mErrorMessageView = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mRecyclerViewWrapper = (LinearLayout) findViewById(R.id.movie_list_rv_wrapper);

        FavoriteMoviesDatabase database = FavoriteMoviesDatabase.getInstance(this);
        MainActivityViewModelFactory factory = new MainActivityViewModelFactory(database);
        mMainActivityViewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);

        // Initiate Recycler View
        mMovieListRecyclerView = (RecyclerView) findViewById(R.id.movie_list_rv);
        int numColms = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
        GridLayoutManager layoutManager = new GridLayoutManager(this, numColms);
        mMovieListRecyclerView.setLayoutManager(layoutManager);
        mMovieListRecyclerView.setHasFixedSize(false);

        // Initiate by Clean Start / Saved Data
        if(savedInstanceState == null){
            mCurrentShownMovieListType = R.string.popular;
            ShowLoadingBar();

            // First checked is internet connectivity, then on complete we continue loading movie data
            mMainActivityViewModel.CheckInternetConnection(this, this);
        }
        else{
            // Current movie list type
            if(savedInstanceState.containsKey(SAVED_STATE_KEY_CURRENT_LIST_TYPE)){
                mCurrentShownMovieListType = savedInstanceState.getInt(SAVED_STATE_KEY_CURRENT_LIST_TYPE);
            }
            else {
                mCurrentShownMovieListType = R.string.popular;
            }

            // Get scroll position
            if(savedInstanceState.containsKey(SAVED_STATE_KEY_CURRENT_SCROLL_POSITION)){
                mScrollListPosition = savedInstanceState.getFloat(SAVED_STATE_KEY_CURRENT_SCROLL_POSITION);
            }

            initiateDatabaseListAndObservers();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save current movie list type
        outState.putInt(SAVED_STATE_KEY_CURRENT_LIST_TYPE, mCurrentShownMovieListType);

        // Save current scroll position
        int verticalScrollOffset = mMovieListRecyclerView.computeVerticalScrollOffset();
        int verticalScrollRange = mMovieListRecyclerView.computeVerticalScrollRange();
        outState.putFloat(SAVED_STATE_KEY_CURRENT_SCROLL_POSITION, (float)verticalScrollOffset/verticalScrollRange);

        super.onSaveInstanceState(outState);
    }

    private void SetupView()
    {
        // Reset RecyclerView
        int numMoviesInVisibleList = MovieDataUtils.getInstance().GetNumMoviesInDataList();
        MovieListRecycleAdapter movieDataListAdapter = new MovieListRecycleAdapter(numMoviesInVisibleList, this);
        mMovieListRecyclerView.setAdapter(movieDataListAdapter);

        ShowMoviePosterListView();
    }

    private void ShowMoviePosterListView()
    {
        mRecyclerViewWrapper.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageView.setVisibility(View.INVISIBLE);

        // Set scroll view position:
        // This is done here to wait enough time so the movie list is already set.
        // Solution inspired from here:
        // https://stackoverflow.com/questions/29581782/how-to-get-the-scrollposition-in-the-recyclerview-layoutmanager
        int numMoviesInVisibleList = MovieDataUtils.getInstance().GetNumMoviesInDataList();
        float scrollTo = numMoviesInVisibleList * mScrollListPosition;

        mMovieListRecyclerView.scrollToPosition((int)scrollTo);
    }

    private void ShowLoadingBar()
    {
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mErrorMessageView.setVisibility(View.INVISIBLE);
        mRecyclerViewWrapper.setVisibility(View.INVISIBLE);
    }

    private void ShowErrorMessage(int messageId)
    {
        mErrorMessageView.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mRecyclerViewWrapper.setVisibility(View.INVISIBLE);

        String message = getResources().getString(messageId);
        mErrorMessageView.setText(message);
    }

    // returns if we have an internet connection:
    @Override
    public void InternetConnectionCallback(boolean value) {
        if(value == getResources().getBoolean(R.bool.has_internet))
        {
            initiateDatabaseListAndObservers();
        }
        else
        {
            // Set offline mode.
            MovieDataUtils.IsOfflineMode = true;

            // Set current movieList type to favorite because this is the only list that is available for us when offline.
            mCurrentShownMovieListType = R.string.favorite;
            // Try get list for current (favorite) from database and apply it to view.
            initiateDatabaseListAndObservers();
        }
    }

    @Override
    protected void onDestroy() {
        // Remove observers
        if(mMainActivityViewModel.GetMovieList().hasActiveObservers())
            mMainActivityViewModel.GetMovieList().removeObservers(this);

        if(mMainActivityViewModel.GetFavoriteMovieList().hasActiveObservers())
            mMainActivityViewModel.GetFavoriteMovieList().removeObservers(this);

        super.onDestroy();
    }

    private void initiateDatabaseListAndObservers()
    {
        // Get movie list from network call only if it is not located in database (favorite)
        if(mCurrentShownMovieListType != R.string.favorite) {
            mMainActivityViewModel.UpdateMovieDataListFromNetwork(mCurrentShownMovieListType);
        }

        // On list change we will update the UI:
        mMainActivityViewModel.GetMovieList().observe(this, new Observer<ArrayList<MovieData>>() {
            @Override
            public void onChanged(@Nullable ArrayList<MovieData> movieData) {
                mMovieDataRepository.SetMovieDataList(movieData);
                SetupView();
            }
        });

        mMainActivityViewModel.GetFavoriteMovieList().observe(this, new Observer<List<MovieData>>() {
            @Override
            public void onChanged(@Nullable List<MovieData> movieDataList) {
                // Update ui only if current shown movie list is favorite movie list : fail safe.
                if(mCurrentShownMovieListType != R.string.favorite) return;

                // If we have at least 1 movie saved in favorite database we show the list.
                if(movieDataList != null && movieDataList.size() > 0) {
                    mMovieDataRepository.SetMovieDataList((ArrayList<MovieData>) movieDataList);
                    SetupView();

                    // If we are in offline mode -> show a toast message indicating it.
                    if(MovieDataUtils.IsOfflineMode)
                    {
                        // For some reason calling Toast.makeText here directly causes an error, so we move it to an outside method.
                        showOfflineMessage();
                    }
                }else{
                    // If we are offline -> show offline error message
                    // Else if we have no movie in favorite list -> show empty list error message
                    if(MovieDataUtils.IsOfflineMode) {
                        ShowErrorMessage(R.string.error_message_no_connection);
                    }else{
                        ShowErrorMessage(R.string.empty_visible_favorite_movies_list);
                    }
                }
            }
        });
    }

    // Show an offline Toast based message.
    private void showOfflineMessage()
    {
        Toast.makeText(this, R.string.offline_mode_message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Menu Item Spinner - was taken from here:
        // https://stackoverflow.com/questions/37250397/how-to-add-a-spinner-next-to-a-menu-in-the-toolbar
        mSortByMenuItem = menu.findItem(R.id.sort_movies_spinner);
        Spinner spinner = (Spinner) mSortByMenuItem.getActionView();

        mSortByMenuItem.setTitle(PREFIX_SORT_BY+getString(mCurrentShownMovieListType));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.action_get_movie_by, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.sort_by_popular:
                // If we are offline, we allow only already set 'favorite' list mode
                if(MovieDataUtils.IsOfflineMode) {
                    Toast.makeText(this, R.string.offline_mode_menu_items_blocked_message, Toast.LENGTH_LONG).show();
                    return true;
                }

                mSortByMenuItem.setTitle(PREFIX_SORT_BY+getString(R.string.popular));

                if(mCurrentShownMovieListType == R.string.popular) return true;

                mCurrentShownMovieListType = R.string.popular;
                mMainActivityViewModel.UpdateMovieDataListFromNetwork(R.string.popular);
                return true;
            case R.id.sort_by_top_rated:
                // If we are offline, we allow only already set 'favorite' list mode
                if(MovieDataUtils.IsOfflineMode) {
                    Toast.makeText(this, R.string.offline_mode_menu_items_blocked_message, Toast.LENGTH_LONG).show();
                    return true;
                }

                mSortByMenuItem.setTitle(PREFIX_SORT_BY+getString(R.string.top_rated));

                if(mCurrentShownMovieListType == R.string.top_rated) return true;

                mCurrentShownMovieListType = R.string.top_rated;
                mMainActivityViewModel.UpdateMovieDataListFromNetwork(R.string.top_rated);
                return true;
            case R.id.sort_by_favorite:
                // If we are offline, we allow only already set 'favorite' list mode
                if(MovieDataUtils.IsOfflineMode) {
                    Toast.makeText(this, R.string.offline_mode_menu_items_blocked_message, Toast.LENGTH_LONG).show();
                    return true;
                }

                mSortByMenuItem.setTitle(PREFIX_SORT_BY+getString(R.string.favorite));

                if(mCurrentShownMovieListType == R.string.favorite) return true;

                mCurrentShownMovieListType = R.string.favorite;
                mMovieDataRepository.SetMovieDataList((ArrayList<MovieData>) mMainActivityViewModel.GetFavoriteMovieList().getValue());
                SetupView();
                // if no movies are in Favorite movies list
                if(MovieDataUtils.getInstance().GetNumMoviesInDataList() == 0)
                {
                    ShowErrorMessage(R.string.empty_visible_favorite_movies_list);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Click handler for Movie Poster from the recycler view movie list.
    // Go to MovieDetailsActivity
    @Override
    public void onListItemClick(int clickedItemIndex) {
        boolean movieInFavoriteList = mMainActivityViewModel.IsMovieInFavoriteList(
                MovieDataUtils.getInstance().GetMovieDataAtPosition(clickedItemIndex).getMovieId(),
                FavoriteMoviesDatabase.getInstance(MainActivity.this));
        Context context = MainActivity.this;
        Intent showMovieDetailsIntent = new Intent(context, MovieDetailsActivity.class);
        showMovieDetailsIntent.putExtra(EXTRA_KEY_MOVIE_POSITION, clickedItemIndex);
        showMovieDetailsIntent.putExtra(EXTRA_KEY_MOVIE_ID, movieInFavoriteList);

        startActivity(showMovieDetailsIntent);
    }
}
