package com.github.kwon37xi.jdisko.commands;

import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.OperatingSystem;
import io.foojay.api.discoclient.pkg.Distribution;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(
        name = "architectures",
        aliases = {"arch"},
        description = "list all supported architectures"
)
public class ArchitecturesCommand extends BaseCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Supported architectures");
        final List<Architecture> standardArchitectures = Arrays.stream(Architecture.values())
                .filter(Architecture::isStandard)
                .filter(architecture -> !architecture.getApiString().isBlank())
                .sorted(Comparator.comparing(Architecture::getApiString))
                .collect(Collectors.toList());

        for (Architecture standardArchitecture : standardArchitectures) {
            System.out.printf("%s%n", standardArchitecture.getApiString());
        }
    }
}
