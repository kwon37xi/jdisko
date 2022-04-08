package com.github.kwon37xi.jdisko.commands;

import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.OperatingSystem;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Comparator;
import java.util.List;

@Command(
        name = "list",
        aliases = {"ls"},
        description = "list JDKs of the target distribution or default distribution"
)
public class ListCommand extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-d", "--distribution"}, description = "target distribution")
    private String distributionStr;

    @Override
    public void run() {
        final Distribution distribution = findDistribution(distributionStr);
        final OperatingSystem operatingSystem = operatingSystem();
        final Architecture architecture = architecture();

        System.out.printf("OS %s, arch : %s ", operatingSystem, architecture);
        final List<Pkg> pkgs = findPackages(distribution)
                .stream()
                .sorted(Comparator.comparing(Pkg::getJavaVersion)).toList();
        pkgs.forEach(pkg -> {
            final Distribution dist = pkg.getDistribution();
            System.out.printf("%s %s %s%n", dist.getName(), pkg.getJavaVersion(), pkg);
        });
    }


}
