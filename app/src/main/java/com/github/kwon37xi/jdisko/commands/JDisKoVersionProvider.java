package com.github.kwon37xi.jdisko.commands;

import picocli.CommandLine;

public class JDisKoVersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        return new String[] { "0.1"};
    }
}
