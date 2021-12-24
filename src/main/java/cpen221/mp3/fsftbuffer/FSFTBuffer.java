package cpen221.mp3.fsftbuffer;

import cpen221.mp3.exceptions.NotFoundException;

import java.time.Instant;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FSFTBuffer<T extends Bufferable> {

    /* the default buffer size is 32 objects */
    private static final int DSIZE = 32;

    /* the default timeout value is 3600s */
    private static final int DTIMEOUT = 3600;

    // size and duration for buffer
    private final int maxSize;
    private final Duration maxTime;

    // tracks objects in the buffer
    private final ConcurrentMap<T, Duration> bufferMap;

    // used for timekeeping
    private final Instant birthTime;
    private Instant lastUpdatedAt;

    /* -------- Rep Invariant -------- */
    // maxSize is >=1
    // maxTime is >=1s
    // buffermap is not null and does not contain nulls.
    // for each key in buffermap, the corresponding duration has value >= 0
    // buffermap contains keys with values > maxTime, but those are not considered
    //  to be occupying any space.

    /* -------- Abstraction Function -------- */
    // FSFTbuffer is a buffer with a fixed capacity and timeout value where
    // objects that have not been refreshed before the timeout period are not
    // occupying any space in the cache.

    /* -------- Thread Safety Argument -------- */
    // Used immutable and thread-safe datatypes and operations
    // Used locks when iterating
    // Mainly used local variables


    /**
     * Create a buffer with a fixed capacity and a timeout value.
     * Objects in the buffer that have not been refreshed within the
     * timeout period are removed from the cache.
     *
     * @param capacity the number of objects the buffer can hold
     * @param timeout  the duration, in seconds, an object should
     *                 be in the buffer before it times out
     */
    public FSFTBuffer(int capacity, int timeout) {
        this.maxSize = capacity;
        this.maxTime = Duration.ofSeconds(timeout);
        this.birthTime = Instant.now();
        this.lastUpdatedAt = birthTime;
        this.bufferMap = new ConcurrentHashMap<>();
    }

    /**
     * Create a buffer with default capacity and timeout values.
     */
    public FSFTBuffer() {
        this.maxSize = DSIZE;
        this.maxTime = Duration.ofSeconds(DTIMEOUT);
        this.birthTime = Instant.now();
        this.lastUpdatedAt = birthTime;
        this.bufferMap = new ConcurrentHashMap<>();
    }

    /**
     * Add a value to the buffer.
     * If the buffer is full then remove the least recently accessed
     * object to make room for the new object.
     */
    public boolean put(T t) {
        this.updateDurations();
        if (bufferMap.size() < maxSize) {
            bufferMap.put(t, Duration.ZERO);
            return true;
        } else {
            return this.replaceMostStaled(t);
        }
    }

    /**
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the
     * buffer
     */
    public T get(String id) throws NotFoundException {
        this.updateDurations();
        for (Map.Entry<T, Duration> entry : this.bufferMap.entrySet()) {
            if (this.isValidEntry(entry, id)) {
                return entry.getKey();
            }
        }
        throw new NotFoundException();
    }

    /**
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its
     * timeout is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise
     */
    public boolean touch(String id) {
        this.updateDurations();
        for (Map.Entry<T, Duration> entry : this.bufferMap.entrySet()) {
            if (this.isValidEntry(entry, id)) {
                this.bufferMap.replace(entry.getKey(), Duration.ZERO);
                return true;
            }
        }
        return false;
    }

    /**
     * Update an object in the buffer.
     * This method updates an object and acts like a "touch" to
     * renew the object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise
     */
    public boolean update(T t) {
        this.updateDurations();
        for (Map.Entry<T, Duration> entry : this.bufferMap.entrySet()) {
            if (entry.getKey().equals(t)) {
                if (!this.maxTime.minus(entry.getValue()).isNegative()) {
                    this.bufferMap.replace(t, Duration.ZERO);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Assumes there is an unstaled object that matches the given id, replace object with new t
     *
     * @param id id of the given unstaled object
     * @param t  new object t that will replace the object of given id
     * @return true if successful, false otherwise
     */
    public boolean replace(String id, T t) {
        T toReplace;
        for (Map.Entry<T, Duration> entry : this.bufferMap.entrySet()) {
            if (this.isValidEntry(entry, id)) {
                toReplace = entry.getKey();
                this.bufferMap.remove(toReplace);
                this.bufferMap.put(t, Duration.ZERO);
                return true;
            }
        }
        return false;
    }

    /**
     * Getter method to view buffer capacity
     *
     * @return int representing buffer capacity
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Getter method to view timeout duration
     *
     * @return int representing timeout duration
     */
    public Duration getMaxTime() {
        return maxTime;
    }

    /**
     * Getter method to view buffermap contents, mainly for testing, may threaten
     * proper functionality of FSFTbuffer but who cares for now
     *
     * @return a defensively-copied buffer to not threaten our datatype's validity
     */
    public synchronized ConcurrentMap<T, Duration> getBufferMap() {
        return new ConcurrentHashMap<>(this.bufferMap);
    }


    /**
     * Helpermethod to determine if buffer entires are valid.
     *
     * @param entry the buffer entry to check
     * @param id    the id to compare the buffer entry's id to
     * @return true if match, false otherwise
     */
    private boolean isValidEntry(Map.Entry<T, Duration> entry, String id) {
        return !this.maxTime.minus(entry.getValue()).isNegative()
            && entry.getKey().id().equals(id);
    }

    /**
     * Helper method to determine if there exists a most stale object, and replace if so.
     *
     * @param t object to replace if stale
     * @return true if successfully replaced
     */
    private synchronized boolean replaceMostStaled(T t) {
        Optional<Duration> mostStale = this.bufferMap.values().stream()
            .max(Comparator.comparing(Duration::getSeconds));

        if (mostStale.isPresent()) {
            T toBeReplaced;
            for (Map.Entry<T, Duration> entry : this.bufferMap.entrySet()) {
                if (entry.getValue() == mostStale.get()) {
                    ConcurrentHashMap<T, Duration> newmap = new ConcurrentHashMap<>();
                    newmap.putAll(bufferMap);
                  
                    toBeReplaced = entry.getKey();
                    newmap.remove(toBeReplaced);
                    newmap.put(t, Duration.ZERO);
                    bufferMap.clear();
                    bufferMap.putAll(newmap);
                    return true;
                }
            }
        }
        return false; // unreachable with valid uses of put
    }

    /**
     * Thread-safe method to lazily update duration of each entry in buffermap,
     */
    private synchronized void updateDurations() {
        checkRep();
        Instant current = Instant.now();
        Duration timeElapsed = Duration.between(lastUpdatedAt, current);
        ConcurrentHashMap<T, Duration> newmap = new ConcurrentHashMap<>();
        bufferMap.forEach((t, duration) ->
            newmap.put(t, duration.plus(timeElapsed)));
        bufferMap.clear();
        bufferMap.putAll(newmap);
        this.lastUpdatedAt = this.lastUpdatedAt.with(current);
    }

    /**
     * Method to check the validity of FSFTbuffer.
     * @return  true if FSFTbuffer is valid.
     */
    private boolean checkRep() {
        // checkRep must ensure that the representation is valid
        if (maxSize < 1) {
            return false;
        }
        if (maxTime.getSeconds() < 1) {
            return false;
        }

        for (T t : bufferMap.keySet()) {
            long time = bufferMap.get(t).getSeconds();
            if (time < 0) {
                return false;
            }
        }
        return true;
    }
}
