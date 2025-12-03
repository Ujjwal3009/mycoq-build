package Model;

import com.myboq.manifest.model.Dependency;
import com.myboq.manifest.model.Node;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class BuildTargetFactory {

    private final Path workspaceRoot;
    private final Path outputRoot;

    public BuildTargetFactory(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
        this.outputRoot = workspaceRoot.resolve("build");
    }

    /**
     * Convert Node -> BuildTarget using fixed workspace conventions.
     */
    public BuildTarget fromNode(Node node) {
        // Convention: sources under /services/<name>/src/main/java

        Path sourceDir = workspaceRoot
                .resolve("services")
                .resolve(node.getName())
                .resolve("src/main/java");
        // Output: /build/<name>/
        Path outputDir = outputRoot.resolve(node.getName());

        // Only names of dependencies (we will resolve jar paths later)
        List<String> depNames = node.getDependencyList()
                .stream()
                .map(Dependency::getName)
                .toList();

        return new BuildTarget(node, sourceDir, outputDir, depNames);



    }


}
