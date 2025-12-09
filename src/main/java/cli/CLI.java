package cli;

import cli.commands.Command;

import java.nio.file.Path;

/**
 * Main CLI entry point for mycoq-build.
 * Handles command-line argument parsing and execution.
 */
public class CLI {

    private final Path workspaceRoot;
    private final Path manifestDir;

    public CLI(Path workspaceRoot, Path manifestDir) {
        this.workspaceRoot = workspaceRoot;
        this.manifestDir = manifestDir;
    }

    /**
     * Run the CLI with the given arguments.
     * 
     * @param args command-line arguments
     */
    public void run(String[] args) {
        try {
            CommandParser parser = new CommandParser(workspaceRoot, manifestDir);
            Command command = parser.parse(args);
            command.execute();
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Build failed: " + e.getMessage());
            if (System.getProperty("verbose") != null) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}
