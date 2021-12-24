package cpen221.mp3.shortestpath;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class ShortestPath {

    /**
     * Executes the search mechanism to find the shortest path between two wikipedia pages
     *
     * @param pageTitle1 String representing the starting page
     * @param pageTitle2 String representing the ending page
     * @param timeout    int representing the number of seconds this method will run before it
     *                   throws a timeout exception
     * @return a list of strings representing the path from pagetitle1 to pagetitle2, will return an
     * empty list if no such path exists
     * @throws TimeoutException if the runtime of the method exceeds the timeout limit
     */
    public static List<String> searchExecutor(String pageTitle1, String pageTitle2, int timeout)
            throws TimeoutException {

        ExecutorService searchExec = Executors.newCachedThreadPool();
        SearchThread search = new SearchThread(pageTitle1, pageTitle2);
        List<String> execResult = null;

        Callable<List<String>> timer = () -> {
            try {
                Thread.sleep(timeout * 1000L);
            } catch (InterruptedException ignored) { }
            return null;
        };

        List<Callable<List<String>>> searchAndTimer = new ArrayList<>();
        searchAndTimer.add(search);
        searchAndTimer.add(timer);

        try {
            execResult = searchExec.invokeAny(searchAndTimer);
        } catch (InterruptedException | ExecutionException ignored) { }

        if (execResult == null) {
            throw new TimeoutException();
        } else {
            return execResult;
        }

    }

}
