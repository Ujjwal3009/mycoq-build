package cli.commands;

import org.example.BuildManager;

import java.nio.file.Path;

/**
 * Builds all targets or a specific target.
 */
public class BuildCommand implements Command {

    private final Path workspaceRoot;
    private final Path manifestDir;
    private final String targetName;

    public BuildCommand(Path workspaceRoot, Path manifestDir, String targetName, boolean verbose) {
        this.workspaceRoot = workspaceRoot;
        this.manifestDir = manifestDir;
        this.targetName = targetName;
    }

    @Override
    public void execute() throws Exception {
        BuildManager manager = new BuildManager();

        if (targetName == null) {
            // Build all targets
            System.out.println("Building all targets...");
            System.out.println();
            manager.build(workspaceRoot, manifestDir);
        } else {
            // Build specific target
            System.out.println("Building target: " + targetName);
            System.out.println();
            manager.buildTarget(workspaceRoot, manifestDir, targetName);
        }
    }
}
