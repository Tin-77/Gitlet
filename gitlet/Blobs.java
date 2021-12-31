package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;

/**
 * A class for blobs and storing blobs contents.
 * @author Ting Qi
 */
public class Blobs implements Serializable {

    /**file content.*/
    private byte[] _blob;

    /**file name.*/
    private String _name;

    /**
     * Blobs constructor.
     * @param file is the file.
     */
    public Blobs(File file) {
        _blob = readContents(file);
        _name = sha1(_blob);
    }

    /**
     * Get the file name.
     * @return String.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the blob.
     * @return byte[].
     */
    public byte[] getBlob() {
        return _blob;
    }
}
