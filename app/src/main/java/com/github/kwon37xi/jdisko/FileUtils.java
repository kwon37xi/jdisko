package com.github.kwon37xi.jdisko;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {
    private FileUtils() {
        // Utility Class
    }

    /**
     * Delete non-empty directory with Java NIO 2 API.
     * @see <a href="Delete a Directory Recursively in Java">https://www.baeldung.com/java-delete-directory</a>
     */
    public static void deleteDirectory(Path directoryToBeDeleted) throws IOException {
        Files.walkFileTree(directoryToBeDeleted, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
