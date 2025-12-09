package cli.commands;

import runtime.PersistentRegistry;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * StatusCommand shows the status of all running services.
 * 
 * Usage: mycoq status
 */
public class StatusCommand implements Command {

    private final Path workspaceRoot;
    private final Path manifestDir;

    public StatusCommand(Path workspaceRoot, Path manifestDir) {
        this.workspaceRoot = workspaceRoot;
        this.manifestDir = manifestDir;
    }

    @Override
    public void execute() throws Exception {
        PersistentRegistry persistentRegistry = new PersistentRegistry();
        Map<String, PersistentRegistry.ServiceEntry> services = persistentRegistry.getAllServices();

        if (services.isEmpty()) {
            System.out.println("No services running.");
            return;
        }

        System.out.println("Running Services:");
        System.out.println();

        for (Map.Entry<String, PersistentRegistry.ServiceEntry> entry : services.entrySet()) {
            PersistentRegistry.ServiceEntry serviceEntry = entry.getValue();

            System.out.println("  " + serviceEntry.serviceName);
            System.out.println("    Status: " + serviceEntry.status);
            System.out.println("    PID: " + serviceEntry.processId);

            if (serviceEntry.startTime != null) {
                Duration uptime = Duration.between(serviceEntry.startTime, Instant.now());
                System.out.println("    Uptime: " + formatDuration(uptime));
            }

            System.out.println();
        }

        System.out.println("Total: " + services.size() + " service(s) running");
    }

    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
