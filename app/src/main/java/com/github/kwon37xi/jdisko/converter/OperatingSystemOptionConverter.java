package com.github.kwon37xi.jdisko.converter;

import eu.hansolo.jdktools.OperatingSystem;
import picocli.CommandLine;

public class OperatingSystemOptionConverter implements CommandLine.ITypeConverter<OperatingSystem> {
    @Override
    public OperatingSystem convert(String value) throws Exception {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OperatingSystem.fromText(value);
    }
}
