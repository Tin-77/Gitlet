package gitlet;

import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Utils.*;

/**
 * A class for staging and checking the staging status.
 * @author Ting Qi
 */
public class Stage implements Serializable {

    /** constructor.*/
    Stage() {
        _current = new HashMap<>();
    }

    /**
     * Put the file name file sha1 into current HashMap.
     * @param name is the name.
     * @param sha1 is the sha1
     */
    void put(String name, String sha1) {
        _current.put(name, sha1);
    }

    /**
     * Remove the file.
     * @param name is the name.
     */
    void remove(String name) {
        _current.remove(name);
    }

    /**
     * Get the current file.
     * @return HashMap.
     */
    HashMap getFile() {
        return _current;
    }

    /**
     * Check if it is tracked.
     * @param name is the name.
     * @return boolean.
     */
    public static boolean tracked(String name) {
        String head = readContentsAsString(join(".gitlet/HEAD"));
        Commit current = readObject(join(".gitlet/refs/heads", head),
                Commit.class);
        return current.getFile().get(name) != null;
    }

    /**
     * Check if it is staged.
     * @param name is the name.
     * @return boolean.
     */
    public static boolean staged(String name) {
        Stage s = readObject(join(".gitlet/stage/index"), Stage.class);
        return s.getFile().get(name) != null;
    }

    /**
     * Check if any changes are not staged.
     * @param name is the name.
     * @return boolean.
     */
    public static boolean changeNotStaged(String name) {
        Stage s = readObject(join(".gitlet/stage/index"), Stage.class);
        String newsha1 = sha1(readContentsAsString(join(name)));
        return s.getFile().get(name) != null
                && !s.getFile().get(name).equals(newsha1);
    }

    /**
     * Check if it is tracked but changed.
     * @param name is the name.
     * @return boolean.
     */
    public static boolean trackedChanged(String name) {
        String head = readContentsAsString(join(".gitlet/HEAD"));
        Commit current = readObject(join(".gitlet/refs/heads", head),
                Commit.class);
        String newSha1 = sha1(readContentsAsString(join(name)));
        return current.getFile().get(name) != null
                && !current.getFile().get(name).equals(newSha1);
    }

    /**
     * Check if it is tracked but removed.
     * @param name is the name.
     * @return boolean.
     */
    public static boolean trackedDeleted(String name) {
        String head = readContentsAsString(join(".gitlet/HEAD"));
        Commit current = readObject(join(".gitlet/refs/heads", head),
                Commit.class);
        return current.getFile().get(name) != null
                && !plainFilenamesIn(System.getProperty("user.dir"))
                .contains(name);
    }

    /** Stores a list of blobs.*/
    private HashMap<String, String> _current;
}
