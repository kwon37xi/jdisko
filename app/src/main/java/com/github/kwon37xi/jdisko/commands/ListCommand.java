package com.github.kwon37xi.jdisko.commands;

import com.github.kwon37xi.jdisko.ArchitectureOptionConverter;
import com.github.kwon37xi.jdisko.OperatingSystemOptionConverter;
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.OperatingSystem;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;

@Command(
        name = "list",
        aliases = {"ls"},
        description = "list JDKs of the target distribution or default distribution"
)
public class ListCommand extends BaseCommand implements Runnable {

    private static final int MAJOR_VERSION_DISABLED = -1;

    @Option(names = {"-d", "--distribution"}, description = "target distribution")
    private String distributionStr;

    @Option(names = {"-m", "--major-version"}, description = "target major version", defaultValue = "-1")
    private int majorJavaVersionNum = MAJOR_VERSION_DISABLED;

    @Option(names = {"-o", "--operating-system"}, description = "force OS", converter = OperatingSystemOptionConverter.class, required = false)
    private OperatingSystem operatingSystem;

    @Option(names = {"-a", "--architecture"}, description = "force architecture", converter = ArchitectureOptionConverter.class, required = false)
    private Architecture architecture;

    @Override
    public void run() {
        final Distribution distribution = findDistribution(distributionStr);
        OperatingSystem targetOperatingSystem = Optional.ofNullable(operatingSystem).orElseGet(this::operatingSystem);
        Architecture targetArchitecture = Optional.ofNullable(architecture).orElseGet(this::architecture);

        System.out.printf("OS %s, arch : %s%n", targetOperatingSystem, targetArchitecture);

        final List<Pkg> pkgs = listDistributions(distribution, majorJavaVersionNum, targetOperatingSystem, targetArchitecture);

        pkgs.forEach(pkg -> {
            final Distribution dist = pkg.getDistribution();
            System.out.printf("%s %s %s%n", dist.getName(), pkg.getJavaVersion(), pkg.getFileName());
        });
    }

    private List<Pkg> listDistributions(final Distribution distribution, int majorJavaVersionNum, OperatingSystem targetOperatingSystem, Architecture targetArchitecture) {
        if (majorJavaVersionNum == MAJOR_VERSION_DISABLED) {
            return findPackages(distribution, null, targetOperatingSystem, targetArchitecture, null);
        }
        return findPackages(distribution, String.valueOf(majorJavaVersionNum), operatingSystem, targetArchitecture);
    }


}
