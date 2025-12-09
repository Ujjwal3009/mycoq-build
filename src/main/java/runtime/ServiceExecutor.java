package runtime;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * ServiceExecutor runs a service in a controlled environment.
 * 
 * CONCEPT: Threading
 * ------------------
 * In Java, a Thread is a separate path of execution.
 * Your main program runs in the "main thread".
 * We create a new thread for each service so they run independently.
 * 
 * WHY THREADS?
 * ------------
 * - Services can run concurrently (at the same time)
 * - If one service crashes, others keep running
 * - We can monitor and control each service independently
 * 
 * THREAD LIFECYCLE:
 * -----------------
 * 1. Create thread
 * 2. Start thread (calls run() method)
 * 3. Thread executes (service runs)
 * 4. Thread completes or is interrupted
 * 
 * CONTEXT CLASSLOADER:
 * --------------------
 * Each thread has a "context classloader" - the classloader it uses
 * to load classes. We set this to the service's classloader so the
 * service uses the correct classes.
 */
public class ServiceExecutor {

    /**
     * Execute a service's main method in a new thread.
     * 
     * Process:
     * 1. Create a new thread for the service
     * 2. Set the thread's context classloader
     * 3. Invoke the main method
     * 4. Handle errors and completion
     * 
     * @param context    RuntimeContext for the service
     * @param mainMethod The main() method to invoke
     * @param registry   Registry to update service status
     * @return The thread running the service
     */
    public Thread execute(RuntimeContext context, Method mainMethod, RuntimeRegistry registry) {
        String serviceName = context.getServiceName();

        System.out.println("[Executor] Starting service: " + serviceName);

        // Create service info and register it as STARTING
        ServiceInfo info = new ServiceInfo(serviceName);
        info.setStatus(ServiceStatus.STARTING);
        info.setStartTime(Instant.now());
        registry.register(serviceName, info);

        // Create a new thread for this service
        Thread serviceThread = new Thread(() -> {
            try {
                System.out.println("[Executor] Service thread started: " + serviceName);

                // IMPORTANT: Set the context classloader for this thread
                // This ensures the service uses its own classloader
                Thread.currentThread().setContextClassLoader(context.getClassLoader());

                // Update status to RUNNING
                info.setStatus(ServiceStatus.RUNNING);
                System.out.println("[Executor] ✓ Service RUNNING: " + serviceName);

                // Invoke the main method
                // The (Object) cast is needed because main() takes String[] args
                // We pass an empty array for now
                mainMethod.invoke(null, (Object) new String[] {});

                // If we reach here, the service completed normally
                System.out.println("[Executor] Service completed: " + serviceName);
                info.setStatus(ServiceStatus.STOPPED);

            } catch (Exception e) {
                // Service failed
                System.err.println("[Executor] ✗ Service FAILED: " + serviceName);
                e.printStackTrace();

                info.setStatus(ServiceStatus.FAILED);
                info.setError(e.getMessage());
            }
        });

        // Set thread name for debugging
        serviceThread.setName("service-" + serviceName);

        // Store the thread in service info
        info.setThread(serviceThread);

        // Start the thread (this calls the run() method above)
        serviceThread.start();

        return serviceThread;
    }

    /**
     * Stop a running service gracefully.
     * 
     * @param serviceName Name of the service to stop
     * @param registry    Registry containing service info
     */
    public void stop(String serviceName, RuntimeRegistry registry) {
        ServiceInfo info = registry.getService(serviceName);

        if (info == null) {
            System.err.println("[Executor] Service not found: " + serviceName);
            return;
        }

        Thread thread = info.getThread();

        if (thread == null || !thread.isAlive()) {
            System.out.println("[Executor] Service not running: " + serviceName);
            return;
        }

        System.out.println("[Executor] Stopping service: " + serviceName);

        // Interrupt the thread (signals it to stop)
        thread.interrupt();

        // Wait for thread to finish (with timeout)
        try {
            thread.join(5000); // Wait up to 5 seconds

            if (thread.isAlive()) {
                System.err.println("[Executor] Service did not stop gracefully: " + serviceName);
                // In production, you might force-kill here
            } else {
                System.out.println("[Executor] ✓ Service stopped: " + serviceName);
                info.setStatus(ServiceStatus.STOPPED);
            }
        } catch (InterruptedException e) {
            System.err.println("[Executor] Interrupted while stopping service: " + serviceName);
        }
    }
}
