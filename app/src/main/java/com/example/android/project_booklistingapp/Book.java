package com.example.android.project_booklistingapp;

import java.util.List;

/**
 * Contains data for a book.
 */

public class Book {

    /** Title of book */
    private String mTitle;

    /** Author */
    private List<String> mAuthors;

    /**
     * Constructor for the {@link Book} class.
     * @param title of the book
     * @param authors of the book
     */
    public Book(String title, List<String> authors) {
        this.mTitle = title;
        this.mAuthors = authors;
    }

    public String getTitle() {
        return mTitle;
    }

    public List<String> getAuthor() {
        return mAuthors;
    }
}
