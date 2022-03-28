package com.github.kwon37xi.jdisko.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "list",
        description = "list JDKs of the target distribution or default distribution"
)
public class ListCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("list JDKs.");
    }
}
