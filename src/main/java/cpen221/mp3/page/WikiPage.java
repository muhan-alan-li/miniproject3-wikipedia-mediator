package cpen221.mp3.page;

import cpen221.mp3.exceptions.NotFoundException;
import cpen221.mp3.fsftbuffer.Bufferable;

public class WikiPage implements Bufferable {

    private final String id;
    private String content;

    /* -------- Rep Invariant ------- */
    // id != null


    /* -------- Abstraction Function -------- */
    // represents a wiki page, id is the page title, content is a string that represents the entire
    // content of the page


    /**
     * ensures the rep invariant is
     * @return true if valid, false otherwise
     */
    private boolean checkRep(){
        return id != null;
    }

    /**
     * Creates a WikiPage with a given id and content
     *
     * @param pageId  the given id of the page
     * @param content the content contained within the page
     */
    public WikiPage(String pageId, String content) {

        this.id = pageId;
        this.content = content;

        checkRep();
    }

    /**
     * Creates a WikiPage with given id, does not record content
     *
     * @param pageId the given id of the page
     */
    public WikiPage(String pageId) {

        this.id = pageId;
        this.content = null;

        checkRep();
    }

    /**
     * Gets the id of this WikiPage
     *
     * @return String representation of id of this WikiPage
     */
    @Override
    public String id() {

        return id;
    }

    /**
     * Gets the content of this page, throws excpetion if content == null
     *
     * @return String representation of the contents of this WikiPage
     * @throws NotFoundException if the content has not been saved in this object
     */
    public String getContent() throws NotFoundException {

        if (content == null) {

            throw new NotFoundException();
        } else {
            return content;
        }

    }

}
