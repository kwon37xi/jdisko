package com.github.kwon37xi.jdisko.commands;

import eu.hansolo.jdktools.*;
import eu.hansolo.jdktools.versioning.VersionNumber;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public abstract class BaseCommand {
    public static final int MAX_WAIT_ELAPSED_MILLIS = 10000;
    private DiscoClient discoClient;

    public BaseCommand() {
        this.discoClient = new DiscoClient("JDisKo");

        long elapsedMillis = 0L;

        while (!discoClient.isInitialzed()) {
            if (elapsedMillis >= MAX_WAIT_ELAPSED_MILLIS) {
                throw new IllegalStateException(String.format("Failed to initialize disco client. It took too long time(%d)", elapsedMillis));
            }
            try {
                Thread.sleep(100);
                elapsedMillis += 100L;
            } catch (InterruptedException e) {
                throw new IllegalStateException("Failed to initialize disco client.", e);
            }
        }
    }

    protected DiscoClient discoClient() {
        return discoClient;
    }

    protected Distribution defaultDistribution() {
        final Map<String, Distribution> distros = discoClient().getDistros();
        final Distribution defaultDistribution = distros.get("defaultDistribution");
        return defaultDistribution;
    }

    protected Distribution findDistribution(String distributionStr) {
        if (distributionStr == null || distributionStr.isBlank()) {
            return defaultDistribution();
        }
        final Distribution distribution = discoClient().getDistros().get(distributionStr.toLowerCase());
        if (distribution == null) {
            throw new IllegalArgumentException(String.format("distribution '%s' is unknown.", distributionStr));
        }
        return distribution;
    }

    protected OperatingSystem operatingSystem() {
        final String osName = System.getProperty("os.name");
        final OperatingSystem operatingSystem = OperatingSystem.fromText(osName);
        return operatingSystem;
    }

    protected Architecture architecture() {
        final String osArch = System.getProperty("os.arch");
        final Architecture architecture = Architecture.fromText(osArch);
        return architecture;
    }

    protected List<Pkg> findPackages(Distribution distribution, String version) {
        return findPackages(distribution, version, operatingSystem(), architecture());
    }

    protected List<Pkg> findPackages(Distribution distribution, String version, OperatingSystem operatingSystem, Architecture architecture) {
        ArchiveType archiveType = archiveTypeForOS(operatingSystem);
        return discoClient().getPkgs(List.of(distribution),
                VersionNumber.fromText(version),
                Latest.PER_VERSION, operatingSystem,
                operatingSystem.getLibCType(),
                architecture,
                null,
                archiveType,
                PackageType.JDK,
                Boolean.FALSE,
                null,
                List.of(ReleaseStatus.GA),
                null,
                null,
                null);
    }

    private ArchiveType archiveTypeForOS(OperatingSystem operatingSystem) {
        if (operatingSystem.name().toLowerCase().contains("windows")) {
            return ArchiveType.ZIP;
        }
        return ArchiveType.TAR_GZ;
    }

    /**
     *
     */
    protected Path jdiskoHome() {
        return Path.of(System.getProperty("user.home"), ".jdisko");
    }

    protected Path distributionHome(Distribution distribution) {
        return jdiskoHome().resolve(distribution.getName().toLowerCase());
    }

    protected Path packageHome(Pkg jdk) {
        return distributionHome(jdk.getDistribution()).resolve(jdk.getJavaVersion().toString());
    }
}
