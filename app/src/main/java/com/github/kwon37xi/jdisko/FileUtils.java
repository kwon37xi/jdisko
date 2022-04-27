package com.github.kwon37xi.jdisko;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

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

    /**
     * @see <a href="https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/Util.java">jenkins/Util.java#modeToPermission</a>
     */
    public static Set<PosixFilePermission> modeToPermissions(int mode) throws IOException {
        // Anything larger is a file type, not a permission.
        int PERMISSIONS_MASK = 07777;
        // setgid/setuid/sticky are not supported.
        int MAX_SUPPORTED_MODE = 0777;
        mode = mode & PERMISSIONS_MASK;
        if ((mode & MAX_SUPPORTED_MODE) != mode) {
            throw new IOException("Invalid mode: " + mode);
        }
        PosixFilePermission[] allPermissions = PosixFilePermission.values();
        Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);
        for (int i = 0; i < allPermissions.length; i++) {
            if ((mode & 1) == 1) {
                result.add(allPermissions[allPermissions.length - i - 1]);
            }
            mode >>= 1;
        }
        return result;
    }

    public static void addDeleteOnExistHook(Path downloadFile) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.printf("Deleting downloaded file - %s.%n", downloadFile);
                Files.delete(downloadFile);
            } catch (IOException e) {
                System.err.printf("Failed to delete downloaded file '%s'. - %s%n", downloadFile, e.getMessage());
            }
        }));
    }
}
