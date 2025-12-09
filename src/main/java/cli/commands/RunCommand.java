package cli.commands;

import runtime.RuntimeManager;

import java.nio.file.Path;

/**
 * RunCommand starts a service using the runtime execution engine.
 * 
 * Usage: mycoq run <service-name>
 * Example: mycoq run payment-service
 */
public class RunCommand implements Command {

    private final Path workspaceRoot;
    private final Path manifestDir;
    private final String serviceName;

    public RunCommand(Path workspaceRoot, Path manifestDir, String serviceName) {
        this.workspaceRoot = workspaceRoot;
        this.manifestDir = manifestDir;
        this.serviceName = serviceName;
    }

    @Override
    public void execute() throws Exception {
        if (serviceName == null || serviceName.isEmpty()) {
            System.err.println("Error: Service name required");
            System.err.println("Usage: mycoq run <service-name>");
            System.err.println("Example: mycoq run payment-service");
            return;
        }

        RuntimeManager runtimeManager = new RuntimeManager(workspaceRoot, manifestDir);
        runtimeManager.runService(serviceName);

        // Keep the main thread alive so services can run
        System.out.println("\nPress Ctrl+C to stop all services and exit.\n");

        // Wait indefinitely
        Thread.currentThread().join();
    }
}
