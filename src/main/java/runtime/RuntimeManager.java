package runtime;

import com.myboq.manifest.model.Dependency;
import com.myboq.manifest.model.Node;
import com.myboq.manifest.model.NodeFactory;
import com.myboq.manifest.parser.ManifestParser;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * RuntimeManager is the main orchestrator for the runtime execution engine.
 * 
 * It ties together all the components:
 * - RuntimeContext (service metadata)
 * - ServiceClassLoader (dynamic loading)
 * - EntryPointResolver (finding main())
 * - ServiceExecutor (running services)
 * - RuntimeRegistry (tracking services)
 * 
 * This is the high-level API that CLI commands will use.
 */
public class RuntimeManager {

    private final Path workspaceRoot;
    private final Path manifestDir;
    private final Path buildDir;

    private final RuntimeRegistry registry;
    private final EntryPointResolver entryPointResolver;
    private final ServiceExecutor serviceExecutor;

    public RuntimeManager(Path workspaceRoot, Path manifestDir) {
        this.workspaceRoot = workspaceRoot;
        this.manifestDir = manifestDir;
        this.buildDir = workspaceRoot.resolve("build");

        this.registry = new RuntimeRegistry();
        this.entryPointResolver = new EntryPointResolver();
        this.serviceExecutor = new ServiceExecutor();
    }

    /**
     * Run a service.
     * 
     * Complete flow:
     * 1. Load manifest to get service info
     * 2. Create RuntimeContext
     * 3. Load dependency JARs
     * 4. Create ServiceClassLoader
     * 5. Resolve main method
     * 6. Execute service
     * 
     * @param serviceName Name of the service to run
     */
    public void runService(String serviceName) throws Exception {
        System.out.println("\n=== STARTING SERVICE: " + serviceName + " ===\n");

        // Step 1: Load manifest
        Node serviceNode = loadManifest(serviceName);

        // Step 2: Create RuntimeContext
        Path serviceJar = buildDir.resolve(serviceName).resolve(serviceName + ".jar");

        if (!Files.exists(serviceJar)) {
            throw new RuntimeException(
                    "Service JAR not found: " + serviceJar +
                            "\nPlease build the service first: mycoq build " + serviceName);
        }

        RuntimeContext context = new RuntimeContext(serviceName, serviceJar);

        // Step 3: Load dependency JARs
        loadDependencies(context, serviceNode);

        // Step 4: Determine main class
        String mainClass = determineMainClass(serviceName);
        context.setMainClass(mainClass);

        System.out.println("[Runtime] Service JAR: " + serviceJar);
        System.out.println("[Runtime] Dependencies: " + context.getDependencyJars().size());
        System.out.println("[Runtime] Main class: " + mainClass);

        // Step 5: Create ServiceClassLoader
        List<Path> allJars = new ArrayList<>();
        allJars.add(serviceJar);
        allJars.addAll(context.getDependencyJars());

        ServiceClassLoader classLoader = new ServiceClassLoader(
                serviceName,
                allJars,
                ClassLoader.getSystemClassLoader());

        context.setClassLoader(classLoader);

        // Step 6: Resolve main method
        Method mainMethod = entryPointResolver.resolveMainMethod(context);

        // Step 7: Execute service
        serviceExecutor.execute(context, mainMethod, registry);

        System.out.println("\n=== SERVICE STARTED: " + serviceName + " ===\n");
    }

    /**
     * Stop a running service.
     */
    public void stopService(String serviceName) {
        serviceExecutor.stop(serviceName, registry);
    }

    /**
     * Get the runtime registry.
     */
    public RuntimeRegistry getRegistry() {
        return registry;
    }

    /**
     * Load manifest for a service.
     */
    private Node loadManifest(String serviceName) throws Exception {
        Path manifestPath = manifestDir.resolve(serviceName + ".yaml");

        if (!Files.exists(manifestPath)) {
            throw new RuntimeException("Manifest not found: " + manifestPath);
        }

        ManifestParser parser = new ManifestParser();
        return NodeFactory.fromManifest(parser.parse(manifestPath));
    }

    /**
     * Load dependency JARs into the context.
     */
    private void loadDependencies(RuntimeContext context, Node serviceNode) {
        List<Dependency> dependencies = serviceNode.getDependencyList();

        for (Dependency dep : dependencies) {
            String depName = dep.getName();
            Path depJar = buildDir.resolve(depName).resolve(depName + ".jar");

            if (Files.exists(depJar)) {
                context.addDependencyJar(depJar);
                System.out.println("[Runtime] Loaded dependency: " + depName);
            } else {
                System.err.println("[Runtime] Warning: Dependency JAR not found: " + depJar);
            }
        }
    }

    /**
     * Determine the main class for a service.
     * 
     * For now, we use a convention:
     * payment-service → com.example.payment.PaymentApp
     * 
     * Future: Read from manifest
     */
    private String determineMainClass(String serviceName) {
        // Use convention for now
        return EntryPointResolver.getConventionalMainClass(serviceName);
    }
}
