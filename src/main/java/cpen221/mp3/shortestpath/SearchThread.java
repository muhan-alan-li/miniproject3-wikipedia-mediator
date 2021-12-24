package cpen221.mp3.shortestpath;

import cpen221.mp3.page.PageNode;
import org.fastily.jwiki.core.Wiki;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class SearchThread implements Callable<List<String>> {

    private static final int THREAD_SIZE = 10;

    private final String start;
    private final String end;

    /* -------- Rep Invariant -------- */
    // THREAD_SIZE > 0
    // start != null
    // end   != null
    // start and end are valid page titles on wikipedia


    /* -------- Abstraction Function -------- */
    // A thread that performs the breadth-first-search required to find the shortest path between
    // two pages. start is starting page, end is ending page


    /**
     * ensures the rep invariant is preserved
     * @return true if valid, false otherwise
     */
    private boolean checkRep(){
        Wiki w = new Wiki.Builder().withDomain("en.wikipedia.org").build();
        return start != null && end != null && w.exists(start) && w.exists(end);
    }

    /**
     * Creates a searchThread with given starting and ending page, assumes the Strings are valid IDs
     *
     * @param pg1 String representing Id of starting page
     * @param pg2 String representing Id of ending page
     */
    SearchThread(String pg1, String pg2) {

        this.start = pg1;
        this.end = pg2;

        checkRep();
    }

    /**
     * Calls and begins the execution of this thread
     *
     * @return list of strings, representing the path between the two pages
     * @throws Exception throws a variety of exceptions
     */
    @Override
    public List<String> call() throws Exception {

        return bfs(start, end);
    }

    /**
     * Multithreaded breadth-first-search on given starting and ending pages
     *
     * @param start String of starting page Id
     * @param end   String of ending page Id
     * @return the path between the two pages
     */
    private static List<String> bfs(String start, String end) {

        Queue<PageNode> searchQueue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        searchQueue.add(new PageNode(start));
        visited.add(start);

        while (!searchQueue.isEmpty()) {

            PageNode current = searchQueue.poll();
            if (current.getChildren().contains(end)) {

                searchQueue.clear();
                return tracePath(new PageNode(end, current));
            }

            List<String> toCheck = current.getChildren().stream()
                    .filter(child -> !visited.contains(child))
                    .collect(Collectors.toList());

            searchQueue.addAll(parallelChildCheck(toCheck, current).stream()
                    .filter(pn -> !visited.contains(pn.id()))
                    .collect(Collectors.toList()));

            visited.addAll(toCheck);
        }
        return new ArrayList<>();
    }

    /**
     * Generates all the children pages of each page represented by a String in the list toCheck
     *
     * @param toCheck a list of String, each the id of a page
     * @param parent  a parent node, every page in toCheck is a child of this parent
     * @return List of pageNodes, representing all children of pages in toCheck
     */
    private static List<PageNode> parallelChildCheck(List<String> toCheck, PageNode parent) {

        ExecutorService childExecutor = Executors.newCachedThreadPool();
        List<Callable<List<PageNode>>> taskList = new ArrayList<>();
        List<Future<List<PageNode>>> execResults = new ArrayList<>();
        List<PageNode> toBeQueued = new ArrayList<>();

        for (List<String> checkList : partitionList(toCheck, THREAD_SIZE)) {

            ChildCheckThread childChecker =
                    new ChildCheckThread(checkList, parent);
            taskList.add(childChecker);
        }

        try {
            execResults = childExecutor.invokeAll(taskList);
        } catch (InterruptedException ignored) {
        }

        execResults.forEach(pnFuture -> {
            if (pnFuture.isDone()) {
                try {
                    toBeQueued.addAll(pnFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        childExecutor.shutdownNow();

        return toBeQueued.stream()
                .sorted(Comparator.comparing(PageNode::id))
                .collect(Collectors.toList());
    }

    /**
     * Helper method, partitions a list into equal partitons about the size of the given size +- 2
     *
     * @param toSplit the List to split
     * @param size    the desired size of each partition
     * @return a list of lists, each list inside representing a partition
     */
    private static List<List<String>> partitionList(List<String> toSplit, int size) {

        List<List<String>> out = new ArrayList<>();
        int length = toSplit.size();
        int parts = length / size;
        int start;
        int end = 0;

        if (length < size) {

            return List.of(toSplit);
        }

        for (int i = 0; i < parts; i++) {

            start = end;
            end = start + length / parts;
            out.add(toSplit.subList(start, end));
        }

        if (end < length) {

            List<String> remainder = toSplit.subList(end, length - 1);
            out.add(remainder);
        }

        return out;
    }

    /**
     * helper method, given an end node, trace the path through which this end node was reached
     *
     * @param end the PageNode that is the end node
     * @return a list of strings representing the path to reach this node
     */
    private static List<String> tracePath(PageNode end) {

        List<String> path = new ArrayList<>();
        PageNode current = end;
        while (current != null) {

            path.add(current.id());
            current = current.getPrev();
        }

        Collections.reverse(path);
        return path;
    }

}
