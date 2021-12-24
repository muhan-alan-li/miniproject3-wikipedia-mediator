package cpen221.mp3.page;

import cpen221.mp3.fsftbuffer.Bufferable;
import org.fastily.jwiki.core.NS;
import org.fastily.jwiki.core.Wiki;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PageNode implements Bufferable {

    private static final Wiki wm = new Wiki.Builder().withDomain("en.wikipedia.org").build();

    private final String pageId;
    private PageNode prev = null;
    private final List<String> children = new ArrayList<>();

    /* -------- Rep Invariant -------- */
    // pageId != null
    // children != null, and does not contain nulls


    /* -------- Abstraction Function -------- */
    // PageNode represents a wiki page, where the pageId is the title of the page, and children
    // contains the titles of every page that can be accessed from a link on this page
    //
    // PageNode is also considered as a node in a graph, where pageId is the Id of this node,
    // children is a list of all Nodes that can be accessed from this node, and prev represents the
    // "parent" node from which this node was accessed from
    //
    // If a given Node has a prev == null, it is considered the root of the graph/tree


    /**
     * ensures the rep invariant is preserved
     * @return true if valid, false otherwise
     */
    private boolean checkRep(){
        return pageId != null && !children.contains(null);
    }

    /**
     * Creates a PageNode with a given String as its Id, the children will be autofilled using the
     * method .getLinksOnPage() provided by the jWiki package
     *
     * @param id the pageId of this given PageNode
     */
    public PageNode(String id) {

        this.pageId = id;
        this.children.addAll(wm.getLinksOnPage(true, id, NS.MAIN).stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList()));

        checkRep();
    }

    /**
     * Creates a pageNode with a give Id and parent, child list is filled with jWiki
     *
     * @param id   the pageId of this given PageNode
     * @param prev the parent Node, or previous Node from which this Node was accessed
     */
    public PageNode(String id, PageNode prev) {

        this.pageId = id;
        this.prev = prev;
        this.children.addAll(wm.getLinksOnPage(true, id, NS.MAIN).stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList()));

        checkRep();
    }

    /**
     * Creates a PageNode with a given Id, parent Node, and list of child Nodes
     *
     * @param id       the pageId of this given PageNode
     * @param prev     the parent Node, or previous Node from which this Node was accessed
     * @param children the list of children this Node has
     */
    public PageNode(String id, PageNode prev, List<String> children) {

        this.pageId = id;
        this.prev = prev;
        this.children.addAll(children);
    }

    /**
     * Gets the parent Node of this given node
     *
     * @return the stored prev value on this node, can return a null or address to another PageNode
     */
    public PageNode getPrev() {

        return this.prev;
    }

    /**
     * Gets the list of children of this node
     *
     * @return a copy of the list of Strings that represents the children of this node
     */
    public List<String> getChildren() {

        return new ArrayList<>(children);
    }

    /**
     * Gets the id of this node
     *
     * @return a String, representing the id of this PageNode
     */
    @Override
    public String id() {

        return this.pageId;
    }
}