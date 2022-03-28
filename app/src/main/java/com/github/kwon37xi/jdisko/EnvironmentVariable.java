package com.github.kwon37xi.jdisko;

import java.util.function.Supplier;

/**
 * Environment variable and default values
 */
public enum EnvironmentVariable {
    /**
     * JDK install directory
     */
    JDISKO_HOME(() -> System.getProperty("user.home")),

    /**
     * default JDK distribution.
     */
    JDISKO_DEFAULT_DISTRIBUTION(() -> Constants.DEFAULT_DISTRIBUTION);

    private Supplier<String> defaultValueSupplier;

    EnvironmentVariable(Supplier<String> defaultValueSupplier) {
        this.defaultValueSupplier = defaultValueSupplier;
    }
}
