package com.github.kwon37xi.jdisko;

import com.github.kwon37xi.jdisko.commands.DistributionsCommand;
import com.github.kwon37xi.jdisko.commands.InstallCommand;
import com.github.kwon37xi.jdisko.commands.ListCommand;
import com.github.kwon37xi.jdisko.commands.RemoveCommand;
import picocli.CommandLine;

public class JDisKoApp {

    public static void main(String[] args) {
        final JDisKoCommand jdiskoCommand = new JDisKoCommand();
        final CommandLine commandLine = new CommandLine(jdiskoCommand);
        commandLine
                .addSubcommand(new DistributionsCommand())
                .addSubcommand(new ListCommand())
                .addSubcommand(new InstallCommand())
                .addSubcommand(new RemoveCommand());

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
