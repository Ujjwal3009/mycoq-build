package runtime;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceClassLoader is responsible for loading service and module JARs
 * dynamically.
 * 
 * CONCEPT: ClassLoaders
 * ----------------------
 * In Java, a ClassLoader loads .class files into memory. By default, Java uses
 * the Application ClassLoader to load your code at startup.
 * 
 * But we need to load JARs AFTER the program has started (at runtime).
 * That's what this custom ClassLoader does.
 * 
 * ISOLATION:
 * ----------
 * Each service gets its own ServiceClassLoader, which means:
 * - Service A's classes are isolated from Service B's classes
 * - Services can have different versions of the same library
 * - We can unload services by discarding their ClassLoader
 * 
 * HIERARCHY:
 * ----------
 * ServiceClassLoader
 * ↓ (parent)
 * System ClassLoader (loads Java standard library)
 * 
 * This means services can use Java's standard library (String, List, etc.)
 * but are isolated from each other.
 */
public class ServiceClassLoader extends URLClassLoader {

    private final String serviceName;

    /**
     * Create a ClassLoader for a service.
     * 
     * @param serviceName Name of the service (for debugging)
     * @param jarPaths    List of JAR files to load (service JAR + dependency JARs)
     * @param parent      Parent classloader (usually the system classloader)
     */
    public ServiceClassLoader(String serviceName, List<Path> jarPaths, ClassLoader parent) {
        super(convertToURLs(jarPaths), parent);
        this.serviceName = serviceName;
    }

    /**
     * Helper method to convert Path objects to URL objects.
     * URLClassLoader needs URLs, not Paths.
     */
    private static URL[] convertToURLs(List<Path> paths) {
        List<URL> urls = new ArrayList<>();

        for (Path path : paths) {
            try {
                urls.add(path.toUri().toURL());
            } catch (Exception e) {
                System.err.println("Failed to convert path to URL: " + path);
                e.printStackTrace();
            }
        }

        return urls.toArray(new URL[0]);
    }

    /**
     * Override toString for better debugging output.
     */
    @Override
    public String toString() {
        return "ServiceClassLoader{service='" + serviceName + "'}";
    }

    /**
     * Get the service name this classloader is for.
     */
    public String getServiceName() {
        return serviceName;
    }
}
