package cpen221.mp3.wikimediator;

import cpen221.mp3.exceptions.NotFoundException;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.page.WikiPage;
import cpen221.mp3.shortestpath.ShortestPath;
import org.fastily.jwiki.core.Wiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class WikiMediator {

    private static final Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
    private final FSFTBuffer<WikiPage> wikiBuffer;

    private final List<Search> searchHistory;
    private final List<Instant> requestHistory;

    /* -------- Rep Invariant -------- */
    // searchHistory is not null
    // All searches are stored in searchHistory
    // There are no duplicate searches in searchHistory.
    // requestHistory is not null
    // All requests are stored in requestHistory
    // wikiBuffer is not null

    /* -------- Abstraction Function -------- */
    // WikiMediator is a mediator service for Wikipedia that uses Wikipedia's API to
    // perform a variety of functions, such as tracking pages, tracking most common
    // searches and requests, and counting max number of requests in a given time.


    /**
     * Creates a cache to store Wikipedia pages with custom capacity
     * and staleness intervals.
     *
     * @param capacity          desired capacity for cache
     * @param stalenessInterval desired staleless interval for cache
     */
    public WikiMediator(int capacity, int stalenessInterval) {

        this.wikiBuffer = new FSFTBuffer<>(capacity, stalenessInterval);
        this.searchHistory = new ArrayList<>();
        this.requestHistory = new ArrayList<>();
        loadHistory();
    }

    /**
     * Creates a cache to store Wikipedia pages with
     * default capacity of 32 objects and
     * staleness interval of 3600s.
     */
    public WikiMediator() {

        this.wikiBuffer = new FSFTBuffer<>();
        this.searchHistory = new ArrayList<>();
        this.requestHistory = new ArrayList<>();
        loadHistory();
    }

    private void loadHistory() {

        final File fat = new File(WikiMediator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String path = fat.getAbsolutePath();
        String[] arr = path.split("mp3-Rumil");
        String newPath = arr[0] + "mp3-Rumil";
        final File f = new File(newPath);

        File local = new File(f, "local");
        File searchPath = new File(local, "searchHistory.txt");
        System.err.println(searchPath);
        File requestPath = new File(local, "requestHistory.txt");
        if (!searchPath.exists() || !requestPath.exists()) {
            return;
        }
        List<Search> searches = new ArrayList<>();
        List<Instant> requests = new ArrayList<>();
        try {

            FileReader frs = new FileReader(searchPath);
            BufferedReader searchReader = new BufferedReader(frs);
            for (String search = searchReader.readLine(); search != null; search = searchReader.readLine()) {

                System.err.println(search);
                String[] results = search.split("\\|");
                System.err.println(results[0]);
                Search input = new Search(results[0], Integer.parseInt(results[1]), Instant.parse(results[2]));
                searches.add(input);
            }

            FileReader frr = new FileReader(requestPath);
            BufferedReader requestReader = new BufferedReader(frr);
            for (String search = requestReader.readLine(); search != null; search = requestReader.readLine()) {
                requests.add(Instant.parse(search));
            }

            searchReader.close();
            frs.close();
            frr.close();
            requestReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.searchHistory.clear();
        this.searchHistory.addAll(searches);
        this.requestHistory.clear();
        this.requestHistory.addAll(requests);
    }

    /**
     * Given a query, return up to limit page titles that match the
     * query string (per Wikipedia's search service).
     *
     * @param query user-defined search entry
     * @param limit desired number of results
     * @return A list of Strings representing Wikipedia results.
     */
    public List<String> search(String query, int limit) {
        this.addToSearchHistory(query);
        this.addToRequestHistory();

        updateFiles();

        List<String> results = wiki.search(query, limit);
        for (String result : results) {
            wikiBuffer.put(new WikiPage(result));
        }
        return results;
    }

    /**
     * Given a pageTitle, return the text associated with
     * the Wikipedia page that matches pageTitle.
     *
     * @param pageTitle String representing title of desired page
     * @return A String of all text on desired Wikipedia page
     */
    public String getPage(String pageTitle) {

        this.addToSearchHistory(pageTitle);
        this.addToRequestHistory();
        updateFiles();
        String content;
        try {

            WikiPage page = this.wikiBuffer.get(pageTitle);
            content = page.getContent();
        } catch (NotFoundException e) {

            content = wiki.getPageText(pageTitle);
            this.wikiBuffer.put(new WikiPage(pageTitle, content));
        }
        return content;
    }

    /**
     * Return the most common Strings used in search and getPage requests,
     * with items being sorted in non-increasing count order.
     * When many requests have been made, return only limit items.
     *
     * @param limit int limiting the number of items to return
     * @return List of the most common Strings used in search and getPage requests.
     */
    public List<String> zeitgeist(int limit) {

        this.addToRequestHistory();
        updateRequestHistoryBackup(requestHistory);

        return new ArrayList<>(this.searchHistory).stream()
                .sorted(Comparator.comparing(Search::getCount).reversed())
                .map(Search::getSearch)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Similar to zeitgeist(), but returns the most frequent requests
     * made in the last timeLimitInSeconds seconds. This method
     * report at most maxItems of the most frequent requests.
     *
     * @param timeLimitInSeconds Specific duration to search
     * @param maxItems           Limit of items to return
     * @return List of trending requests as Strings.
     */
    public List<String> trending(int timeLimitInSeconds, int maxItems) {

        this.addToRequestHistory();

        updateRequestHistoryBackup(requestHistory);
        return new ArrayList<>(this.searchHistory).stream().filter(s -> {
                    Duration lifeSpan = Duration.between(Instant.now(), s.getLatestSearch()).abs();
                    return lifeSpan.minus(Duration.ofSeconds(timeLimitInSeconds)).isNegative();
                })
                .sorted(Comparator.comparing(Search::getCount).reversed())
                .map(Search::getSearch)
                .limit(maxItems)
                .collect(Collectors.toList());

        //i love streams
    }

    /**
     * Return the maximum number of requests seen in any time window of given length.
     *
     * @param timeWindowInSeconds Time window to view max number of requests.
     * @return An integer representing the number of requests seen in a specified time interval
     */
    public int windowedPeakLoad(int timeWindowInSeconds) {

        this.addToRequestHistory();
        updateRequestHistoryBackup(requestHistory);
        List<Instant> currentRequestHistory = new ArrayList<>(this.requestHistory);
        Duration durCap = Duration.ofSeconds(timeWindowInSeconds);
        Optional<Long> output = currentRequestHistory.stream()
                .map(instant -> new ArrayList<>(currentRequestHistory).stream()
                        .filter(i ->
                                !i.isBefore(instant) &&
                                        !durCap.minus(Duration.between(instant, i).abs()).isNegative())
                        .count())
                .max(Comparator.comparing(Long::valueOf));

        return output.map(Long::intValue).orElse(0);
    }

    /**
     * Return the maximum number of requests seen in 30s.
     *
     * @return An integer representing the number of requests seen in 30s
     */
    public int windowedPeakLoad() {

        return windowedPeakLoad(30);
    }

    /**
     * Helper method to add current time to request history.
     */
    private void addToRequestHistory() {

        this.requestHistory.add(Instant.now());
    }

    /**
     * Helper method to add search to search history.
     *
     * @param search String to add to search history.
     */
    private void addToSearchHistory(String search) {

        for (int i = 0; i < this.searchHistory.size(); i++) {

            Search current = this.searchHistory.get(i);
            if (current.getSearch().equals(search)) {

                Instant rn = Instant.now();
                this.searchHistory.set(i,
                        new Search(current.getSearch(), current.getCount() + 1, rn));
                return;
            }
        }
        Instant rn = Instant.now();
        this.searchHistory.add(new Search(search, 1, rn));
    }

    /**
     * Method to check the validity of WikiMediator.
     *
     * @return True if WikiMediator is valid, false otherwise
     */
    private boolean checkRep() {
        for (Search search : searchHistory) {

            if (search.getSearch() == null) {

                return false;
            }
            if (search.getCount() < 1) {

                return false;
            }
        }
        return true;
    }

    private void updateFiles() {

        updateSearchHistoryBackup(searchHistory);
        updateRequestHistoryBackup(requestHistory);
    }

    private void updateSearchHistoryBackup(List<Search> search) {

        final File fat = new File(WikiMediator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String path = fat.getAbsolutePath();
        String[] arr = path.split("mp3-Rumil");
        String newPath = arr[0] + "mp3-Rumil";
        final File f = new File(newPath);

        File local = new File(f, "local");
        local.mkdir();
        File newFile = new File(local, "searchHistory.txt");
        try {

            newFile.delete();
            newFile.createNewFile();
            FileWriter fw = new FileWriter(newFile, true);
            for (Search s : search) {

                fw.write(s.getSearch() + "|" +
                        s.getCount() + "|" +
                        s.getLatestSearch() + "\n");
            }
            fw.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void updateRequestHistoryBackup(List<Instant> requestHistory) {

        final File fat = new File(WikiMediator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String path = fat.getAbsolutePath();
        String[] arr = path.split("mp3-Rumil");
        String newPath = arr[0] + "mp3-Rumil";
        final File f = new File(newPath);

        File local = new File(f, "local");
        File newFile = new File(local, "requestHistory.txt");
        try {

            newFile.delete();
            newFile.createNewFile();
            FileWriter fw = new FileWriter(newFile, true);
            for (Instant i : requestHistory) {

                fw.write(i + "\n");
            }
            fw.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /* -------- Task 5 -------- */

    /**
     * Executes a breadth-first-search to find the shortest path between two wikipedia pages
     *
     * @param pageTitle1 String representing the starting page
     * @param pageTitle2 String representing the ending page
     * @param timeout    int representing the number of seconds this method will run before it
     *                   throws a timeout exception
     * @return a list of strings representing the path from pageTitle1 to pageTitle2, will return an
     * empty list if no such path exists
     * @throws TimeoutException if the runtime of the method exceeds the timeout limit
     */
    public List<String> shortestPath(String pageTitle1, String pageTitle2, int timeout) throws
            TimeoutException {

        this.addToRequestHistory();
        return ShortestPath.searchExecutor(pageTitle1, pageTitle2, timeout);
    }
}
