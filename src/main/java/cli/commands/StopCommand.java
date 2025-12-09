package cli.commands;

import runtime.RuntimeManager;

import java.nio.file.Path;

/**
 * StopCommand stops a running service.
 * 
 * Usage: mycoq stop <service-name>
 * Example: mycoq stop payment-service
 */
public class StopCommand implements Command {

    private final Path workspaceRoot;
    private final Path manifestDir;
    private final String serviceName;

    public StopCommand(Path workspaceRoot, Path manifestDir, String serviceName) {
        this.workspaceRoot = workspaceRoot;
        this.manifestDir = manifestDir;
        this.serviceName = serviceName;
    }

    @Override
    public void execute() throws Exception {
        if (serviceName == null || serviceName.isEmpty()) {
            System.err.println("Error: Service name required");
            System.err.println("Usage: mycoq stop <service-name>");
            System.err.println("Example: mycoq stop payment-service");
            return;
        }

        RuntimeManager runtimeManager = new RuntimeManager(workspaceRoot, manifestDir);
        runtimeManager.stopService(serviceName);
    }
}
