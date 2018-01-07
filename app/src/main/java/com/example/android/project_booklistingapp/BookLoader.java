package com.example.android.project_booklistingapp;

import android.content.Context;

import java.util.List;

/**
 * Loader class - handles creating a list - on a background thread - of {@link Book} objects
 * that are fetched from a server, parsed, and added to a list.
 */
public class BookLoader extends android.support.v4.content.AsyncTaskLoader {

    /** Tag for log/exception messages */
    public static final String LOG_TAG = BookLoader.class.getName();

    /** Reference to the query string */
    private String mQueryUrl;

    /**
     * Constructs a new {@link BookLoader}.
     * @param context of the activity.
     * @param queryUrl to load data from.
     */
    public BookLoader(Context context, String queryUrl) {
        super(context);

        this.mQueryUrl = queryUrl;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * Calls utility/helper methods that perform network operations
     * on a background thread.
     * @return a list of {@link Book} objects.
     */
    @Override
    public List<Book> loadInBackground() {
        return QueryUtils.extractBooks(mQueryUrl);
    }


}
