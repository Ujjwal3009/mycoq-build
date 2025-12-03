package exec;

import Model.BuildTarget;
import com.myboq.manifest.graph.DependencyGraph;
import com.myboq.manifest.model.Node;
import com.myboq.manifest.model.NodeType;
import compile.JavaCompileService;
import fs.SourceScanner;
import jar.JarPackager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * BuildExecutor ties everything together.
 * It:
 *   - loads dependency graph
 *   - computes topological order
 *   - compiles sources
 *   - packages jar
 *   - stores jar paths for dependent targets
 */
public class BuildExecutor {

    private final JavaCompileService compiler;
    private final SourceScanner scanner;
    private final JarPackager packager;

    public BuildExecutor(JavaCompileService compiler,
                         SourceScanner scanner,
                         JarPackager packager) {
        this.compiler = compiler;
        this.scanner = scanner;
        this.packager = packager;
    }

    /**
     * Execute build for all given nodes.
     *
     * @param nodes         nodes loaded from manifest
     * @param targetsByName map of BuildTarget by name
     * @param outputRoot    /workspace/build
     */
    public void execute(List<Node> nodes,
                        Map<String, BuildTarget> targetsByName,
                        Path outputRoot) throws Exception {

        System.out.println("\n=== STAGE 1: BUILD GRAPH ===");

        // 1. Graph creation
        DependencyGraph graph = new DependencyGraph();
        nodes.forEach(graph::addNode);
        nodes.forEach(graph::addEdgesFor);

        // 2. Topological sorting
        List<String> order = graph.topologicalOrder();

        System.out.println("Build order: " + order);

        System.out.println("\n=== STAGE 2: BUILD TARGETS ===");

        // Tracks jar output of each node for classpath usage
        Map<String, Path> jarByNode = new HashMap<>();

        // 3. Build in graph order
        for (String nodeName : order) {

            BuildTarget target = targetsByName.get(nodeName);
            Node node = target.getNode();

            System.out.println("\n-- Building: " + nodeName + " (" + node.getType() + ")");

            // COMPOSITE has no build output
            if (node.getType() == NodeType.COMPOSITE) {
                System.out.println("   [skip] COMPOSITE node (no jar)");
                continue;
            }

            // 3.1 dependency jars for classpath
            List<Path> depJars = target.getDependencyNames().stream()
                    .map(jarByNode::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 3.2 scan sources
            var sources = scanner.findJavaSources(target.getSourceDir());
            System.out.println("   Sources found: " + sources.size());

            // 3.3 compile → classes/
            Path classesDir = target.getOutputDir().resolve("classes");
            compiler.compile(sources, classesDir, depJars);

            // 3.4 package jar
            Path jarPath = target.getOutputDir().resolve(target.getName() + ".jar");

            String mainClass = null;  // future: read from manifest

            packager.createJar(classesDir, jarPath, mainClass);

            // 3.5 record jar for dependents to use
            jarByNode.put(nodeName, jarPath);

            System.out.println("   JAR: " + jarPath.toAbsolutePath());
        }

        System.out.println("\n=== BUILD COMPLETE ===");
        System.out.println("Output directory: " + outputRoot.toAbsolutePath());
    }
}
