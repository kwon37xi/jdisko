package com.github.kwon37xi.jdisko.commands;

import com.github.kwon37xi.jdisko.decompressor.Decompressor;
import com.github.kwon37xi.jdisko.decompressor.DecompressorFactory;
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.OperatingSystem;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Command(
        name = "install",
        aliases = {"i"},
        description = "install JDK"
)
public class InstallCommand extends BaseCommand implements Runnable {
    @Option(names = {"-d", "--distribution"}, description = "target distribution")
    private String distributionStr;

    @Option(names = {"-p", "--print-installed-path-only"}, description = "quiet but print installed path only.", defaultValue = "false")
    private boolean printInstalledPathOnly = false;

    @Parameters(paramLabel = "<java-version>", defaultValue = "", description = "target version")
    private String javaVersionStr;

    @Override
    public void run() {
        Distribution distribution = findDistribution(distributionStr);
        OperatingSystem operatingSystem = operatingSystem();
        Architecture architecture = architecture();

        final List<Pkg> packages = findPackages(distribution, this.javaVersionStr, operatingSystem, architecture);
        log("installing packages : %n%s%n".formatted(packages.stream().map(Pkg::toString).collect(Collectors.joining("\n"))), !printInstalledPathOnly);

        if (packages.isEmpty()) {
            throw new IllegalArgumentException("There are no candidate.");
        }
        if (packages.size() > 1) {
            throw new IllegalArgumentException(String.format("There are too many candidates - %d.", packages.size()));
        }

        final Pkg targetPackage = packages.get(0);
        log("installing - %s%n".formatted(targetPackage.getFileName()), !printInstalledPathOnly);
        if (!targetPackage.isDirectlyDownloadable()) {
            throw new IllegalStateException(String.format("JDK %s %s is now downloadable.", targetPackage.getDistributionName(), targetPackage.getFileName()));
        }

        try {
            Path downloadFile = Files.createTempFile("jdisko-", targetPackage.getFileName());
            log("Start downloading - %s.%n".formatted(downloadFile), !printInstalledPathOnly);
            final Future<?> downloading = discoClient().downloadPkg(targetPackage.getId(), downloadFile.toString());
            downloading.get();
            log("Downloading succeeded - %s.%n".formatted(targetPackage.getFileName()), !printInstalledPathOnly);
            final Decompressor decompressor = DecompressorFactory.decompressorFor(downloadFile);

            log("Decompressing - %s%n".formatted(targetPackage.getFileName()), !printInstalledPathOnly);
            final Path targetDir = packageHome(targetPackage);
            decompressor.decompress(downloadFile, targetDir);
            log("Decompressed to %s%n.".formatted(targetDir.toString()), !printInstalledPathOnly);
            log(targetDir.toFile().getAbsolutePath(), printInstalledPathOnly);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new IllegalStateException(String.format("Download failed - %s.", targetPackage.getFileName()), e);
        }
    }

    private void log(String message, boolean printable) {
        if (printable) {
            System.out.println(message);
        }
    }
}
