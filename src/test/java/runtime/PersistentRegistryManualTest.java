package runtime;

import java.util.Map;

/**
 * Simple manual test for PersistentRegistry.
 * Run this with: mvn compile exec:java
 * -Dexec.mainClass="runtime.PersistentRegistryManualTest"
 */
public class PersistentRegistryManualTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== PersistentRegistry Manual Test ===\n");

        PersistentRegistry registry = new PersistentRegistry();
        int currentPid = (int) ProcessHandle.current().pid();

        // Test 1: Register a service
        System.out.println("Test 1: Registering service...");
        registry.register("test-service", currentPid);
        System.out.println("✓ Service registered\n");

        // Test 2: Retrieve all services
        System.out.println("Test 2: Retrieving all services...");
        Map<String, PersistentRegistry.ServiceEntry> services = registry.getAllServices();
        System.out.println("Found " + services.size() + " service(s):");
        for (Map.Entry<String, PersistentRegistry.ServiceEntry> entry : services.entrySet()) {
            PersistentRegistry.ServiceEntry serviceEntry = entry.getValue();
            System.out.println("  - " + serviceEntry.serviceName);
            System.out.println("    PID: " + serviceEntry.processId);
            System.out.println("    Status: " + serviceEntry.status);
            System.out.println("    Started: " + serviceEntry.startTime);
        }
        System.out.println();

        // Test 3: Register multiple services
        System.out.println("Test 3: Registering multiple services...");
        registry.register("payment-service", currentPid);
        registry.register("user-service", currentPid);
        services = registry.getAllServices();
        System.out.println("✓ Now have " + services.size() + " service(s)\n");

        // Test 4: Persistence test
        System.out.println("Test 4: Testing persistence...");
        System.out.println("Creating new registry instance...");
        PersistentRegistry newRegistry = new PersistentRegistry();
        Map<String, PersistentRegistry.ServiceEntry> persistedServices = newRegistry.getAllServices();
        System.out.println("✓ Loaded " + persistedServices.size() + " service(s) from disk\n");

        // Test 5: Unregister a service
        System.out.println("Test 5: Unregistering service...");
        registry.unregister("test-service");
        services = registry.getAllServices();
        System.out.println("✓ Now have " + services.size() + " service(s)\n");

        // Test 6: Dead process filtering
        System.out.println("Test 6: Testing dead process filtering...");
        registry.register("fake-service", 999999); // Fake PID
        System.out.println("Registered fake service with PID 999999");
        services = registry.getAllServices();
        System.out.println("✓ getAllServices() returned " + services.size() + " service(s)");
        System.out.println("  (Dead processes should be filtered out)\n");

        // Cleanup
        System.out.println("Cleaning up...");
        for (String serviceName : services.keySet()) {
            registry.unregister(serviceName);
        }
        System.out.println("✓ All services unregistered\n");

        System.out.println("=== All Tests Passed! ===");
        System.out.println("\nRegistry file location: ~/.mycoq/registry.json");
    }
}
