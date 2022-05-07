package com.github.kwon37xi.jdisko.commands;

import com.github.kwon37xi.jdisko.EnvironmentVariable;
import eu.hansolo.jdktools.*;
import eu.hansolo.jdktools.versioning.Semver;
import eu.hansolo.jdktools.versioning.VersionNumber;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toMap;

public abstract class BaseCommand {
    public static final int MAX_WAIT_ELAPSED_MILLIS = 10000;

    /**
     * Supported Archive Types.
     * Some distributions only support zip even for linux.
     */
    public static final List<ArchiveType> SUPPORTED_ARCHIVE_TYPES = List.of(ArchiveType.TAR_GZ, ArchiveType.TGZ, ArchiveType.ZIP);

    private final DiscoClient discoClient;

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
        final Distribution defaultDistribution = distros.get(EnvironmentVariable.JDISKO_DEFAULT_DISTRIBUTION.getEnvValue());
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
        final String osName = EnvironmentVariable.JDISKO_DEFAULT_OPERATING_SYSTEM.getEnvValue();
        final OperatingSystem operatingSystem = OperatingSystem.fromText(osName);
        return operatingSystem;
    }

    protected Architecture architecture() {
        final String osArch = EnvironmentVariable.JDISKO_DEFAULT_ARCHITECTURE.getEnvValue();
        final Architecture architecture = Architecture.fromText(osArch);
        return architecture;
    }

    protected List<Pkg> findPackages(Distribution distribution) {
        return findPackages(distribution, null, operatingSystem(), architecture(), null);
    }

    protected List<Pkg> findPackages(Distribution distribution, String javaVersion) {
        return findPackages(distribution, javaVersion, operatingSystem(), architecture(), Latest.ALL_OF_VERSION);
    }

    protected List<Pkg> findPackages(Distribution distribution, String javaVersion, OperatingSystem operatingSystem, Architecture architecture) {
        return findPackages(distribution, javaVersion, operatingSystem, architecture, Latest.ALL_OF_VERSION);
    }

    protected List<Pkg> findPackages(Distribution distribution, String javaVersion, OperatingSystem operatingSystem, Architecture architecture, Latest latest) {
        final List<Pkg> packages = discoClient().getPkgs(List.of(distribution),
                Optional.ofNullable(javaVersion).map(VersionNumber::fromText).orElse(null),
                latest,
                operatingSystem,
                operatingSystem.getLibCType(),
                architecture,
                null,
                null,
                PackageType.JDK,
                Boolean.FALSE,
                null,
                List.of(ReleaseStatus.GA),
                null,
                null,
                null);
        final List<Pkg> supportedPackages = packages.stream()
                .filter(pkg -> SUPPORTED_ARCHIVE_TYPES.contains(pkg.getArchiveType()))
                .collect(uniqueByJavaVersionWithSmallerSize())
                .values()
                .stream()
                .sorted(Comparator.comparing(Pkg::getJavaVersion))
                .toList();
        return supportedPackages;
    }

    private Collector<Pkg, ?, Map<Semver, Pkg>> uniqueByJavaVersionWithSmallerSize() {
        return toMap(Pkg::getJavaVersion, Function.identity(), (Pkg p1, Pkg p2) -> p1.getSize() > p2.getSize() ? p2 : p1);
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
        return Path.of(EnvironmentVariable.JDISKO_HOME.getEnvValue());
    }

    protected Path distributionHome(Distribution distribution) {
        return jdiskoHome().resolve(distribution.getName().toLowerCase()).toAbsolutePath();
    }

    protected Path packageHome(Pkg jdk) {
        return packageHome(jdk.getDistribution(), jdk.getJavaVersion(), jdk.getArchitecture());
    }

    protected Path packageHome(Distribution distribution, Semver javaVersion, Architecture architecture) {
        return packageHome(distribution, javaVersion.toString(), architecture.getApiString());
    }

    /**
     * JDK javaVersion home directory
     */
    protected Path packageHome(Distribution distribution, String javaVersion, String architectureApiString) {
        final String javaVersionHomeFinal = String.format("%s_%s", javaVersion, architectureApiString);
        return distributionHome(distribution).resolve(javaVersionHomeFinal).toAbsolutePath();
    }
}
