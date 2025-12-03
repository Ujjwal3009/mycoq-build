package compile;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiles Java source files into .class files using the built-in JavaCompiler API.
 */
public class JavaCompileService {

    private final JavaCompiler compiler;

    public JavaCompileService() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            throw new IllegalStateException(
                    "No system JavaCompiler found. You must run MyBoq using a JDK, not a JRE."
            );
        }
    }
    /**
     * Compile a list of .java files into the given classesDir.
     * @param sources list of .java Path files
     * @param classesDir output directory for .class files
     * @param classpathJars list of dependent jar files for classpath
     */
    public void compile(List<Path> sources, Path classesDir, List<Path> classpathJars) {

        // No Java files found — skip quietly
        if (sources.isEmpty()) {
            System.out.println("[compile] No sources in: " + classesDir + " (skipped)");
            return;
        }

        try {
            Files.createDirectories(classesDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create output directory: " + classesDir, e);
        }

        // Compiler options
        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(classesDir.toString());  // where .class files should go

        // Add classpath when deps exist
        if (!classpathJars.isEmpty()) {
            String cp = classpathJars.stream()
                    .map(Path::toString)
                    .reduce((a, b) -> a + System.getProperty("path.separator") + b)
                    .orElse("");
            options.add("-classpath");
            options.add(cp);
        }

        // FileManager + Java file objects
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(
                        sources.stream().map(Path::toFile).toList()
                );

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, null, options, null, compilationUnits
        );

        System.out.println("[compile] Compiling " + sources.size() + " sources → " + classesDir);

        boolean success = task.call();

        try {
            fileManager.close();
        } catch (IOException ignored) {}

        if (!success) {
            throw new RuntimeException("Compilation FAILED for: " + classesDir);
        }

        System.out.println("[compile] SUCCESS for: " + classesDir);
    }

}
