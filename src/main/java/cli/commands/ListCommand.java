package cli.commands;

import com.myboq.manifest.model.Node;
import com.myboq.manifest.model.NodeFactory;
import com.myboq.manifest.parser.ManifestParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists all available build targets from manifest files.
 */
public class ListCommand implements Command {

    private final Path manifestDir;
    private final boolean verbose;

    public ListCommand(Path workspaceRoot, Path manifestDir, boolean verbose) {
        this.manifestDir = manifestDir;
        this.verbose = verbose;
    }

    @Override
    public void execute() throws Exception {
        ManifestParser parser = new ManifestParser();

        // Load all manifest files
        List<Node> nodes = Files.list(manifestDir)
                .filter(p -> p.toString().endsWith(".yaml"))
                .sorted()
                .map(parser::parse)
                .map(NodeFactory::fromManifest)
                .collect(Collectors.toList());

        System.out.println("Available build targets:");
        System.out.println();

        for (Node node : nodes) {
            System.out.println("  " + node.getName());
            System.out.println("    Type: " + node.getType());

            if (!node.getDependencyList().isEmpty()) {
                String deps = node.getDependencyList().stream()
                        .map(d -> d.getName())
                        .collect(java.util.stream.Collectors.joining(", "));
                System.out.println("    Dependencies: " + deps);
            } else {
                System.out.println("    Dependencies: none");
            }

            if (verbose) {
                System.out.println("    Version: " + node.getVersion());
            }

            System.out.println();
        }

        System.out.println("Total targets: " + nodes.size());
    }
}
