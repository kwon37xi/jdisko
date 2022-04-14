package com.github.kwon37xi.jdisko.decompressor;

import com.github.kwon37xi.jdisko.FileUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ZipDecompressor implements Decompressor {
    @Override
    public boolean acceptable(Path compressedFile) {
        return compressedFile.toString().toLowerCase().endsWith(".zip");
    }

    @Override
    public void decompress(Path compressedFile, Path targetVersionDir) throws IOException {
        Files.createDirectories(targetVersionDir);
        try (InputStream fis = Files.newInputStream(compressedFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipArchiveInputStream zis = new ZipArchiveInputStream(bis)) {

            ZipArchiveEntry entry;
            while ((entry = zis.getNextZipEntry()) != null) {
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
                Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
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
