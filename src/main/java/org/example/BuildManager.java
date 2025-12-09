package org.example;

import Model.BuildTarget;
import Model.BuildTargetFactory;
import com.myboq.manifest.model.Node;
import com.myboq.manifest.model.NodeFactory;
import com.myboq.manifest.parser.ManifestParser;
import compile.JavaCompileService;
import exec.BuildExecutor;
import fs.SourceScanner;
import jar.JarPackager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * org.example.BuildManager:
 * 1. Load manifests
 * 2. Convert Manifest → Node
 * 3. Convert Node → BuildTarget
 * 4. Invoke BuildExecutor to run entire build
 */
public class BuildManager {
    /**
     * Build entire workspace.
     *
     * @param workspaceRoot /absolute/path/to/workspace
     * @param manifestDir   workspaceRoot/manifests
     */
    public void build(Path workspaceRoot, Path manifestDir) throws Exception {

        ManifestParser parser = new ManifestParser();

        System.out.println("=== LOADING MANIFESTS ===");
        System.out.println("Manifest directory: " + manifestDir.toAbsolutePath());

        // 1. Load all YAML manifest files
        List<Node> nodes = Files.list(manifestDir)
                .filter(p -> p.toString().endsWith(".yaml"))
                .sorted()
                .map(parser::parse) // YAML → Manifest
                .map(NodeFactory::fromManifest) // Manifest → Node
                .collect(Collectors.toList());

        System.out.println("Nodes loaded: ");
        nodes.forEach(n -> System.out.println(" - " + n.getName()));

        // 2. Node → BuildTarget
        BuildTargetFactory targetFactory = new BuildTargetFactory(workspaceRoot);

        Map<String, BuildTarget> targetsByName = nodes.stream()
                .map(targetFactory::fromNode)
                .collect(Collectors.toMap(BuildTarget::getName, t -> t));

        System.out.println("\n=== STARTING BUILD ===");

        // 3. Create helpers
        JavaCompileService compiler = new JavaCompileService();
        SourceScanner scanner = new SourceScanner();
        JarPackager packager = new JarPackager();

        // 4. Execute build
        BuildExecutor executor = new BuildExecutor(compiler, scanner, packager);
        executor.execute(nodes, targetsByName, workspaceRoot.resolve("build"));

        System.out.println("\n=== BUILD DONE SUCCESSFULLY ===");
    }

    /**
     * Build a specific target and its dependencies.
     *
     * @param workspaceRoot /absolute/path/to/workspace
     * @param manifestDir   workspaceRoot/manifests
     * @param targetName    name of the target to build
     */
    public void buildTarget(Path workspaceRoot, Path manifestDir, String targetName) throws Exception {
        ManifestParser parser = new ManifestParser();

        System.out.println("=== LOADING MANIFESTS ===");
        System.out.println("Manifest directory: " + manifestDir.toAbsolutePath());

        // 1. Load all YAML manifest files
        List<Node> allNodes = Files.list(manifestDir)
                .filter(p -> p.toString().endsWith(".yaml"))
                .sorted()
                .map(parser::parse)
                .map(NodeFactory::fromManifest)
                .collect(Collectors.toList());

        // 2. Find the target node
        Node targetNode = allNodes.stream()
                .filter(n -> n.getName().equals(targetName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target not found: " + targetName));

        System.out.println("Target found: " + targetNode.getName() + " (" + targetNode.getType() + ")");

        // 3. Build dependency graph to find all dependencies
        com.myboq.manifest.graph.DependencyGraph graph = new com.myboq.manifest.graph.DependencyGraph();
        allNodes.forEach(graph::addNode);
        allNodes.forEach(graph::addEdgesFor);

        // 4. Get topological order and filter to include only target and its
        // dependencies
        List<String> fullOrder = graph.topologicalOrder();
        int targetIndex = fullOrder.indexOf(targetName);
        List<String> requiredNodes = fullOrder.subList(0, targetIndex + 1);

        System.out.println("Building target and dependencies: " + requiredNodes);

        // 5. Filter nodes to only those required
        List<Node> nodesToBuild = allNodes.stream()
                .filter(n -> requiredNodes.contains(n.getName()))
                .collect(Collectors.toList());

        // 6. Node → BuildTarget
        BuildTargetFactory targetFactory = new BuildTargetFactory(workspaceRoot);
        Map<String, BuildTarget> targetsByName = nodesToBuild.stream()
                .map(targetFactory::fromNode)
                .collect(Collectors.toMap(BuildTarget::getName, t -> t));

        System.out.println("\n=== STARTING BUILD ===");

        // 7. Create helpers
        JavaCompileService compiler = new JavaCompileService();
        SourceScanner scanner = new SourceScanner();
        JarPackager packager = new JarPackager();

        // 8. Execute build
        BuildExecutor executor = new BuildExecutor(compiler, scanner, packager);
        executor.execute(nodesToBuild, targetsByName, workspaceRoot.resolve("build"));

        System.out.println("\n=== BUILD DONE SUCCESSFULLY ===");
    }
}
