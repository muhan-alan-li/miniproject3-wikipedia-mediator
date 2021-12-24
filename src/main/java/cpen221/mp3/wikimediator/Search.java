package cpen221.mp3.wikimediator;

import org.fastily.jwiki.core.Wiki;

import java.time.Instant;

public class Search {

    private final String search;
    private final int count;
    private final Instant latestSearch;

    /* -------- Rep Invariant -------- */
    // search is not null or empty
    // count is >= 1
    // latestSearch is the latest instance of this search


    /* -------- Abstraction Function -------- */
    // Search is a helper class that tracks the count and latest search
    // of any specific string searched on Wikipedia. Search is not Thread-safe.


    /**
     * Creates an instance of Search, containing the searched String,
     * the number of searches, and the latest time of search.
     *
     * @param search String to search
     * @param count  Number of times search has been searched
     * @param time   Time of latest search
     */
    public Search(String search, int count, Instant time) {

        this.search = search;
        this.count = count;
        this.latestSearch = time;
    }

    /**
     * Returns the string searched by this Search instance.
     *
     * @return A String.
     */
    public String getSearch() {

        return search;
    }

    /**
     * Returns the number of time this specific query has been searched
     *
     * @return An integer representing the count of searches.
     */
    public int getCount() {

        return count;
    }

    /**
     * Returns a timestamp of the latest search of this query
     *
     * @return An Instant representing the latest search time.
     */
    public Instant getLatestSearch() {

        return latestSearch;
    }

    /**
     * Helper method to check the validity of the Search object.
     *
     * @return True if Search is valid, false otherwise.
     */
    private boolean checkRep() {

        if (search == null || !search.isEmpty()) {
            return false;
        }
        return count >= 1;
    }

}
