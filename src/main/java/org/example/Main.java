package org.example;

import cli.CLI;

import java.nio.file.Path;

/**
 * Main entry point for mycoq-build CLI.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Path workspaceRoot = Path.of(".").toAbsolutePath();
        Path manifestDir = workspaceRoot.resolve("manifests");

        CLI cli = new CLI(workspaceRoot, manifestDir);
        cli.run(args);
    }
}