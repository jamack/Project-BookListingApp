package com.example.android.project_booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
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
     * TEST QUERY STRING
     */
    private static final String TEST_STRING = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=2";

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = BookActivity.class.getSimpleName();

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

    /**
     * Store reference to ListView's state. (If we need to save it at any point).
     */
    private Parcelable mListViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        // Store reference to the ListView
        mListView = findViewById(R.id.list_view);

        // Store reference to the ProgressBar
        mProgressBar = findViewById(R.id.progress_bar);


        if (savedInstanceState != null && savedInstanceState.containsKey("BookListState")) {
            mBooks = savedInstanceState.getParcelableArrayList("BookListState");
            mAdapter = new BookAdapter(this, mBooks);
        } else {
            // Create new {@link com.example.android.project_booklistingapp.BookAdapter}.
            // Provide a blank List to first create the adapter; this will be updated when data has loaded.
            mAdapter = new BookAdapter(this, new ArrayList<Book>());
        }

        // TODO: TRY TO IMPLEMENT AN "EITHER/OR" SCENARIO WITH FINDING LISTVIEW (FIND OR RESTORE)
        // If we've saved the ListView state previously (for example, upon orientation change),
        // restore that state.
        if (savedInstanceState != null && savedInstanceState.containsKey("ListViewState")) {
            Log.v(LOG_TAG,"In onCreate method; savedInstanceState is NOT null, so trying to restore ListView state...");
            // Restore previous state (including selected item index and scroll position)
            mListViewState = savedInstanceState.getParcelable("ListViewState");
            Log.v(LOG_TAG,"Value of ListViewState is: " + mListViewState.toString());
            mListView.onRestoreInstanceState(mListViewState);
            // Force redraw of ListView views
            mListView.invalidateViews();
        } else {
            // Store reference to the ListView's empty view
            mEmptyView = findViewById(R.id.empty_view);
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
                    Log.v(LOG_TAG, "TEST: Retrieved text from the search field: " + mSearchTerm);
                    fetchBooks(TEST_STRING);
                } else {
                    // Display toast notifying user to enter a search term before pressing button
                    Toast toast = Toast.makeText(BookActivity.this, "Please enter a search term", Toast.LENGTH_SHORT);
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
     *
     * @param userInput search term(s)
     */
    private void fetchBooks(String userInput) {
        Log.v(LOG_TAG, "Entering fetchBooks method.");

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
            mEmptyView.setText("No network connection.\n\nPlease check connection and try again.");
            return;
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

        // Display the ProgressBar while books are fetched
        mProgressBar.setVisibility(View.VISIBLE);

        // Declare and initialize new StringBuilder with server protocol/domain/partial path
        StringBuilder queryStringBuilder = new StringBuilder("https://www.googleapis.com/books/v1/volumes?q=");

        // Add search term(s) to path
        queryStringBuilder.append(mSearchTerm);
        // Add search criteria to path
        queryStringBuilder.append("&maxResults=" + Integer.toString(MAX_RESULTS));

        // Convert results to string. If any spaces between multiple search terms, replaces with needed "+" symbol.
        mQueryString = queryStringBuilder.toString().replace(" ", "+");

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
        Log.v(LOG_TAG, "In onCreateLoader method.");
        return new BookLoader(this, mQueryString);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {

        Log.v(LOG_TAG, "In the onLoadFinished method; returned list of books: " + books.toString());
        Log.v(LOG_TAG, "Book #1 title: " + books.get(0).getTitle());

        // Hide the ProgressBar so we can display either list of books or empty state message
        mProgressBar.setVisibility(View.GONE);

        // TODO: USE THE FOLLOWING LINE FOR TESTING THE EMPTY VIEW. DELETE ONCE DONE.
        //books = null;

        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);

            mBooks = books;
        } else { // No data returned
            // Set text to display message to user
            mEmptyView.setText("No books found for topic.");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // Clear the data
        mAdapter.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "In the onSaveInstanceState method. TEST: first book's title is: " + mBooks.get(0).getTitle());
//        // Save the ListView state (= includes scroll position) as a Parceble
//        Parcelable mListViewState = mListView.onSaveInstanceState();
//
//        outState.putParcelable("ListViewState", mListViewState);

//        ArrayList<Book> testCast = (ArrayList<Book>) mBooks;
        outState.putParcelable("ListViewState", mListView.onSaveInstanceState());
        outState.putParcelableArrayList("BookListState", (ArrayList<Book>) mBooks);

    }
}
