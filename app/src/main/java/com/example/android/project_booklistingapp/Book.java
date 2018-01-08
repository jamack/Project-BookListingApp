package com.example.android.project_booklistingapp;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Contains data for a book.
 */

public class Book implements Parcelable {

    /**
     * Reference to system resources, for getting string resources
     */
    private static Resources mResources;

    /**
     * Title of book
     */
    private String mTitle;

    /**
     * Author(s)
     */
    private List<String> mAuthors;

    /**
     * Constructor for the {@link Book} class.
     *
     * @param title   of the book
     * @param authors of the book
     */
    public Book(String title, List<String> authors) {
        this.mTitle = title;
        this.mAuthors = authors;
    }

    /**
     * Reconstruct {@link Book} from a Parcelable
     *
     * @param in Parcelable with {@link Book}'s data
     */
    protected Book(Parcel in) {
        mTitle = in.readString();
        mAuthors = in.createStringArrayList();
    }

    /**
     * Create new list of {@link Book} objects
     */
    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    /**
     * Getter method to return title
     *
     * @return title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Getter method to return author(s)
     *
     * @return authors, as formatted string of one or more authors
     */
    public String getAuthors() {

        // Store reference to system resources, for getting string resources
        mResources = Resources.getSystem();

        // Check whether any authors are listed for book.
        // If no authors (null), return message to that effect.
        if (mAuthors == null) {
            // Getting string resource via the next line crashes the app
            // return mResources.getString(R.string.book_authors_unknown);

            return "Author(s) unknown.";
        }

        // Store size of the authors list
        int numAuthors = mAuthors.size();

        if (numAuthors < 2) { // Check whether more than one author
            return mAuthors.get(0);
        }

        // Need to prepare a String from the List of authors
        StringBuilder stringBuilder = new StringBuilder();

        // Loop through each author in the list and add to the StringBuilder
        for (int i = 0; i < numAuthors; i++) {
            stringBuilder.append(mAuthors.get(i));

            // If more than 2 authors, add comma
            if (numAuthors > 2 && i != numAuthors - 1) {
                // Getting string resource via the next line crashes the app
                // stringBuilder.append(mResources.getString(R.string.book_authors_separator_comma));
                stringBuilder.append(", ");
            }

            // Check whether to add "and" before last item in list
            if (i == numAuthors - 2) {
                // Getting string resource via the next line crashes the app
                // stringBuilder.append(mResources.getString(R.string.book_authors_also));

                stringBuilder.append(" and ");
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write {@link Book}' data to a Parcelable that can be saved as part of an Activity's state
     *
     * @param parcel destination
     * @param i      flags
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeStringList(mAuthors);
    }
}
