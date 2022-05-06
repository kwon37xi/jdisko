package com.github.kwon37xi.jdisko;

import java.io.File;
import java.util.function.Supplier;

/**
 * Environment variable and default values
 */
public enum EnvironmentVariable {
    /**
     * JDK install directory
     */
    JDISKO_HOME(() -> System.getProperty("user.home") + File.separator + ".jdisko"),

    /**
     * default JDK distribution.
     */
    JDISKO_DEFAULT_DISTRIBUTION(() -> Constants.DEFAULT_DISTRIBUTION),

    JDISKO_DEFAULT_OPERATING_SYSTEM(() -> System.getProperty("os.name")),

    JDISKO_DEFAULT_ARCHITECTURE(() -> System.getProperty("os.arch"));

    private Supplier<String> defaultValueSupplier;

    EnvironmentVariable(Supplier<String> defaultValueSupplier) {
        this.defaultValueSupplier = defaultValueSupplier;
    }

    public String getEnvValue() {
        final String environmentValue = System.getenv(name());
        if (environmentValue == null || environmentValue.isBlank()) {
            return defaultValueSupplier.get();
        }
        return environmentValue;
    }
}
