package com.example.android.project_booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Book>> {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = BookActivity.class.getSimpleName();

    /**
     * Key for saving ListView state
     */
    private static final String LIST_VIEW_STATE = "LIST_VIEW_STATE";

    /**
     * Key for saving {@link List<Book>} books list state
     */
    private static final String BOOKS_LIST_STATE = "BOOKS_LIST_STATE";

    /**
     * Query string "space" character. Does not get translated.
     */
    private static final String QUERY_CHARACTER_SPACE = " ";

    /**
     * Query string "plus" character. Does not get translated.
     */
    private static final String QUERY_CHARACTER_PLUS = "+";

    /**
     * Query string "protocol, domain, and initial path" portion. Does not get translated.
     */
    private static final String QUERY_INITIALPATH = "https://www.googleapis.com/books/v1/volumes?q=";

    /**
     * Query string "max results" portion. Does not get translated.
     */
    private static final String QUERY_MAXRESULTS = "&maxResults=";

    /**
     * Reference to the {@link ListView}
     */
    private ListView mListView;

    /**
     * Reference to the {@link ListView}'s empty view.
     */
    private TextView mEmptyView;

    /**
     * Reference to the {@link android.widget.ProgressBar}
     */
    private ProgressBar mProgressBar;

    /**
     * Reference to the {@link BookAdapter}
     */
    private BookAdapter mAdapter;

    /**
     * Limit search results to this number.
     */

    private static final int MAX_RESULTS = 20;

    /**
     * Reference to ConnectivityManager
     */
    private ConnectivityManager mConnectivityManager;

    /**
     * Reference to LoaderManager
     */
    private LoaderManager loaderManager;

    /**
     * Determine whether Loader needs to be restarted for new search term(s)
     */
    private static boolean mFirstSearch = true;

    /**
     * Reference to EditText with search term(s) input by user
     */
    private EditText mSearchField;

    /**
     * Reference to the search button
     */
    private Button mSearchButton;

    /**
     * Store any search term(s) entered by the user
     */
    private String mSearchTerm = null;

    /**
     * Store formatted HTTP query string, created from user's search term(s)
     */
    private String mQueryString = null;

    /**
     * Store reference to InputManager
     */
    private InputMethodManager mInputManager;

    /**
     * Store reference to {@link List} of {@link Book}
     */
    private List<Book> mBooks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        // Store reference to the ListView
        mListView = findViewById(R.id.list_view);

        // Store reference to the ProgressBar
        mProgressBar = findViewById(R.id.progress_bar);


        // Check whether saved state exists and contains previously loaded list of books from Google Books.
        // If so, retrieve data and create new adapter using it.
        if (savedInstanceState != null && savedInstanceState.containsKey(BOOKS_LIST_STATE)) {
            mBooks = savedInstanceState.getParcelableArrayList(BOOKS_LIST_STATE);
            mAdapter = new BookAdapter(this, mBooks);
        } else { // If no saved state,
            // Create new {@link com.example.android.project_booklistingapp.BookAdapter}.
            // Provide a blank List to first create the adapter; this will be updated when data has loaded.
            mAdapter = new BookAdapter(this, new ArrayList<Book>());
        }

        // If we've saved the ListView state previously (for example, upon orientation change),
        // restore that state.
        if (savedInstanceState != null && savedInstanceState.containsKey(LIST_VIEW_STATE)) {
            // Restore previous state (scroll position, etc.)
            mListView.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_VIEW_STATE));
        }

        // Store reference to the ListView's empty view
        mEmptyView = findViewById(R.id.empty_view);

        // Set empty view
        mListView.setEmptyView(mEmptyView);

        // Set adapter on the ListView
        mListView.setAdapter(mAdapter);

        // Get reference to a ConnectivityManager instance
        mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get reference to a loader manager instance
        loaderManager = getSupportLoaderManager();

        // Get references to search text & button, and store in global variables
        mSearchField = (EditText) findViewById(R.id.search_term);
        mSearchButton = (Button) findViewById(R.id.search_button);

        // Set listener to detect when Search button is pressed
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            // Code here executes on main thread after user presses button
            public void onClick(View v) {
                //  Retrieve entered text
                mSearchTerm = mSearchField.getText().toString();

                // Check whether entry is valid
                if (!mSearchTerm.isEmpty()) {
                    fetchBooks();
                } else {
                    // Display toast notifying user to enter a search term before pressing button
                    Toast toast = Toast.makeText(BookActivity.this, R.string.error_message_no_search_terms, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 512);
                    toast.show();
                }
            }
        });
    }

    /**
     * Format a server query string from the user's input search term(s).
     * Create a new {@link BookLoader} to perform server operations on a background thread.
     * If additional searches, restart the {@link BookLoader} to get new data.
     */
    private void fetchBooks() {

        // Get instance of InputManager, if one doesn't already exist
        if (mInputManager == null) {
            mInputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        // Hide the keyboard, since search button has been pressed.
        // (User can bring soft keyboard up again by tapping in the search term EditText for another search).
        mInputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        // Check whether there is network connectivity
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // If no network connection
        if (!isConnected) {
            // Clear out previous data
            mAdapter.clear();

            // Turn on empty view text
            mEmptyView.setVisibility(View.VISIBLE);

            // Set empty view text to show error message regarding network connectivity
            mEmptyView.setText(R.string.error_message_no_network_connection);
            return;
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

        // Display the ProgressBar while books are fetched
        mProgressBar.setVisibility(View.VISIBLE);

        // Declare and initialize new StringBuilder with server protocol/domain/partial path
        StringBuilder queryStringBuilder = new StringBuilder(QUERY_INITIALPATH);

        // Add search term(s) to path
        queryStringBuilder.append(mSearchTerm);
        // Add search criteria to path
        queryStringBuilder.append(QUERY_MAXRESULTS + Integer.toString(MAX_RESULTS));

        // Convert results to string. If any spaces between multiple search terms, replaces with needed "+" symbol.
        mQueryString = queryStringBuilder.toString().replace(QUERY_CHARACTER_SPACE, QUERY_CHARACTER_PLUS);

        // Create a new loader if initial search, otherwise restart the loader with fresh search term(s)
        if (mFirstSearch) {
            mFirstSearch = false;
            loaderManager.initLoader(1, null, this);
        } else if (!mFirstSearch) {
            mAdapter.clear();
            loaderManager.restartLoader(1, null, this);
        }
    }

    // Create a new loader, including the user's search term(s)
    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        return new BookLoader(this, mQueryString);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {

        // Hide the ProgressBar so we can display either list of books or empty state message
        mProgressBar.setVisibility(View.GONE);

        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);

            mBooks = books;
        } else { // No data returned
            // Set text to display message to user
            mEmptyView.setText(R.string.empty_message_no_books_found);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // Clear the data
        mAdapter.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // Check whether there is any data (i.e. list of books, which would also mean list view state is present).
        // (If list of books is null, then we don't need to worry about saving it or list view state).
        if (mBooks != null) {

            super.onSaveInstanceState(outState);

            // Save the ListView state into bundle
            outState.putParcelable(LIST_VIEW_STATE, mListView.onSaveInstanceState());

            // Save the list of books into bundle
            outState.putParcelableArrayList(BOOKS_LIST_STATE, (ArrayList<Book>) mBooks);
        }
    }
}
