package gitlet;

import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static gitlet.Utils.*;

/**
 * A class for all the commands that gitlet uses.
 * @author Ting Qi
 */
public class Commands {
    /**
     * Creates a new Gitlet version-control system in the current directory.
     */
    public void init() {
        File dir = new File(".gitlet");
        if (!dir.exists()) {
            dir.mkdir();
            new File(".gitlet/stage").mkdir();
            new File(".gitlet/objects").mkdir();
            new File(".gitlet/refs").mkdir();
            new File(".gitlet/refs/heads").mkdir();
            Commit initial = new Commit("initial commit", null, null,
                    new Date(0));
            String branch = "master";
            writeObject(join(".gitlet/objects", initial.sha1()), initial);
            writeContents(join(".gitlet/HEAD"), branch.getBytes());
            writeObject(join(".gitlet/refs/heads/master"), initial);
            writeObject(join(".gitlet/remove"), new Remove());
            Stage s = new Stage();
            writeObject(join(".gitlet/stage/index"), s);
        } else {
            System.out.println("A Gitlet version-control system"
                    + " already exists in the current directory.");
        }
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * @param name is the file name.
     */
    public void add(String name) {
        Stage s = readObject(join(".gitlet/stage/index"), Stage.class);
        File file = new File(name);
        Blobs b;
        Remove r = readObject(join(".gitlet/remove"), Remove.class);
        if (file.exists()) {
            if (Stage.changeNotStaged(name)
                    || Stage.trackedChanged(name) || !Stage.tracked(name)) {
                b = new Blobs(file);
                String path = ".gitlet/stage/";
                writeObject(join(path, b.getName()), b);
                s.put(name, b.getName());
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        if (Stage.tracked(name) && r.getfile().contains(name)) {
            r.remove(name);
            s.remove(name);
        }
        writeObject(join(".gitlet/stage/index"), s);
        writeObject(join(".gitlet/remove"), r);
    }

    /**
     * Saves a snapshot of certain files in the current commit and staging
     * area so they can be restored at a later time, creating a new commit.
     * @param msg is the commit message.
     */
    @SuppressWarnings("unchecked")
    public void commit(String msg) {
        if (msg == null || msg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        boolean changed = false;
        String head = readContentsAsString(join(".gitlet/HEAD"));
        Commit parent = readObject(join(".gitlet/refs/heads", head),
                Commit.class);
        Commit parent2 = null;
        Commit commit = new Commit(msg, parent.getSha1(), null, new Date());
        if (msg.split(" ")[0].equals("Merged")) {
            parent2 = readObject(join(".gitlet/refs/heads",
                    msg.split(" ")[1]), Commit.class);
            commit = new Commit(msg, parent.getSha1(), parent2.getSha1(),
                    new Date());
        }
        Set<String> set = (Set<String>) parent.getFile().keySet();
        for (String name : set) {
            commit.put(name, (String) parent.getFile().get(name));
        }
        Stage s = readObject(join(".gitlet/stage/index"), Stage.class);
        Set<String> keys = s.getFile().keySet();
        for (String name : keys) {
            changed = true;
            String sha1 = (String) s.getFile().get(name);
            Blobs b = readObject(join(".gitlet/stage", sha1), Blobs.class);
            commit.put(name, sha1);
            writeObject(join(".gitlet/objects", sha1), b);
        }
        Remove rm = readObject(join(".gitlet/remove"), Remove.class);
        for (String r : rm.getfile()) {
            changed = true;
            if (commit.getFile().get(r) != null) {
                commit.remove(r);
            }
        }
        rm = new Remove();
        if (!changed) {
            System.out.println("No changes added to the commit.");
            return;
        }
        writeObject(join(".gitlet/objects", commit.sha1()), commit);
        writeObject(join(".gitlet/refs/heads", head), commit);
        writeObject(join(".gitlet/remove"), rm);
        for (String f : plainFilenamesIn(".gitlet/stage")) {
            join(".gitlet/stage", f).delete();
        }
        writeObject(join(".gitlet/stage/index"), new Stage());
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it for removal
     * and remove the file from the working directory if the user has not
     * already done so (do not remove it unless it is tracked in
     * the current commit).
     * @param name is the file name.
     */
    public void rm(String name) {
        String head = readContentsAsString(join(".gitlet/HEAD"));
        Commit current = readObject(join(".gitlet/refs/heads", head),
                Commit.class);
        File record = new File(".gitlet/remove");
        Remove r = readObject(record, Remove.class);
        boolean find = false;
        if (current.getFile().get(name) != null) {
            r.add(name);
            if (join(name).exists()) {
                join(name).delete();
            }
            find = true;
        }
        if (join(".gitlet/stage/index").exists()) {
            Stage s = readObject(join(".gitlet/stage/index"), Stage.class);
            if (s.getFile().get(name) != null) {
                s.remove(name);
                writeObject(join(".gitlet/stage/index"), s);
                find = true;
            }
        }
        writeObject(join(".gitlet/remove"), r);
        if (!find) {
            System.out.println("No reason to remove the file.");
        }
    }

    /** Starting at the current head commit, display information about each
     *  commit backwards along the commit tree until the initial commit,
     *  following the first parent commit links, ignoring any second parents
     *  found in merge commits.*/
    public void log() {
        String head = readContentsAsString(join(".gitlet/HEAD"));
        Commit current = readObject(join(".gitlet/refs/heads", head),
                Commit.class);
        String path = ".gitlet/objects";
        String sha1 = current.getSha1();
        while (sha1 != null) {
            Commit commit = readObject(join(path, sha1), Commit.class);
            commit.print();
            sha1 = commit.getParent();
        }
    }

    /** Like log, except displays information about all commits ever made. */
    public void globalLog() {
        String path = ".gitlet/objects";
        List<String> all = plainFilenamesIn(path);
        for (String c : all) {
            if (c.charAt(0) == 'c') {
                Commit commit = readObject(join(path, c), Commit.class);
                commit.print();
            }
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message,
     * one per line.
     * @param msg is the commit message.
     */
    public void find(String msg) {
        String path = ".gitlet/objects";
        boolean find = false;
        for (String sha1 : plainFilenamesIn(path)) {
            if (join(path, sha1).exists() && sha1.charAt(0) == 'c') {
                Commit commit = readObject(join(path, sha1), Commit.class);
                if (commit.getMessage().equals(msg)) {
                    find = true;
                    System.out.println(sha1);
                }
            }
        }
        if (!find) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Displays what branches currently exist, and marks the current branch
     *  with a *. */
    @SuppressWarnings("unchecked")
    public void status() {
        File dir = new File(".gitlet");
        if (!dir.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        String head = readContentsAsString(join(".gitlet/HEAD"));
        System.out.println("=== Branches ===");
        List<String> branches = plainFilenamesIn(".gitlet/refs/heads");
        Collections.sort(branches);
        for (String branch : branches) {
            if (head.equals(branch)) {
                System.out.format("*%s\n", branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        Stage s = readObject(join(".gitlet/stage/index"), Stage.class);
        List<String> keys = new ArrayList<String>(s.getFile().keySet());
        Collections.sort(keys);
        System.out.println("=== Staged Files ===");
        for (String name : keys) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Remove d = readObject(join(".gitlet/remove"), Remove.class);
        for (String toDelete : d.getfile()) {
            System.out.println(toDelete);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String f : plainFilenamesIn(System.getProperty("user.dir"))) {
            if (Stage.trackedChanged(f) && s.getFile().get(f) == null) {
                System.out.format("%s (modified)\n", f);
            } else if (Stage.changeNotStaged(f)) {
                System.out.format("%s (modified)\n", f);
            }
        }
        Commit current = readObject(join(".gitlet/refs/heads", head),
                Commit.class);
        for (String f : (Set<String>) current.getFile().keySet()) {
            if (s.getFile().get(f) != null && !join(f).exists()) {
                System.out.format("%s (deleted)\n", f);
            } else if (!join(f).exists() && Stage.trackedDeleted(f)
                    && !d.getfile().contains(f)) {
                System.out.format("%s (deleted)\n", f);
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String f : plainFilenamesIn(System.getProperty("user.dir"))) {
            if (!Stage.tracked(f) && !Stage.staged(f)) {
                System.out.format("%s\n", f);
            }
        }
        System.out.println();
    }

    /**
     * Checkout is a kind of general command that can do a few different
     * things depending on what its arguments are.
     * @param args is the input args.
     */
    public void checkout(String[] args) {
        if (args[1].equals("--")) {
            String name = args[2];
            String head = readContentsAsString(join(".gitlet/HEAD"));
            Commit current = readObject(join(".gitlet/refs/heads", head),
                    Commit.class);
            String blobSha1 = (String) current.getFile().get(name);
            if (blobSha1 != null) {
                Blobs b = readObject(join(".gitlet/objects", blobSha1),
                        Blobs.class);
                writeContents(join(name), b.getBlob());
                return;
            } else {
                System.out.println("File does not exist in that commit.");
            }
        } else if (args.length == 4) {
            if (args[2].equals("--")) {
                String commitId = args[1];
                String name = args[3];
                String path = ".gitlet/objects";
                Boolean exsitId = false;
                for (String sha1 : plainFilenamesIn(path)) {
                    if (sha1.charAt(0) == 'c') {
                        Commit commit = readObject(join(path, sha1),
                                Commit.class);
                        if (commitId.regionMatches(true, 0,
                                sha1, 0, commitId.length())) {
                            exsitId = true;
                            if (commit.getFile().get(name) != null) {
                                String blobsha1 = (String)
                                        commit.getFile().get(name);
                                Blobs b = readObject(join(".gitlet/objects",
                                        blobsha1),
                                        Blobs.class);
                                writeContents(join(name), b.getBlob());
                                return;
                            }
                        }
                    }
                }
                if (exsitId) {
                    System.out.println("File does not exist in that commit.");
                } else {
                    System.out.println("No commit with that id exists.");
                }
            } else {
                System.out.println("Incorrect operands.");
            }
        } else {
            checkout(args[1]);
        }
    }

    /**
     * Takes all files in the commit at the head of the given branch, and
     * puts them in the working directory, overwriting the versions of the
     * files that are already there if they exist.
     * @param bran is the given branch.
     */
    @SuppressWarnings("unchecked")
    private void checkout(String bran) {
        String currentBranch = readContentsAsString(join(".gitlet/HEAD"));
        Commit current = readObject(join(".gitlet/refs/heads",
                currentBranch), Commit.class);
        if (bran.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        if (join(".gitlet/refs/heads", bran).exists()) {
            Commit given = readObject(join(".gitlet/refs/heads",
                    bran), Commit.class);
            Set<String> keys = (Set<String>) given.getFile().keySet();
            Set<String> keysCurrent = (Set<String>) current.getFile().keySet();
            for (String f : plainFilenamesIn(System.getProperty("user.dir"))) {
                if (Stage.changeNotStaged(f)
                        || Stage.trackedChanged(f) || !Stage.tracked(f)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    return;
                }
            }
            for (String name : keys) {
                String sha1 = (String) given.getFile().get(name);
                Blobs b = readObject(join(".gitlet/objects", sha1),
                        Blobs.class);
                writeContents(join(System.getProperty("user.dir"), name),
                        b.getBlob());
            }
            for (String name : keysCurrent) {
                if (!keys.contains(name)) {
                    join(System.getProperty("user.dir"), name).delete();
                }
            }
            writeContents(join(".gitlet/HEAD"), bran.getBytes());
            for (String f : plainFilenamesIn(".gitlet/stage")) {
                join(".gitlet/stage", f).delete();
            }
            writeObject(join(".gitlet/stage/index"), new Stage());
        } else {
            System.out.println("No such branch exists.");
        }
    }

    /**
     * Creates a new branch with the given name, and points it at the
     * current head node.
     * @param name is the branch name.
     */
    public void branch(String name) {
        File branch = join(".gitlet/refs/heads", name);
        if (name.split("/").length == 2) {
            join(".gitlet/refs/heads", name
                    .split("/")[0]).mkdir();
        }
        if (!branch.exists()) {
            String head = readContentsAsString(join(".gitlet/HEAD"));
            Commit current = readObject(join(".gitlet/refs/heads", head),
                    Commit.class);
            writeObject(branch, current);
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    /**
     * Deletes the branch with the given name.
     * @param name is the branch name.
     */
    public void rmBranch(String name) {
        String head = readContentsAsString(join(".gitlet/HEAD"));
        String path = ".gitlet/refs/heads";
        if (join(path, name).exists()) {
            if (!name.equals(head)) {
                (join(path, name)).delete();
            } else {
                System.out.println("Cannot remove the current branch.");
            }
        } else {
            System.out.println("A branch with that name does not exist.");
        }
    }

    /**
     * Checks out all the files tracked by the given commit.
     * @param id is the commit id.
     */
    @SuppressWarnings("unchecked")
    public void reset(String id) {
        String head = readContentsAsString(join(".gitlet/HEAD"));
        String path = ".gitlet/objects";
        boolean find = false;
        for (String sha1 : plainFilenamesIn(path)) {
            if (sha1.charAt(0) == 'c') {
                if (id.regionMatches(true, 0,
                        sha1, 0, id.length())) {
                    find = true;
                    Commit commit = readObject(join(path, sha1), Commit.class);
                    Set<String> given = commit.getFile().keySet();
                    for (String f : plainFilenamesIn(System.getProperty
                            ("user.dir"))) {
                        if (Stage.changeNotStaged(f)
                            || Stage.trackedChanged(f) || !Stage.tracked(f)) {
                            System.out.println("There is an untracked file "
                                    + "in the way; delete it, "
                                    + "or add and commit it first.");
                            return;
                        }
                    }
                    for (String name : given) {
                        String[] file = new String[4];
                        file[1] = commit.getSha1();
                        file[2] = "--";
                        file[3] = name;
                        checkout(file);
                    }
                    for (String f : plainFilenamesIn(System.getProperty
                            ("user.dir"))) {
                        if (!given.contains(f)) {
                            join(f).delete();
                        }
                    }
                    writeObject(join(".gitlet/refs/heads", head), commit);
                    writeObject(join(".gitlet/stage/index"), new Stage());
                }
            }
        }
        if (!find) {
            System.out.println("No commit with that id exists.");
        }
    }

    /**
     * Merges files from the given branch into the current branch.
     * @param bran is the given branch.
     */
    @SuppressWarnings("unchecked")
    public void merge(String bran) {
        String givenBranch = bran;
        String currentBranch = readContentsAsString(join(".gitlet/HEAD"));
        String path = ".gitlet/refs/heads";
        if (!join(path, givenBranch).exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currentBranch.equals(givenBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Stage s = readObject(join(".gitlet/stage/index"), Stage.class);
        Remove r = readObject(join(".gitlet/remove"), Remove.class);
        if (!s.getFile().isEmpty() || !r.getfile().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        Commit given = readObject(join(path, givenBranch), Commit.class);
        Commit current = readObject(join(path, currentBranch), Commit.class);
        Set<String> keysGiven = given.getFile().keySet();
        Set<String> keysCurrent = current.getFile().keySet();
        for (String f : plainFilenamesIn(System.getProperty("user.dir"))) {
            if (Stage.changeNotStaged(f)
                    || Stage.trackedChanged(f) || !Stage.tracked(f)) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
                return;
            }
        }
        merge(current, given,
                currentBranch, givenBranch, keysCurrent, keysGiven);
    }

    /**
     * Merges help function.
     * @param current is the current commit.
     * @param given is the given commit.
     * @param currentBranch is the current branch.
     * @param givenBranch is the given branch.
     * @param keysCurrent is the current keys of a list.
     * @param keysGiven is the given keys of a list.
     */
    private void merge(Commit current, Commit given,
                       String currentBranch, String givenBranch,
                       Set<String> keysCurrent, Set<String> keysGiven) {
        String pathCommit = ".gitlet/objects";
        ArrayList<String> pastCommit1 = new ArrayList<>();
        ArrayList<String> pastCommit2 = new ArrayList<>();
        String currentSha1 = current.getSha1();
        while (currentSha1 != null) {
            pastCommit1.add(currentSha1);
            current = readObject(join(pathCommit, currentSha1), Commit.class);
            currentSha1 = current.getParent();
        }
        if (pastCommit1.contains(given.getSha1())) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return;
        }
        String givenSha1 = given.getSha1();
        Commit split = null;
        while (join(pathCommit, givenSha1).exists()) {
            pastCommit2.add(givenSha1);
            if (givenSha1.equals(pastCommit1.get(0))) {
                System.out.println("Current branch fast-forwarded.");
                writeObject(join(".gitlet/refs/heads", currentBranch),
                        pastCommit2.get(0));
                return;
            } else if (pastCommit1.contains(givenSha1)) {
                split = readObject(join(pathCommit, givenSha1),
                        Commit.class);
                break;
            }
            given = readObject(join(pathCommit, givenSha1), Commit.class);
            givenSha1 = given.getParent();
        }
        merge(currentBranch, givenBranch, split, keysCurrent, keysGiven);
    }

    /**
     * Merges help function.
     * @param currentBranch is the current branch.
     * @param givenBranch is the given branch.
     * @param split is the commit where two branches splits.
     * @param keysCurrent is the current keys of a list.
     * @param keysGiven is the given keys of a list.
     */
    @SuppressWarnings("unchecked")
    private void merge(String currentBranch, String givenBranch,
                       Commit split, Set keysCurrent, Set keysGiven) {
        Set<String> keys = split.getFile().keySet();
        boolean conflict = false;
        String path = ".gitlet/refs/heads";
        String pathCommit = ".gitlet/objects";
        Commit given = readObject(join(path, givenBranch), Commit.class);
        Commit current = readObject(join(path, currentBranch), Commit.class);
        for (String name : keys) {
            if (!split.getFile().get(name).equals(given.getFile().get(name))
                    && split.getFile().get(name).equals
                    (current.getFile().get(name))) {
                if (given.getFile().get(name) == null) {
                    String[] fileToD = new String[1];
                    fileToD[0] = (String) split.getFile().get(name);
                    rm(name);
                } else {
                    Blobs b = readObject(join(".gitlet/objects",
                            (String) given.getFile().get(name)), Blobs.class);
                    writeContents(join(name), b.getBlob());
                    add(name);
                }
            } else if (!split.getFile().get(name)
                    .equals(given.getFile().get(name))
                    && !split.getFile().get(name)
                    .equals(current.getFile().get(name))) {
                conflict = checkConflict(given, current, name,
                        pathCommit, conflict);
            }
        }
        for (String name : (Set<String>) keysGiven) {
            if (!keys.contains(name) && !keysCurrent.contains(name)) {
                String[] file = new String[4];
                file[1] = given.getSha1();
                file[2] = "--";
                file[3] = name;
                checkout(file);
                add(name);
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        String s = String.format("Merged %s into %s. ", givenBranch,
                currentBranch);
        commit(s);
    }

    /**
     * Return true if it has conflict.
     * @param given is the given commit.
     * @param current is the current commit.
     * @param name is the file name.
     * @param pathCommit is the direction.
     * @param conflict is a boolean variable.
     * @return boolean.
     */
    private boolean checkConflict(Commit given, Commit current, String name,
                                  String pathCommit, boolean conflict) {
        if (given.getFile().get(name) != null
                && current.getFile().get(name) != null
                && !given.getFile().get(name)
                .equals(current.getFile().get(name))) {
            Blobs givenB = readObject(join(pathCommit,
                    (String) given.getFile().get(name)), Blobs.class);
            Blobs currentB = readObject(join(pathCommit,
                    (String) current.getFile().get(name)), Blobs.class);
            writeContents(join(System.getProperty("user.dir"), name),
                    "<<<<<<< HEAD\n",
                    currentB.getBlob(), "=======\n", givenB.getBlob(),
                    ">>>>>>>\n");
            add(name);
            conflict = true;
        } else if (given.getFile().get(name) == null
                && current.getFile().get(name) != null) {
            Blobs currentB = readObject(join(pathCommit,
                    (String) current.getFile().get(name)), Blobs.class);
            writeContents(join(System.getProperty("user.dir"), name),
                    "<<<<<<< HEAD\n",
                    currentB.getBlob(), "=======\n", ">>>>>>>\n");
            add(name);
            conflict = true;
        } else if (given.getFile().get(name) != null
                && current.getFile().get(name) == null) {
            Blobs givenB = readObject(join(pathCommit,
                    (String) given.getFile().get(name)), Blobs.class);
            writeContents(join(System.getProperty("user.dir"), name),
                    "<<<<<<< HEAD\n", "=======\n", givenB.getBlob(),
                    ">>>>>>>\n");
            add(name);
            conflict = true;
        }
        return conflict;
    }
}
