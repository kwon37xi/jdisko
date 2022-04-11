package com.github.kwon37xi.jdisko.commands;

import java.util.List;
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.OperatingSystem;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "list",
        aliases = {"ls"},
        description = "list JDKs of the target distribution or default distribution"
)
public class ListCommand extends BaseCommand implements Runnable {

    private static final int MAJOR_VERSION_DISABLED = -1;

    @CommandLine.Option(names = {"-d", "--distribution"}, description = "target distribution")
    private String distributionStr;

    @CommandLine.Option(names = {"-m", "--major-version"}, description = "target major version", defaultValue = "-1")
    private int majorJavaVersionNum = MAJOR_VERSION_DISABLED;

    @Override
    public void run() {
        final Distribution distribution = findDistribution(distributionStr);
        final OperatingSystem operatingSystem = operatingSystem();
        final Architecture architecture = architecture();

        System.out.printf("OS %s, arch : %s%n", operatingSystem, architecture);

        final List<Pkg> pkgs = listDistributions(distribution, majorJavaVersionNum);

        pkgs.forEach(pkg -> {
            final Distribution dist = pkg.getDistribution();
            System.out.printf("%s %s %s%n", dist.getName(), pkg.getJavaVersion(), pkg.getFileName());
        });
    }

    private List<Pkg> listDistributions(final Distribution distribution, int majorJavaVersionNum) {
        if (majorJavaVersionNum == MAJOR_VERSION_DISABLED) {
            return findPackages(distribution);
        }
        return findPackages(distribution, String.valueOf(majorJavaVersionNum));
    }


}
