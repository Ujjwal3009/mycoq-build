package runtime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PersistentRegistry.
 * Tests JSON serialization/deserialization and process tracking.
 */
class PersistentRegistryTest {

    private PersistentRegistry registry;
    private Path registryFile;

    @BeforeEach
    void setUp() {
        registry = new PersistentRegistry();
        registryFile = Paths.get(System.getProperty("user.home"), ".mycoq", "registry.json");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test registry file
        if (Files.exists(registryFile)) {
            Files.delete(registryFile);
        }
    }

    @Test
    void testRegisterService() {
        // Register a service
        int currentPid = (int) ProcessHandle.current().pid();
        registry.register("test-service", currentPid);

        // Verify it's in the registry
        Map<String, PersistentRegistry.ServiceEntry> services = registry.getAllServices();
        assertTrue(services.containsKey("test-service"));

        PersistentRegistry.ServiceEntry entry = services.get("test-service");
        assertEquals("test-service", entry.serviceName);
        assertEquals(currentPid, entry.processId);
        assertEquals("RUNNING", entry.status);
        assertNotNull(entry.startTime);
    }

    @Test
    void testUnregisterService() {
        // Register and then unregister
        int currentPid = (int) ProcessHandle.current().pid();
        registry.register("test-service", currentPid);
        registry.unregister("test-service");

        // Verify it's removed
        Map<String, PersistentRegistry.ServiceEntry> services = registry.getAllServices();
        assertFalse(services.containsKey("test-service"));
    }

    @Test
    void testPersistenceAcrossInstances() {
        // Register with first instance
        int currentPid = (int) ProcessHandle.current().pid();
        registry.register("persistent-service", currentPid);

        // Create new instance (simulates different JVM process)
        PersistentRegistry newRegistry = new PersistentRegistry();
        Map<String, PersistentRegistry.ServiceEntry> services = newRegistry.getAllServices();

        // Verify data persisted
        assertTrue(services.containsKey("persistent-service"));
        assertEquals(currentPid, services.get("persistent-service").processId);
    }

    @Test
    void testMultipleServices() {
        int currentPid = (int) ProcessHandle.current().pid();

        // Register multiple services
        registry.register("service-1", currentPid);
        registry.register("service-2", currentPid);
        registry.register("service-3", currentPid);

        Map<String, PersistentRegistry.ServiceEntry> services = registry.getAllServices();
        assertEquals(3, services.size());
        assertTrue(services.containsKey("service-1"));
        assertTrue(services.containsKey("service-2"));
        assertTrue(services.containsKey("service-3"));
    }

    @Test
    void testDeadProcessFiltering() {
        int currentPid = (int) ProcessHandle.current().pid();
        int fakePid = 999999; // Unlikely to exist

        // Register one live and one dead process
        registry.register("live-service", currentPid);
        registry.register("dead-service", fakePid);

        // getAllServices should filter out dead processes
        Map<String, PersistentRegistry.ServiceEntry> services = registry.getAllServices();

        assertTrue(services.containsKey("live-service"));
        assertFalse(services.containsKey("dead-service"),
                "Dead process should be filtered out");
    }

    @Test
    void testRegistryFileCreation() throws IOException {
        // Register a service
        int currentPid = (int) ProcessHandle.current().pid();
        registry.register("test-service", currentPid);

        // Verify registry file was created
        assertTrue(Files.exists(registryFile), "Registry file should be created");

        // Verify it's valid JSON
        String content = Files.readString(registryFile);
        assertTrue(content.contains("test-service"));
        assertTrue(content.contains("RUNNING"));
    }
}
