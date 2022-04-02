package com.github.kwon37xi.jdisko.decompressor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DecompressorFactory {
    private static final List<Decompressor> decompressors = new ArrayList<>();

    static {
        decompressors.add(new TarGzDecompressor());
    }

    public static Decompressor decompressorFor(Path archiveFile) {
        return decompressors.stream()
                .filter(decompressor -> decompressor.acceptable(archiveFile))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No decompressor for %s.", archiveFile)));
    }
}
