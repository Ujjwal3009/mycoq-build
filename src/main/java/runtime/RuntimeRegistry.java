package runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RuntimeRegistry is an in-memory database of all services.
 * 
 * CONCEPT: Registry Pattern
 * --------------------------
 * A registry is a central place to store and look up objects.
 * Think of it like a phone book - you can:
 * - Add entries (register a service)
 * - Look up entries (get service info)
 * - List all entries (get all services)
 * 
 * THREAD SAFETY:
 * --------------
 * Multiple threads might access the registry at the same time:
 * - One thread starting a service
 * - Another thread checking status
 * - Another thread stopping a service
 * 
 * We use ConcurrentHashMap which is thread-safe - multiple threads
 * can read/write without corrupting the data.
 * 
 * WHY WE NEED THIS:
 * -----------------
 * - Track which services are running
 * - Enable "mycoq status" command
 * - Support service discovery (future)
 * - Enable health checks (future)
 */
public class RuntimeRegistry {

    // Thread-safe map of service name → service info
    private final ConcurrentHashMap<String, ServiceInfo> services;

    public RuntimeRegistry() {
        this.services = new ConcurrentHashMap<>();
    }

    /**
     * Register a service in the registry.
     * 
     * @param name Service name
     * @param info Service information
     */
    public void register(String name, ServiceInfo info) {
        services.put(name, info);
        System.out.println("[Registry] Registered service: " + name);
    }

    /**
     * Get information about a specific service.
     * 
     * @param name Service name
     * @return ServiceInfo or null if not found
     */
    public ServiceInfo getService(String name) {
        return services.get(name);
    }

    /**
     * Get all registered services.
     * 
     * @return List of all service info
     */
    public List<ServiceInfo> getAllServices() {
        return new ArrayList<>(services.values());
    }

    /**
     * Get all running services.
     * 
     * @return List of services with status RUNNING
     */
    public List<ServiceInfo> getRunningServices() {
        List<ServiceInfo> running = new ArrayList<>();

        for (ServiceInfo info : services.values()) {
            if (info.isRunning()) {
                running.add(info);
            }
        }

        return running;
    }

    /**
     * Unregister a service (remove from registry).
     * 
     * @param name Service name
     */
    public void unregister(String name) {
        ServiceInfo removed = services.remove(name);
        if (removed != null) {
            System.out.println("[Registry] Unregistered service: " + name);
        }
    }

    /**
     * Check if a service is registered.
     * 
     * @param name Service name
     * @return true if registered
     */
    public boolean isRegistered(String name) {
        return services.containsKey(name);
    }

    /**
     * Get count of registered services.
     */
    public int getServiceCount() {
        return services.size();
    }

    /**
     * Get count of running services.
     */
    public int getRunningCount() {
        return getRunningServices().size();
    }

    /**
     * Print a summary of all services.
     */
    public void printSummary() {
        System.out.println("\n=== RUNTIME REGISTRY ===");
        System.out.println("Total services: " + getServiceCount());
        System.out.println("Running services: " + getRunningCount());
        System.out.println();

        for (ServiceInfo info : services.values()) {
            System.out.println("  " + info.getName() + " - " + info.getStatus());
        }

        System.out.println("========================\n");
    }
}
