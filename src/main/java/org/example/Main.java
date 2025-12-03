package org.example;

import java.nio.file.Path;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
        Path workspaceRoot = Path.of(".").toAbsolutePath();
        Path manifestDir = workspaceRoot.resolve("manifests");

        BuildManager manager = new BuildManager();
        manager.build(workspaceRoot, manifestDir);

    }
}