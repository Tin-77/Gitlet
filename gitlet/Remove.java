package gitlet;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A class for removing files.
 * @author Ting Qi
 */
public class Remove implements Serializable {

    /** A new Remove, using Current Blobs.*/
    Remove() {
        _current = new ArrayList<String>();
    }

    /**
     * Add the file.
     * @param name is the name.
     */
    void add(String name) {
        _current.add(name);
    }

    /**
     * Remove the file.
     * @param name is the name.
     */
    void remove(String name) {
        _current.remove(name);
    }

    /**
     * Get the list of blobs.
     * @return ArrayList.
     */
    ArrayList<String> getfile() {
        return _current;
    }

    /** Stores a list of blobs.*/
    private ArrayList<String> _current;
}
