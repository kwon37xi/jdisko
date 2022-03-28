package com.github.kwon37xi.jdisko;

import picocli.CommandLine;

public class JDisKoApp {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JDisKoCommand()).execute(args);
        System.exit(exitCode);
    }
}
