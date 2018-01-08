package com.example.android.project_booklistingapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving book data from the Google Books API.
 */

public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Takes a server query in string format, creates a {@link URL}, makes HTTP server connection,
     * reads input stream, parses returned JSON data string, and saves data as a list of {@link Book} objects.
     * Utilizes helper methods.
     *
     * @param context for getting string resources
     * @param url     to fetch data
     * @return {@link List<Book>} with title & author information
     */
    public static List<Book> extractBooks(Context context, String url) {

        // Reference to string for JSON query response (to be parsed)
        String queriedString;

        // Initialize new list of com.example.android.project_booklistingapp.Book objects
        List<Book> books = new ArrayList<Book>();


        // Variable to store formatted URL returned by helper method
        URL formattedUrl;
        if (url != null && !url.isEmpty()) {
            formattedUrl = formatUrl(url);
        } else {
            Log.e(LOG_TAG, "String passed into extractBooks method is either null or empty.");
            return null;
        }


        if (formattedUrl != null) {
            queriedString = makeHttpRequest(formattedUrl);
        } else {
            Log.e(LOG_TAG, "URL object produced by extractBooks' formatUrl method is null.");
            return null;
        }

        // Parse the returned JSON string to extract desired data and create a list of Book objects
        if (queriedString != null && queriedString != "") {
            // Try to parse the queryString response. If there's a problem with the way the JSON
            // is formatted, a JSONException exception object will be thrown.
            // Catch the exception so the app doesn't crash, and print the error message to the logs.
            try {
                // Convert query string into a JSON objects containing books + other query data
                JSONObject jsonQuery = new JSONObject(queriedString);

                // Check that query has returned an array of "items" rather than "totalItems: 0"
                if (jsonQuery.has("items")) {

                    // Get the "items" array of books
                    JSONArray jsonBooks = jsonQuery.getJSONArray("items");

                    // Loop through objects (books) in the array
                    for (int i = 0; i < jsonBooks.length(); i++) {

                        // Get the object (book) at given index
                        JSONObject book = jsonBooks.getJSONObject(i);

                        // Get the "volumeInfo" object for the book
                        JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                        // Get the title
                        String title = volumeInfo.getString("title");

                        // Create new JSONArray to hold authors, if any
                        JSONArray authorsJSON = new JSONArray();
                        // If book has authors listed, get the author(s)
                        if (volumeInfo.has("authors")) {
                            authorsJSON = volumeInfo.getJSONArray("authors");
                        } else { // If no authors listed for book, nullify the JSONArray
                            authorsJSON = null;
                        }

                        // Create string to hold formatted author(s) info.
                        // Default text for no author data; to be overwritten if present.
                        String authors = context.getResources().getString(R.string.book_authors_unknown);

                        if (authorsJSON != null) {
                            // Need to prepare a String from the List of authors
                            StringBuilder stringBuilder = new StringBuilder();

                            // Store size of the authors list
                            int numAuthors = authorsJSON.length();

                            // Loop through each author in the list and add to the StringBuilder
                            for (int j = 0; j < numAuthors; j++) {
                                stringBuilder.append(authorsJSON.get(j));

                                // If more than 2 authors, add comma
                                if (numAuthors > 2 && j != numAuthors - 1) {
                                    stringBuilder.append(context.getResources().getString(R.string.book_authors_separator_comma));
                                }

                                // Check whether to add "and" before last item in list
                                if (j == numAuthors - 2) {
                                    stringBuilder.append(context.getResources().getString(R.string.book_authors_also));
                                }
                            }

                            authors = stringBuilder.toString();
                        }

                        // Add parsed book data to the list that will be returned
                        books.add(new Book(title, authors));
                    }
                } else { // If JSON string from server does not contain "items", there is no book data to parse
                    // Nullify books list
                    books = null;
                }


            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing data from JSON string.", e);
            }
        }

        return books;
    }

    /**
     * Format the provided String into a URL.
     *
     * @param url in String format
     * @return URL object. (Null if it cannot be properly formatted).
     */
    public static URL formatUrl(String url) {
        // Reference to a URL object.
        URL formattedURL;
        try {
            // Try to create new URL object from provided String
            formattedURL = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Cannot format provided string into a URL object...", e);
            return null;
        }

        return formattedURL;
    }

    /**
     * Attempt to open HTTP connection and retrieve data from the server
     *
     * @param queryUrl URL object with protocol/server/query data
     * @return String of returned JSON data
     */
    public static String makeHttpRequest(URL queryUrl) {
        // String to hold response. Initialized as an empty string.
        String jsonResponse = "";

        // Ensure passed URL is valid.
        if (queryUrl == null) {
            return null;
        }

        // Obtain a new HttpURLConnection by calling URL.openConnection() and casting the result to HttpURLConnection
        HttpURLConnection httpConnection;
        try {
            httpConnection = (HttpURLConnection) queryUrl.openConnection();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to create HttpURLConnection object.", e);
            return null;
        }

        // Prepare the request. The primary property of a request is its URI. Request headers may also include
        // metadata such as credentials, preferred content types, and session cookies.
        // Prepare the request / Set the parameters
        httpConnection.setConnectTimeout(10000 /* milliseconds */);
        httpConnection.setReadTimeout(15000 /* milliseconds */);
        try {
            httpConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            Log.e(LOG_TAG, "Problem setting up HTTP Connection.", e);
            return null;
        }

        // Make HTTP connection
        try {
            httpConnection.connect();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to make HTTP connection.", e);
            return null;
        }

        // Check whether HTTP connection was successful
        try {
            if (httpConnection.getResponseCode() == 200) {
                // The response body may be read from the stream returned by getInputStream().
                // If the response has no body, that method returns an empty stream.
                // Get the InputStream
                InputStream inputStream = httpConnection.getInputStream();

                // Save full contents of InputStream to a String, via helper method
                jsonResponse = readFromStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonResponse;
    }

    /**
     * Read an input stream, buffer it, and return a single String
     *
     * @param inputStream of character data
     * @return String of buffered and compiled data
     * @throws IOException to be caught by calling code
     */
    public static String readFromStream(InputStream inputStream) throws IOException {

        // Create new StringBuilder object to hold server response
        StringBuilder output = new StringBuilder();

        BufferedReader reader;
        // Check whether passed input stream is valid
        if (inputStream != null) {
            // Create new InputStreamReader and wrap it in a new BufferedReader
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

            // Read first line from the buffered reader
            String line = reader.readLine();
            // If initial line is not null, add it to the StringBuilder.
            // Repeat for as many non-null lines as exist
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }

        }

        // Convert StringBuilder's content and return a final String
        return output.toString();
    }


}
