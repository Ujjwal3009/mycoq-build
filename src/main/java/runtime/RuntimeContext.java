package runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RuntimeContext holds all the metadata and configuration needed to run a
 * service.
 * 
 * Think of this as a "backpack" that contains everything a service needs:
 * - Its name
 * - Where its JAR file is located
 * - Where its dependency JARs are located
 * - Runtime configuration (like ports, environment variables)
 * - The ClassLoader that will load its classes
 * 
 * This context is passed through all runtime stages.
 */
public class RuntimeContext {

    // Basic service information
    private final String serviceName;
    private final Path serviceJar;

    // Dependencies
    private final List<Path> dependencyJars;

    // Runtime configuration (ports, env vars, etc.)
    private final Map<String, String> config;

    // The main class to execute (e.g., "com.example.payment.Main")
    private String mainClass;

    // The ClassLoader that will load this service's classes
    private ClassLoader classLoader;

    /**
     * Create a new RuntimeContext for a service.
     * 
     * @param serviceName Name of the service (e.g., "payment-service")
     * @param serviceJar  Path to the service's JAR file
     */
    public RuntimeContext(String serviceName, Path serviceJar) {
        this.serviceName = serviceName;
        this.serviceJar = serviceJar;
        this.dependencyJars = new ArrayList<>();
        this.config = new HashMap<>();
    }

    // Getters

    public String getServiceName() {
        return serviceName;
    }

    public Path getServiceJar() {
        return serviceJar;
    }

    public List<Path> getDependencyJars() {
        return dependencyJars;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public String getMainClass() {
        return mainClass;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    // Setters and utility methods

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Add a dependency JAR to this service's runtime classpath.
     */
    public void addDependencyJar(Path jarPath) {
        this.dependencyJars.add(jarPath);
    }

    /**
     * Add a configuration value (e.g., port number, environment variable).
     */
    public void addConfig(String key, String value) {
        this.config.put(key, value);
    }

    /**
     * Get a configuration value.
     */
    public String getConfigValue(String key) {
        return config.get(key);
    }

    @Override
    public String toString() {
        return "RuntimeContext{" +
                "serviceName='" + serviceName + '\'' +
                ", serviceJar=" + serviceJar +
                ", dependencyJars=" + dependencyJars.size() +
                ", mainClass='" + mainClass + '\'' +
                '}';
    }
}
