package cli.commands;

/**
 * Interface for all CLI commands.
 * Each command implements its own execution logic.
 */
public interface Command {
    /**
     * Execute the command.
     * 
     * @throws Exception if command execution fails
     */
    void execute() throws Exception;
}
