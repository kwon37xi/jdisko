package com.github.kwon37xi.jdisko.decompressor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

public class TarGzDecompressor implements Decompressor {
    @Override
    public boolean acceptable(Path compressedFile) {
        return compressedFile.toString().toLowerCase().endsWith(".tar.gz");
    }

    /**
     * @param compressedFile
     * @param targetVersionDir
     * @see <a href="https://cdmana.com/2021/07/20210727022326883H.html">Use Java API to compress and decompress tar.gz file and folder</a>
     */
    @Override
    public void decompress(Path compressedFile, Path targetVersionDir) throws IOException {
        Files.createDirectories(targetVersionDir);
        try (InputStream fis = Files.newInputStream(compressedFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GzipCompressorInputStream gzis = new GzipCompressorInputStream(bis);
             TarArchiveInputStream tis = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;
            while ((entry = tis.getNextTarEntry()) != null) {
                System.out.println("Entry : " + entry.getName());
                Path newPath = zipSlipProtect(entry, targetVersionDir);

                if (newPath.equals(targetVersionDir)) {
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                    continue;
                }

                Path parent = newPath.getParent();
                if (parent != null) {
                    if (Files.notExists(parent)) {
                        Files.createDirectories(parent);
                    }
                }

                // tis 전체를 다 읽는게 아니라 tis 가 currentEntry 내에서만 read 를 호출하기 때문에
                // 전체 InputStream이 다 써지는게 아니라 currentEntry의 내용만 복제된다.
                Files.copy(tis, newPath, StandardCopyOption.REPLACE_EXISTING);

                Files.setPosixFilePermissions(newPath, modeToPermissions(entry.getMode()));
            }

        }
    }

    /**
     * 압축 파일 내부의 파일이 상위디렉토리로 풀리거나 하는 현상이 있는지 확인.
     *
     * @throws IOException
     */
    private Path zipSlipProtect(ArchiveEntry entry, Path targetDir) throws IOException {
        final String entryName = entry.getName();

        final int firstDirIdx = entryName.indexOf("/");

        final String skippedEntryName = entryName.substring(firstDirIdx + 1);

        if (skippedEntryName.isEmpty()) {
            return targetDir;
        }

        Path targetDirResolved = targetDir.resolve(skippedEntryName);

        Path normalizedPath = targetDirResolved.normalize();

        if (!normalizedPath.startsWith(targetDir)) {
            throw new IOException("The compressed file has been damaged : " + entryName);
        }
        return normalizedPath;
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

}
