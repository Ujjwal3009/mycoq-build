package cli;

import cli.commands.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Parses command-line arguments and creates appropriate Command objects.
 */
public class CommandParser {

    private Path workspaceRoot;
    private Path manifestDir;
    private boolean verbose;

    public CommandParser(Path workspaceRoot, Path manifestDir) {
        this.workspaceRoot = workspaceRoot;
        this.manifestDir = manifestDir;
        this.verbose = false;
    }

    /**
     * Parse command-line arguments and return the appropriate Command.
     * 
     * @param args command-line arguments
     * @return Command to execute
     */
    public Command parse(String[] args) {
        if (args.length == 0) {
            // Default: build all targets
            return new BuildCommand(workspaceRoot, manifestDir, null, verbose);
        }

        String commandName = args[0].toLowerCase();
        List<String> remainingArgs = Arrays.asList(args).subList(1, args.length);

        // Parse flags from remaining args
        parseFlags(remainingArgs);

        // Remove flags from args to get positional arguments
        List<String> positionalArgs = remainingArgs.stream()
                .filter(arg -> !arg.startsWith("--"))
                .toList();

        return switch (commandName) {
            case "build" -> {
                String target = positionalArgs.isEmpty() ? null : positionalArgs.get(0);
                yield new BuildCommand(workspaceRoot, manifestDir, target, verbose);
            }
            case "clean" -> new CleanCommand(workspaceRoot, verbose);
            case "list" -> new ListCommand(workspaceRoot, manifestDir, verbose);
            case "graph" -> new GraphCommand(workspaceRoot, manifestDir, verbose);
            case "help", "--help", "-h" -> new HelpCommand();
            default -> throw new IllegalArgumentException(
                    "Unknown command: " + commandName + "\nUse 'help' to see available commands.");
        };
    }

    /**
     * Parse flags from arguments.
     */
    private void parseFlags(List<String> args) {
        for (String arg : args) {
            if (arg.equals("--verbose") || arg.equals("-v")) {
                verbose = true;
            }
            // Future: add more flags like --workspace, --manifest-dir
        }
    }
}
