package Model;

import com.myboq.manifest.model.Node;
import com.myboq.manifest.model.NodeType;

import java.nio.file.Path;
import java.util.List;

/**
 * name → Node ka naam (e.g. "payment-service")
 *
 * type → EXECUTABLE / SHARED / COMPOSITE
 *
 * node → original Node object (agar kabhi extra info chahiye)
 *
 * sourceDir → jahan is target ka Java code pada hoga
 *
 * convention later decide: workspace/services/<name>/src/main/java
 *
 * outputDir → jahan iske compiled classes & jar jayenge
 *
 * e.g. workspace/build/payment-service/
 *
 * dependencyNames → ["auth-core", "logging-core"] style list
 *
 * isse BuildExecutor ko pata chalega:
 *
 * kaunse JAR iske classpath me add karne hain
 *
 * order verify kar sake
 *
 * Abhi BuildTarget sirf ek dumb data holder hai.
 */

public class BuildTarget {

    private final String name;
    private final NodeType type;

    public String getName() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    public NodeType getType() {
        return type;
    }

    public Path getSourceDir() {
        return sourceDir;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public List<String> getDependencyNames() {
        return dependencyNames;
    }

    private final Node node;

     // where to read Java sources from, e.g. workspace/services/<name>/src/main/java
    private final Path sourceDir;
    // where to place compiled classes & jar, e.g. workspace/build/<name>/
    private final Path outputDir;

    // names of dependent nodes (for classpath + order check)
    private final List<String> dependencyNames;

    public BuildTarget(Node node,
                       Path sourceDir,
                       Path outputDir,
                       List<String> dependencyNames) {
        this.name = node.getName();
        this.type = node.getType();
        this.node = node;
        this.sourceDir = sourceDir;
        this.outputDir = outputDir;
        this.dependencyNames = List.copyOf(dependencyNames);
    }



    @Override
    public String toString() {
        return "BuildTarget{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", sourceDir=" + sourceDir +
                ", outputDir=" + outputDir +
                ", dependencyNames=" + dependencyNames +
                '}';
    }


}
