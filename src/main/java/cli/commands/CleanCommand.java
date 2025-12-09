package cli.commands;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Cleans build outputs by deleting the build directory.
 */
public class CleanCommand implements Command {

    private final Path workspaceRoot;
    private final boolean verbose;

    public CleanCommand(Path workspaceRoot, boolean verbose) {
        this.workspaceRoot = workspaceRoot;
        this.verbose = verbose;
    }

    @Override
    public void execute() throws Exception {
        Path buildDir = workspaceRoot.resolve("build");

        if (!Files.exists(buildDir)) {
            System.out.println("Nothing to clean - build directory does not exist.");
            return;
        }

        System.out.println("Cleaning build directory: " + buildDir.toAbsolutePath());

        // Delete directory recursively
        Files.walkFileTree(buildDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (verbose) {
                    System.out.println("  Deleting: " + file);
                }
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (verbose) {
                    System.out.println("  Deleting: " + dir);
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("✓ Clean complete");
    }
}
