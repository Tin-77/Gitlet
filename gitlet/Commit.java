package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * A class for commit and storing all commits.
 * @author Ting Qi
 */
public class Commit implements Serializable {

    /** Message.*/
    private String message;

    /** Timestamp.*/
    private String _timestamp;

    /** Sha1.*/
    private String _sha1;

    /** Something that keeps track of what files.*/
    private HashMap<String, String> store = new HashMap<>();

    /** Parent.*/
    private String _parent;

    /** Parent2 for merging.*/
    private String _parent2;

    /**
     * Commit constructor.
     * @param msg is message.
     * @param parent is parent.
     * @param parent2 is parent2.
     * @param timestamp is timestamp.
     */
    public Commit(String msg, String parent, String parent2, Date timestamp) {
        message = msg;
        _parent = parent;
        _parent2 = parent2;
        SimpleDateFormat dt = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        dt.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        _timestamp = dt.format(timestamp);
    }

    /**
     * Get the message.
     * @return String.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the timestamp.
     * @return String.
     */
    public String getTimestamp() {
        return _timestamp;
    }

    /**
     * Get the parent.
     * @return String.
     */
    public String getParent() {
        return _parent;
    }

    /**
     * Get the store HashMap.
     * @return HashMap.
     */
    public HashMap getFile() {
        return store;
    }

    /**
     * Get the Sha1.
     * @return String.
     */
    public String getSha1() {
        return _sha1;
    }

    /**
     * Put sha1 to the name.
     * @param name is the name.
     * @param sha1 is the sha1.
     */
    public void put(String name, String sha1) {
        store.put(name, sha1);
    }

    /**
     * Remove the commit.
     * @param name is the name.
     */
    void remove(String name) {
        store.remove(name);
    }

    /**
     * Create Sha1.
     * @return String.
     */
    public String sha1() {
        _sha1 = "c" + Utils.sha1(_timestamp, message);
        return _sha1;
    }

    /** Print commit.*/
    public void print() {
        System.out.println("===");
        System.out.println("commit " + sha1());
        if (_parent2 != null) {
            System.out.println("Merge: " + getParent().substring(0, 7)
                    + " " + _parent2.substring(0, 7));
        }
        System.out.println("Date: " + _timestamp);
        System.out.println(message);
        System.out.println();
    }

}
