package jar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarPackager {
    /**
     * Packages compiled .class files from classesDir into jarPath.
     * Adds a MANIFEST.MF with optional Main-Class.
     */
    public Path createJar(Path classesDir, Path jarPath, String mainClassOrNull) {

        try {
            // ensure parent directory exists
            Files.createDirectories(jarPath.getParent());

            // create jar output stream
            try (FileOutputStream fos = new FileOutputStream(jarPath.toFile());
                 JarOutputStream jos = new JarOutputStream(fos)) {

                // 1. Manifest
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

                if (mainClassOrNull != null) {
                    manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClassOrNull);
                }

                // Add manifest entry first
                JarEntry mfEntry = new JarEntry("META-INF/MANIFEST.MF");
                jos.putNextEntry(mfEntry);
                manifest.write(jos);
                jos.closeEntry();

                // 2. Add all compiled classes
                Files.walk(classesDir)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {

                            // Get path relative to classesDir → com/x/Service.class
                            String entryName = classesDir
                                    .relativize(path)
                                    .toString()
                                    .replace("\\", "/"); // Windows fix

                            try {
                                JarEntry entry = new JarEntry(entryName);
                                jos.putNextEntry(entry);

                                Files.copy(path, jos);

                                jos.closeEntry();
                            } catch (IOException e) {
                                throw new RuntimeException("Error writing entry: " + entryName, e);
                            }
                        });
            }

            return jarPath;

        } catch (IOException e) {
            throw new RuntimeException("Error creating jar: " + jarPath, e);
        }
    }

}
