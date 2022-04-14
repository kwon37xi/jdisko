package com.github.kwon37xi.jdisko.decompressor;

import java.io.IOException;
import java.nio.file.Path;

public interface Decompressor {
    boolean acceptable(Path compressedFile);
    void decompress(Path compressedFile, Path targetVersionDir) throws IOException;
}
