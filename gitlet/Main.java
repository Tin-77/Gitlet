package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ting Qi
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains.
     *  <COMMAND> <OPERAND> ....tp
     *  java gitlet.Main add hello.txt*/
    public static void main(String... args) {
        Commands cmd = new Commands();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (args[0].equals("init")) {
            cmd.init();
        } else if (args[0].equals("log")) {
            cmd.log();
        } else if (args[0].equals("global-log")) {
            cmd.globalLog();
        } else if (args[0].equals("status")) {
            cmd.status();
        } else if (args[0].equals("add")) {
            cmd.add(args[1]);
        } else if (args[0].equals("commit")) {
            cmd.commit(args[1]);
        } else if (args[0].equals("rm")) {
            cmd.rm(args[1]);
        } else if (args[0].equals("find")) {
            cmd.find(args[1]);
        } else if (args[0].equals("checkout")) {
            cmd.checkout(args);
        } else if (args[0].equals("branch")) {
            cmd.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            cmd.rmBranch(args[1]);
        } else if (args[0].equals("reset")) {
            cmd.reset(args[1]);
        } else if (args[0].equals("merge")) {
            cmd.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }
}
