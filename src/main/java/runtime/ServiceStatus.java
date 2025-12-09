package runtime;

/**
 * Enum representing the lifecycle status of a service.
 * 
 * STARTING - Service is being initialized
 * RUNNING - Service is actively running
 * STOPPED - Service has been stopped gracefully
 * FAILED - Service encountered an error and stopped
 */
public enum ServiceStatus {
    STARTING,
    RUNNING,
    STOPPED,
    FAILED
}
