package cli.commands;

import runtime.RuntimeManager;
import runtime.ServiceInfo;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

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
        RuntimeManager runtimeManager = new RuntimeManager(workspaceRoot, manifestDir);
        List<ServiceInfo> services = runtimeManager.getRegistry().getAllServices();

        if (services.isEmpty()) {
            System.out.println("No services running.");
            return;
        }

        System.out.println("Running Services:");
        System.out.println();

        for (ServiceInfo info : services) {
            System.out.println("  " + info.getName());
            System.out.println("    Status: " + info.getStatus());

            if (info.getStartTime() != null) {
                Duration uptime = Duration.between(info.getStartTime(), Instant.now());
                System.out.println("    Uptime: " + formatDuration(uptime));
            }

            if (info.getPort() != null) {
                System.out.println("    Port: " + info.getPort());
            }

            if (!info.getDependencies().isEmpty()) {
                System.out.println("    Dependencies: " + String.join(", ", info.getDependencies()));
            }

            if (info.getError() != null) {
                System.out.println("    Error: " + info.getError());
            }

            System.out.println();
        }

        System.out.println("Total: " + services.size() + " service(s)");
        System.out.println("Running: " + runtimeManager.getRegistry().getRunningCount() + " service(s)");
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
