package com.example.android.project_booklistingapp;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
     * Limit search results to this number.
     */

    private static final int MAX_RESULTS = 10;

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
     * Reference to list of {@link Book} objects
     */
    private List<Book> mBooksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

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

//        String queryString = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=2""
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
        mBooksList = books;
        Log.v(LOG_TAG, "In the onLoadFinished method; returned list of books: " + mBooksList.toString());
        Log.v(LOG_TAG, "Book #1: " + mBooksList.get(0).getTitle());
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // TODO: ADD CODE TO CLEAR OUT ADAPTER, ONCE I'VE CREATED THAT...
    }

}
