package cli.commands;

/**
 * Displays help information for the CLI.
 */
public class HelpCommand implements Command {

    @Override
    public void execute() {
        System.out.println("mycoq-build - A dependency-aware build system for Java projects");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("  java -jar mycoq-build.jar [COMMAND] [OPTIONS]");
        System.out.println();
        System.out.println("COMMANDS:");
        System.out.println("  build [target]    Build all targets or a specific target");
        System.out.println("                    Example: build payment-service");
        System.out.println();
        System.out.println("  clean             Clean build outputs (deletes build/ directory)");
        System.out.println();
        System.out.println("  list              List all available build targets");
        System.out.println();
        System.out.println("  graph             Show dependency graph and build order");
        System.out.println();
        System.out.println("  run <service>     Run a service (starts the runtime engine)");
        System.out.println("                    Example: run payment-service");
        System.out.println();
        System.out.println("  status            Show status of all running services");
        System.out.println();
        System.out.println("  stop <service>    Stop a running service");
        System.out.println("                    Example: stop payment-service");
        System.out.println();
        System.out.println("  help              Show this help message");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  --verbose, -v     Enable verbose output");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  # Build all targets");
        System.out.println("  java -jar mycoq-build.jar build");
        System.out.println();
        System.out.println("  # Build specific target");
        System.out.println("  java -jar mycoq-build.jar build payment-service");
        System.out.println();
        System.out.println("  # List all targets");
        System.out.println("  java -jar mycoq-build.jar list");
        System.out.println();
        System.out.println("  # Run a service");
        System.out.println("  java -jar mycoq-build.jar run payment-service");
        System.out.println();
        System.out.println("  # Check service status");
        System.out.println("  java -jar mycoq-build.jar status");
        System.out.println();
        System.out.println("  # Clean build outputs");
        System.out.println("  java -jar mycoq-build.jar clean");
    }
}
