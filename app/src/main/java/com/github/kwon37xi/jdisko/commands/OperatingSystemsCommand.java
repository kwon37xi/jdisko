package com.github.kwon37xi.jdisko.commands;

import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.OperatingSystem;
import picocli.CommandLine.Command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Command(
        name = "operatingsystems",
        aliases = {"os"},
        description = "list all supported operating systems"
)
public class OperatingSystemsCommand extends BaseCommand implements Runnable {

    /**
     * Hard coded supported OSes because {@link OperatingSystem} enum is too complicated.
     */
    private static final List<String> SUPPORTED_OPERATING_SYSTEM_NAMES =
            List.of("linux",
                    "linux-musl",
                    "solaris",
                    "qnx",
                    "aix",
                    "macos",
                    "windows");

    @Override
    public void run() {
        System.out.println("Supported Operating Systems");
        for (String supportedOperatingSystemName : SUPPORTED_OPERATING_SYSTEM_NAMES) {
            System.out.printf("%s%n", supportedOperatingSystemName);
        }
    }
}
