package com.example.android.project_booklistingapp;

import java.util.List;

/**
 * Contains data for a book.
 */

public class Book {

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

    public String getTitle() {
        return mTitle;
    }

    public String getAuthors() {
        // Store size of the Authors list
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
}
