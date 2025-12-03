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
 *  1. Load manifests
 *  2. Convert Manifest → Node
 *  3. Convert Node → BuildTarget
 *  4. Invoke BuildExecutor to run entire build
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
                .map(parser::parse)         // YAML → Manifest
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
}
