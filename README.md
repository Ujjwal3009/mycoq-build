# mycoq-build

A dependency-aware build system for Java projects with a powerful CLI.

## Overview

mycoq-build is a custom build tool that manages Java project compilation with automatic dependency resolution. It reads YAML manifest files to understand project structure and builds targets in the correct topological order.

## Features

- 🔧 **Dependency-Aware Building** - Automatically resolves and builds dependencies in the correct order
- 📦 **Multiple Target Types** - Supports SHARED libraries, EXECUTABLE applications, and COMPOSITE bundles
- 🎯 **Selective Building** - Build all targets or specific targets with their dependencies
- 📊 **Dependency Visualization** - View dependency graphs and build order
- 🧹 **Clean Management** - Easy cleanup of build artifacts

## Quick Start

### Prerequisites

- Java 23 or higher
- Maven 3.x

### Build the Project

```bash
mvn clean package
```

### Run the CLI

Use the `mycoq` wrapper script for easy access:

```bash
./mycoq help
./mycoq list
./mycoq build
```

## CLI Commands

### `help` - Show Usage Information

```bash
./mycoq help
```

Displays all available commands, options, and examples.

### `list` - List All Build Targets

```bash
./mycoq list
```

Shows all available build targets with their types and dependencies.

**Example Output:**
```
Available build targets:

  auth-core
    Type: SHARED
    Dependencies: none

  payment-service
    Type: EXECUTABLE
    Dependencies: auth-core, logging-core
```

### `graph` - Show Dependency Graph

```bash
./mycoq graph
```

Displays the dependency graph and topological build order.

**Example Output:**
```
Dependency Graph:

  payment-service (EXECUTABLE)
    depends on:
      → auth-core
      → logging-core

Build Order (topological sort):
  auth-core → logging-core → payment-service → payments-bundle
```

### `build` - Build All Targets

```bash
./mycoq build
```

Builds all targets in dependency order.

### `build <target>` - Build Specific Target

```bash
./mycoq build payment-service
```

Builds only the specified target and its dependencies.

### `clean` - Clean Build Outputs

```bash
./mycoq clean
```

Deletes the `build/` directory and all compiled artifacts.

## Making `mycoq` Available Globally

To run `mycoq` from anywhere without `./`:

### Option 1: Add to PATH

Add this line to your `~/.zshrc` or `~/.bash_profile`:

```bash
export PATH="/Users/ujjwalkumar/IdeaProjects/mycoq-build:$PATH"
```

Reload your shell:
```bash
source ~/.zshrc
```

Now you can run:
```bash
mycoq build
mycoq list
```

### Option 2: Create Symlink

```bash
sudo ln -s /Users/ujjwalkumar/IdeaProjects/mycoq-build/mycoq /usr/local/bin/mycoq
```

## Project Structure

```
mycoq-build/
├── manifests/           # YAML manifest files defining build targets
│   ├── auth-core.yaml
│   ├── logging-core.yaml
│   ├── payment-service.yaml
│   └── payment-bundle.yaml
├── services/            # Source code for each service
│   ├── auth-core/
│   ├── logging-core/
│   └── payment-service/
├── src/main/java/       # Build system source code
│   ├── cli/            # CLI implementation
│   ├── compile/        # Java compilation service
│   ├── exec/           # Build executor
│   ├── fs/             # File system scanner
│   ├── jar/            # JAR packaging
│   └── org/example/    # Main entry point
├── build/              # Build output (generated)
├── mycoq               # CLI wrapper script
└── pom.xml             # Maven configuration
```

## Manifest Files

Build targets are defined in YAML files in the `manifests/` directory.

### Example: Shared Library

```yaml
name: auth-core
type: SHARED
version: 1.0.0
dependencies: []
```

### Example: Executable Application

```yaml
name: payment-service
type: EXECUTABLE
version: 1.0.0
dependencies:
  - auth-core@1.0.0
  - logging-core@1.0.0
```

### Example: Composite Bundle

```yaml
name: payments-bundle
type: COMPOSITE
version: 1.0.0
includes:
  - payment-service@1.0.0
  - auth-core@1.0.0
  - logging-core@1.0.0
```

## Target Types

- **SHARED** - Reusable library components (compiled to JAR)
- **EXECUTABLE** - Runnable applications (compiled to JAR)
- **COMPOSITE** - Bundles that combine multiple targets (no JAR output)

## Build Output

Build artifacts are placed in the `build/` directory:

```
build/
├── auth-core/
│   ├── classes/        # Compiled .class files
│   └── auth-core.jar   # Packaged JAR
├── logging-core/
│   ├── classes/
│   └── logging-core.jar
└── payment-service/
    ├── classes/
    └── payment-service.jar
```

## Development

### Running Without the Wrapper

If you prefer to run the CLI directly with Java:

```bash
java -cp "target/mycoq-build-1.0-SNAPSHOT.jar:$(cat classpath.txt)" org.example.Main [COMMAND]
```

### Regenerate Classpath

If dependencies change, regenerate the classpath file:

```bash
mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
```

## Troubleshooting

**Error: JAR file not found**
- Run `mvn clean package` to build the project

**Error: command not found: mycoq**
- Use `./mycoq` from the project directory, or
- Add the project to your PATH (see "Making mycoq Available Globally")

**Build fails with compilation errors**
- Check that Java 23 is installed: `java -version`
- Ensure all dependencies are available: `mvn dependency:tree`

## Examples

```bash
# Build the project
mvn clean package

# Show all available commands
./mycoq help

# List all build targets
./mycoq list

# View dependency graph
./mycoq graph

# Build everything
./mycoq build

# Build only payment-service and its dependencies
./mycoq build payment-service

# Clean build outputs
./mycoq clean

# Build again
./mycoq build
```

## License

This project is part of a learning exercise for understanding build systems.
