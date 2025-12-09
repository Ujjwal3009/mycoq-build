package runtime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceInfo holds metadata about a running (or stopped) service.
 * 
 * This is stored in the RuntimeRegistry and tracks:
 * - Service name
 * - Current status (STARTING, RUNNING, STOPPED, FAILED)
 * - When it started
 * - What port it's using (if applicable)
 * - What dependencies it has
 * - The thread it's running in
 * - Any error message if it failed
 */
public class ServiceInfo {

    private final String name;
    private ServiceStatus status;
    private Instant startTime;
    private Integer port;
    private List<String> dependencies;
    private Thread thread;
    private String error;

    public ServiceInfo(String name) {
        this.name = name;
        this.status = ServiceStatus.STARTING;
        this.dependencies = new ArrayList<>();
    }

    // Getters

    public String getName() {
        return name;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Integer getPort() {
        return port;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public Thread getThread() {
        return thread;
    }

    public String getError() {
        return error;
    }

    // Setters

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Check if the service is currently running.
     */
    public boolean isRunning() {
        return status == ServiceStatus.RUNNING &&
                thread != null &&
                thread.isAlive();
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", port=" + port +
                ", dependencies=" + dependencies +
                ", running=" + isRunning() +
                '}';
    }
}
