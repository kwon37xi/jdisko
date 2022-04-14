package com.github.kwon37xi.jdisko;

import eu.hansolo.jdktools.Architecture;
import picocli.CommandLine;

public class ArchitectureOptionConverter implements CommandLine.ITypeConverter<Architecture> {
    @Override
    public Architecture convert(String value) throws Exception {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Architecture.fromText(value);
    }
}
