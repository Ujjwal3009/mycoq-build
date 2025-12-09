package runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * PersistentRegistry saves service information to disk so it can be
 * shared across different JVM processes.
 * 
 * This allows:
 * - Terminal 1: ./mycoq run payment-service
 * - Terminal 2: ./mycoq status (can see the running service)
 * 
 * Registry is stored at: ~/.mycoq/registry.json
 */
public class PersistentRegistry {

    private static final Path REGISTRY_DIR = Paths.get(System.getProperty("user.home"), ".mycoq");
    private static final Path REGISTRY_FILE = REGISTRY_DIR.resolve("registry.json");

    private final ObjectMapper mapper;

    public PersistentRegistry() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create registry directory if it doesn't exist
        try {
            Files.createDirectories(REGISTRY_DIR);
        } catch (IOException e) {
            System.err.println("Warning: Could not create registry directory: " + e.getMessage());
        }
    }

    /**
     * Register a service (save to disk).
     */
    public void register(String serviceName, int processId) {
        try {
            Map<String, ServiceEntry> registry = loadRegistry();

            ServiceEntry entry = new ServiceEntry();
            entry.serviceName = serviceName;
            entry.processId = processId;
            entry.startTime = Instant.now();
            entry.status = "RUNNING";

            registry.put(serviceName, entry);
            saveRegistry(registry);

            System.out.println("[Registry] Registered service: " + serviceName + " (PID: " + processId + ")");
        } catch (IOException e) {
            System.err.println("Warning: Could not save registry: " + e.getMessage());
        }
    }

    /**
     * Get all registered services.
     */
    public Map<String, ServiceEntry> getAllServices() {
        try {
            Map<String, ServiceEntry> registry = loadRegistry();

            // Filter out dead processes
            Map<String, ServiceEntry> alive = new HashMap<>();
            for (Map.Entry<String, ServiceEntry> entry : registry.entrySet()) {
                if (isProcessAlive(entry.getValue().processId)) {
                    alive.put(entry.getKey(), entry.getValue());
                }
            }

            // Save cleaned registry
            if (alive.size() != registry.size()) {
                saveRegistry(alive);
            }

            return alive;
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    /**
     * Unregister a service.
     */
    public void unregister(String serviceName) {
        try {
            Map<String, ServiceEntry> registry = loadRegistry();
            registry.remove(serviceName);
            saveRegistry(registry);
            System.out.println("[Registry] Unregistered service: " + serviceName);
        } catch (IOException e) {
            System.err.println("Warning: Could not update registry: " + e.getMessage());
        }
    }

    /**
     * Load registry from disk.
     */
    private Map<String, ServiceEntry> loadRegistry() throws IOException {
        if (!Files.exists(REGISTRY_FILE)) {
            return new HashMap<>();
        }

        return mapper.readValue(
                REGISTRY_FILE.toFile(),
                mapper.getTypeFactory().constructMapType(HashMap.class, String.class, ServiceEntry.class));
    }

    /**
     * Save registry to disk.
     */
    private void saveRegistry(Map<String, ServiceEntry> registry) throws IOException {
        mapper.writeValue(REGISTRY_FILE.toFile(), registry);
    }

    /**
     * Check if a process is still alive.
     */
    private boolean isProcessAlive(int pid) {
        try {
            // On Unix/Mac: kill -0 checks if process exists without killing it
            Process process = Runtime.getRuntime().exec(new String[] { "kill", "-0", String.valueOf(pid) });
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Service entry stored in registry.
     */
    public static class ServiceEntry {
        public String serviceName;
        public int processId;
        public Instant startTime;
        public String status;
    }
}
