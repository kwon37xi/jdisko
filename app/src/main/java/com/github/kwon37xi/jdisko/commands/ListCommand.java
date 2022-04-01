package com.github.kwon37xi.jdisko.commands;

import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.Latest;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.PackageType;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import picocli.CommandLine.Command;

import java.util.Comparator;
import java.util.List;

@Command(
        name = "list",
        description = "list JDKs of the target distribution or default distribution"
)
public class ListCommand extends BaseCommand implements Runnable {
    @Override
    public void run() {
        final Distribution defaultDistribution = defaultDistribution();
        final OperatingSystem operatingSystem = operatingSystem();
        final Architecture architecture = architecture();

        System.out.printf("OS %s, arch : %s ", operatingSystem, architecture);
        final List<Pkg> pkgs = discoClient().getPkgs(List.of(defaultDistribution), null, Latest.AVAILABLE, operatingSystem, operatingSystem.getLibCType(), architecture, null, null, PackageType.JDK, null, null, null,
                null, null, null, null);
        pkgs.sort(Comparator.comparing(pkg -> pkg.getJavaVersion()));
        pkgs.forEach(pkg -> {
            final Distribution distribution = pkg.getDistribution();
            System.out.printf("%s %s %s%n", distribution.getName(), pkg.getJavaVersion(), pkg);
        });
    }


}
