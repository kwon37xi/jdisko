package com.github.kwon37xi.jdisko;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "jdisko",
        description = "JDisKo JDK installer based on foojay's disco API.",
        versionProvider = JDisKoVersionProvider.class,
        mixinStandardHelpOptions = true
)
public class JDisKoCommand implements Runnable {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec commandSpec;
    @Override
    public void run() {
        final CommandLine commandLine = commandSpec.commandLine();
        commandLine.usage(commandLine.getOut());
    }
}
