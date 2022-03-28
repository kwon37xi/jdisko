package com.github.kwon37xi.jdisko;

import com.github.kwon37xi.jdisko.commands.JDisKoVersionProvider;
import com.github.kwon37xi.jdisko.commands.ListCommand;
import picocli.CommandLine.Command;

@Command(
        name = "jdisko",
        description = "JDisKo JDK installer",
        versionProvider = JDisKoVersionProvider.class,
        mixinStandardHelpOptions = true,
        subcommands = {
                ListCommand.class
        }
)
public class JDisKoCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("JDisKo JDK installer");
    }
}
