package cli.commands;

import com.myboq.manifest.graph.DependencyGraph;
import com.myboq.manifest.model.Node;
import com.myboq.manifest.model.NodeFactory;
import com.myboq.manifest.parser.ManifestParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shows the dependency graph and build order.
 */
public class GraphCommand implements Command {

    private final Path manifestDir;

    public GraphCommand(Path workspaceRoot, Path manifestDir, boolean verbose) {
        this.manifestDir = manifestDir;
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

        // Build dependency graph
        DependencyGraph graph = new DependencyGraph();
        nodes.forEach(graph::addNode);
        nodes.forEach(graph::addEdgesFor);

        // Get topological order
        List<String> buildOrder = graph.topologicalOrder();

        System.out.println("Dependency Graph:");
        System.out.println();

        for (Node node : nodes) {
            System.out.println("  " + node.getName() + " (" + node.getType() + ")");

            if (!node.getDependencyList().isEmpty()) {
                System.out.println("    depends on:");
                for (com.myboq.manifest.model.Dependency dep : node.getDependencyList()) {
                    System.out.println("      → " + dep.getName());
                }
            } else {
                System.out.println("    no dependencies");
            }

            System.out.println();
        }

        System.out.println("Build Order (topological sort):");
        System.out.println("  " + String.join(" → ", buildOrder));
    }
}
