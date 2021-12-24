package cpen221.mp3.FSFTBufferTests.Tee;

import cpen221.mp3.exceptions.InvalidIDException;
import cpen221.mp3.fsftbuffer.Bufferable;

import java.util.HashSet;
import java.util.Set;

public class Tee implements Bufferable {

    private static Set<String> IDs = new HashSet<>();
    private static int count = 0;
    String id;

    /* -------- Rep Invariant -------- */
    // For a given tee, its id should be unique
    //
    // the field count represents the total number of instances of Tee that exists at a given time
    //
    // the field IDs is a set of all ids of all instances of Tee that exists at a given time

    /* -------- Abstraction Function -------- */
    // class Tee is simply a testing class made to implement the interface Bufferable to help test
    // the methods created in the class FSFTBuffer

    /**
     * Creates a new instance of Tee, where the id of Tee must be unique
     *
     * @param id String that represents the id of Tee
     * @throws InvalidIDException If there already exists a Tee with this id, an exception is thrown
     */
    public Tee(String id) throws InvalidIDException {

        if (!IDs.contains(id)) {

            this.id = id;
            count++;
        } else {
            throw new InvalidIDException();
        }
    }

    /**
     * Basic getter method
     *
     * @return The String that represents the id of a given Tee
     */
    public String id() {

        return id;
    }
}
