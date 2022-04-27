package com.github.kwon37xi.jdisko.decompressor;

import com.github.kwon37xi.jdisko.FileUtils;
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

                if (entry.isSymbolicLink()) {
                    final Path linkTarget = newPath.getParent().resolve(entry.getLinkName()).toAbsolutePath();
                    System.out.printf("creating symbolic link %s to %s%n", newPath, linkTarget);
                    Files.createSymbolicLink(newPath, linkTarget);
                    continue;
                }

                if (entry.isLink()) {
                    final Path linkTarget = newPath.getParent().resolve(entry.getLinkName()).toAbsolutePath();
                    System.out.printf("creating link %s to %s%n", newPath, linkTarget);
                    Files.createLink(newPath, linkTarget);
                    continue;
                }

                // tis 전체를 다 읽는게 아니라 tis 가 currentEntry 내에서만 read 를 호출하기 때문에
                // 전체 InputStream이 다 써지는게 아니라 currentEntry의 내용만 복제된다.
                Files.copy(tis, newPath, StandardCopyOption.REPLACE_EXISTING);

                Files.setPosixFilePermissions(newPath, FileUtils.modeToPermissions(entry.getMode()));
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

}
