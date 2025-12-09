package runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * EntryPointResolver finds and validates the main() method of a service.
 * 
 * CONCEPT: Reflection
 * -------------------
 * Reflection is Java's ability to inspect and call methods at runtime.
 * Normally, you call methods like this:
 * MyClass.myMethod();
 * 
 * But what if you don't know the class name until runtime?
 * That's where reflection comes in:
 * Class<?> clazz = Class.forName("MyClass");
 * Method method = clazz.getMethod("myMethod");
 * method.invoke(null);
 * 
 * WHY WE NEED THIS:
 * -----------------
 * We don't know the service's main class name at compile time.
 * We read it from the manifest or use a convention.
 * Then we use reflection to find and call the main() method.
 * 
 * MAIN METHOD SIGNATURE:
 * ----------------------
 * A valid main method must be:
 * public static void main(String[] args)
 * 
 * We validate all these requirements.
 */
public class EntryPointResolver {

    /**
     * Find and validate the main() method for a service.
     * 
     * Process:
     * 1. Get the main class name from RuntimeContext
     * 2. Load the class using the service's ClassLoader
     * 3. Find the "main" method
     * 4. Validate it has the correct signature
     * 5. Return the Method object
     * 
     * @param context RuntimeContext containing service info
     * @return The main Method object, ready to be invoked
     * @throws RuntimeException if main class not found or invalid
     */
    public Method resolveMainMethod(RuntimeContext context) throws Exception {
        String mainClassName = context.getMainClass();

        if (mainClassName == null || mainClassName.isEmpty()) {
            throw new RuntimeException(
                    "No main class specified for service: " + context.getServiceName());
        }

        System.out.println("[EntryPoint] Resolving main class: " + mainClassName);

        // Step 1: Load the main class using the service's ClassLoader
        ClassLoader classLoader = context.getClassLoader();
        Class<?> mainClass;

        try {
            mainClass = classLoader.loadClass(mainClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Main class not found: " + mainClassName +
                            " in service: " + context.getServiceName(),
                    e);
        }

        System.out.println("[EntryPoint] Main class loaded: " + mainClass.getName());

        // Step 2: Find the main method
        Method mainMethod;

        try {
            // Look for: public static void main(String[] args)
            mainMethod = mainClass.getMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "No main(String[]) method found in class: " + mainClassName, e);
        }

        // Step 3: Validate the method signature
        validateMainMethod(mainMethod, mainClassName);

        System.out.println("[EntryPoint] ✓ Valid main method found");

        return mainMethod;
    }

    /**
     * Validate that the main method has the correct signature.
     * 
     * Requirements:
     * - Must be public
     * - Must be static
     * - Must return void
     * - Must take String[] as parameter
     */
    private void validateMainMethod(Method method, String className) {
        int modifiers = method.getModifiers();

        // Check if public
        if (!Modifier.isPublic(modifiers)) {
            throw new RuntimeException(
                    "main() method must be public in class: " + className);
        }

        // Check if static
        if (!Modifier.isStatic(modifiers)) {
            throw new RuntimeException(
                    "main() method must be static in class: " + className);
        }

        // Check return type is void
        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new RuntimeException(
                    "main() method must return void in class: " + className);
        }

        // Parameter type is already validated by getMethod("main", String[].class)
    }

    /**
     * Determine the main class name using a convention if not explicitly set.
     * 
     * Convention: com.example.<servicename>.<ServiceName>App
     * Example: payment-service → com.example.payment.PaymentApp
     * 
     * @param serviceName The service name (e.g., "payment-service")
     * @return Conventional main class name
     */
    public static String getConventionalMainClass(String serviceName) {
        // Convert "payment-service" to "payment"
        String baseName = serviceName.replace("-service", "");

        // Capitalize first letter: "payment" → "Payment"
        String capitalized = Character.toUpperCase(baseName.charAt(0)) +
                baseName.substring(1);

        // Build conventional class name
        return "com.example." + baseName + "." + capitalized + "App";
    }
}
