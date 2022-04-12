package com.github.kwon37xi.jdisko;

import picocli.CommandLine.Command;

@Command(
        name = "jdisko",
        description = "JDisKo JDK installer based on foojay's disco API.",
        versionProvider = JDisKoVersionProvider.class,
        mixinStandardHelpOptions = true
)
public class JDisKoCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("JDisKo JDK installer");
    }
}
