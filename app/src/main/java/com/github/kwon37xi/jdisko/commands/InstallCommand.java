package com.github.kwon37xi.jdisko.commands;

import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.OperatingSystem;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.stream.Collectors;

@Command(
        name="install",
        description = "install JDK"
)
public class InstallCommand extends BaseCommand implements Runnable {
    @Option(names = {"-d", "--distribution"}, description = "target distribution")
    private String distributionStr;

    @Parameters(paramLabel = "<version>", defaultValue = "", description = "target version")
    private String version;

    @Override
    public void run() {
        Distribution distribution = findDistribution(distributionStr);
        OperatingSystem operatingSystem = operatingSystem();
        Architecture architecture = architecture();

        final List<Pkg> packages = findPackages(distribution, version, operatingSystem, architecture);
        System.out.printf("installing packages : %n%s%n", packages.stream().map(Pkg::toString).collect(Collectors.joining("\n")));
        if (packages.isEmpty()) {
            throw new IllegalArgumentException("There are no candidate.");
        }
        if (packages.size() > 1) {
            throw new IllegalArgumentException(String.format("There are too many candidates - %d.", packages.size()));
        }

        final Pkg targetPackage = packages.get(0);
        System.out.printf("installing - %s%n", targetPackage.getFileName());
    }
}
