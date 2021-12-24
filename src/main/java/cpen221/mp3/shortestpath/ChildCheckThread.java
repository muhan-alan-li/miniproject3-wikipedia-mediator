package cpen221.mp3.shortestpath;

import com.google.common.util.concurrent.RateLimiter;
import cpen221.mp3.page.PageNode;
import org.fastily.jwiki.core.MQuery;
import org.fastily.jwiki.core.NS;
import org.fastily.jwiki.core.Wiki;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ChildCheckThread implements Callable<List<PageNode>> {

    private static final Wiki w = new Wiki.Builder().withDomain("en.wikipedia.org").build();
    private static final double RATE_LIMIT = 13;
    private static final int PERMITS = 1;
    private static final RateLimiter limiter = RateLimiter.create(RATE_LIMIT);

    private final List<String> checkList;
    private final PageNode current;

    /* -------- Rep Invariant -------- */
    // Wiki w != null
    // RATE_LIMIT > 0
    // PERMITS > 0
    // limiter != null
    //
    // checkList is not null and contains no nulls
    // current is not null


    /* -------- Abstraction Function -------- */
    // A callable object that will begin a new thread and return a List of PageNodes


    /**
     * ensures the rep invariant is preserved
     * @return true if valid, false otherwise
     */
    private boolean checkRep(){
        return w != null && checkList != null && !checkList.contains(null) && current != null;
    }

    /**
     * Creates a new ChildCheckThread with the given checklist and current PageNode
     *
     * @param checkList a list of Strings, each string representing a page to check
     * @param current   the current PageNode, parent to all pages in the checkList
     */
    ChildCheckThread(List<String> checkList, PageNode current) {

        this.checkList = checkList;
        this.current = current;

        checkRep();
    }

    /**
     * Calls the object and begins execution of the thread
     *
     * @return a list of pagenodes, representing all the children of each page in checkList
     * @throws Exception may throw a variety of exceptions
     */
    @Override
    public List<PageNode> call() throws Exception {

        limiter.acquire(PERMITS);
        return MQuery.getLinksOnPage(w, checkList, NS.MAIN).entrySet().stream()
                .map(entry -> new PageNode(entry.getKey(), current, entry.getValue()))
                .collect(Collectors.toList());
    }

}
