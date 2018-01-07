package com.example.android.project_booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Contains data for a book.
 */

public class Book implements Parcelable {

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
        // Store size of the uthors list
        int numAuthors = mAuthors.size();

        // Check that author(s) list is not null
        if (mAuthors == null) {
            return "Author(s) unknown.";
        } else if (numAuthors < 2) { // Check whether more than one author
            return mAuthors.get(0);
        }

        // Need to prepare a String from the List of authors
        StringBuilder stringBuilder = new StringBuilder();

        // Loop through each author in the list and add to the StringBuilder
        for (int i = 0; i < numAuthors; i++) {
            stringBuilder.append(mAuthors.get(i));

            // If more than 2 authors, add comma
            if (numAuthors > 2 && i != numAuthors - 1) {
                stringBuilder.append(", ");
            }

            // Check whether to add "and" before last item in list
            if (i == numAuthors - 2) {
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
     * @param i flags
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeStringList(mAuthors);
    }
}
