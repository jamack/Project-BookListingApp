package com.example.android.project_booklistingapp;

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

    public static List<Book> extractBooks(String url) {
        Log.v(LOG_TAG, "In extractBooks method.");

        // Reference to string for JSON query response (to be parsed)
        String queriedString;

        // Initialize new list of com.example.android.project_booklistingapp.Book objects
        List<Book> books = new ArrayList<Book>();


        // Variable to store formatted URL returned by helper method
        URL formattedUrl;
        if (url != null && !url.isEmpty()) {
            formattedUrl = formatUrl(url);
            Log.v(LOG_TAG, "In extractBooks method; just called formatUrl helper method.");
        } else {
            Log.e(LOG_TAG, "String passed into extractBooks method is either null or empty.");
            return null;
        }


        if (formattedUrl != null) {
            queriedString = makeHttpRequest(formattedUrl);
//            Log.v(LOG_TAG, "In extractBooks method; just called makeHttpRequest helper method. " +
//                    "The value the returned JSON data - in string format - is: " + queriedString);
            Log.v(LOG_TAG, "In extractBooks method; just called makeHttpRequest helper method.");
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
                    Log.v(LOG_TAG,"Title of book #" + Integer.toString(i+1) + " is: " + title);
                    // Get the author(s)
                    JSONArray authorsJSON = volumeInfo.getJSONArray("authors");

                    // Convert authors from JSONArray to an ArrayList that can be used in Book constructor
                    List<String> authors = new ArrayList<>();
                    for (int j = 0; j < authorsJSON.length(); j++) {
                        authors.add(j, authorsJSON.getString(j));
                        Log.v(LOG_TAG,"An author is: " + authorsJSON.getString(j));
                    }

                    // TODO: ADD THE PARSED BOOK DATA TO THE LIST THAT WILL BE RETURNED
                    books.add(new Book(title, authors));
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing data from JSON string.", e);
            }
        }

        Log.v(LOG_TAG,"In extractBooks method; parsed server data and returning ArrayList books.");
        return books;

    }

//    public static URL formatUrl(String url) {
//        URL formattedURL;
//        try {
//            formattedURL = new URL(url);
//        } catch (MalformedURLException e) {
//            Log.e(LOG_TAG,"Cannot format provided string into a URL object...", e);
//            return null;
//        }
//
//        Log.v(LOG_TAG,"In formatUrl method; returning a URL of: " + formattedURL.toString());
//        return formattedURL;
//    }

    // TODO: TESTING - MOVE THIS METHOD TO QUERYUTILS WHEN FINISHED...
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

        Log.v(LOG_TAG, "In formatUrl method; returning a URL of: " + formattedURL.toString());
        return formattedURL;
    }

    // TODO: TESTING - MOVE THIS METHOD TO QUERYUTILS WHEN FINISHED...
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

    // TODO: TESTING - MOVE THIS METHOD TO QUERYUTILS WHEN FINISHED...
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
