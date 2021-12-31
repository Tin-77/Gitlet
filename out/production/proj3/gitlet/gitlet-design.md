# Gitlet Design Document

**Name**: Ting Qi

## Classes and Data Structures


### Branch 

Fields: string file name, string sha-1, byte[] contents.

### Blob 

Fields: ArrrayList<string> blob name.

### Remove 

Fields: string file name, string sha-1, byte[] contents.

### Commit 

Fields: HashMap of SHA-1 id and name, log message, timestamp, parent(s).

### Main
 
Fields: Branches branches, Staging staging.

History 

Fields: Commits commits.

### UnitTest 

Where all the small tests for functions will be written.

### Utils 

Given class to help with serialization or deserialization.

### Remote 

Fields: String name, branch, history.

### Staging 

Fields: Branch branches.


## Algorithms

Blob 

Reads a text file, and deserializes its content to store in a byte array. 
Each blob should have the file name, deserialized contents and a unique sha1 id.

Branch 

Creates a new branch with the given name, and points it at the current head node. 
A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.

Commit 

Sets a commit's (blob) sha1 id, timestamp, contents, log message, parent (or parent(s) if it's a merge). 
Has a list of the history of commits too. Stores sha1 id and file names in a hashmap, 
for easy accessing and to prevent duplicates. Will have to deal with overwriting (?) on main maybe.

Main 

Main will have the command functions like add(), log(), status(), merge(), push(), pull(),fetch(), 
checkout(), reset(), etc. those operations can be performed on a the current branch. during parsing, 
it encounters an failure error we should print our the corresponding message and exit with code zero.

Remote 

This is for the extra credit. Should add, remove remotes. Should be able to read commits and get their history, 
timestamp, name, sha1 id, and other fields. Not sure what to do for remote yet.

Staging 
This is the staging area, where we take in a branch and we will either remove, or add. Maybe keep track of what 
has been staged and what hasn't been staged so this will help the rm and status or log functions on Main.


## Persistence

In order to persist the settings of the current work directory, we will need to keep track of several fields 
that the current head has. For my .gitlet directory where the tracking happens, I plan to have:
branch, head, objects, stage as sub directories.

branch: to keep track of existing branches in our directory.

head: so we don't lose the pointer to head, or the current branch we are on.

objects: keep track of the blobs/files we have committed thus far.

stage: keep track of what has been staged/not yet staged, so we know what and when to add/remove/perform an action.
