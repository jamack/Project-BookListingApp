package com.example.android.project_booklistingapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter to populate ListView with series of {@link com.example.android.project_booklistingapp.Book} objects.
 */

public class BookAdapter extends ArrayAdapter {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = BookAdapter.class.getSimpleName();

    private List<Book> mBookList;

    private TextView mTitle;

    /**
     * Constructor for the {@link BookAdapter}.
     *
     * @param context to be used in inflating the view.
     * @param bookList List of {@link com.example.android.project_booklistingapp.Book} objects.
     */
    public BookAdapter(@NonNull Context context, @NonNull List<Book> bookList) {
        super(context, 0, bookList);

        this.mBookList = bookList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //Get the {@link Book} object located at this position in the list
        Book currentBook = (Book) getItem(position);

        // Cache with view lookup for a Book object which will be stored in a tag
        ViewHolder viewHolder;

        // Check whether we are reusing a View or need to inflate a new one
        if (convertView == null) { // No existing View; create one
            // Instantiate new ViewHolder object to cache Views for current KidThing
            viewHolder = new ViewHolder();

            // Inflate current view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);

            // Perform View lookups and cache those in the ViewHolder:
            viewHolder.titleTextView = convertView.findViewById(R.id.list_item_title);
            viewHolder.authorsTextView = convertView.findViewById(R.id.list_item_authors);

            convertView.setTag(viewHolder);
        } else { // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Access View's title TextView via ViewHolder, get title from current {@link Book},
        // and set title String on the TextView.
        viewHolder.titleTextView.setText(currentBook.getTitle());

        // Access View's author TextView via ViewHolder, get author(s) from current {@link Book},
        // and set author(s) String on the TextView.
        viewHolder.authorsTextView.setText(currentBook.getAuthors());

        return convertView;
    }

    /**
     * ViewHolder class, to be used as part of ViewHolder pattern with ListView
     */
    private static class ViewHolder {
        // Store reference to the title TextView
        private TextView titleTextView;
        // Store reference to the author(s) TextView
        private TextView authorsTextView;
    }

}
