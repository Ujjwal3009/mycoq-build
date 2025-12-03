package fs;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for discovering Java source files for a given source root.
 *
 * Example:
 *   srcRoot = /workspace/services/payment-service/src/main/java
 *   -> returns all *.java under this directory, recursively.
 */
public class SourceScanner {

    /**
     * Find all .java source files under the given srcRoot path.
     *
     * @param srcRoot root directory containing Java sources
     * @return list of Paths to .java files (may be empty, never null)
     */

    public List<Path> findJavaSources(Path srcRoot) {
        List<Path> result = new ArrayList<>();

        // If the directory doesn't exist, treat as "no sources"
        if (srcRoot == null || !Files.exists(srcRoot)) {
            return result;
        }

        try {
            Files.walk(srcRoot)
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                    .forEach(result::add);
        } catch (IOException e) {
            throw new RuntimeException("Error scanning sources in: " + srcRoot, e);
        }

        return result;
    }



}
