package com.example.android.project_booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;

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
    private String mAuthors;

    /**
     * Constructor for the {@link Book} class.
     *
     * @param title   of the book
     * @param authors of the book
     */
    public Book(String title, String authors) {
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
        mAuthors = in.readString();
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
        return mAuthors;
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
        parcel.writeString(mAuthors);
    }
}
