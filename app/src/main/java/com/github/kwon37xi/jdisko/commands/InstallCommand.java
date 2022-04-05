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
        description= "install JDK"
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
        if (!targetPackage.isDirectlyDownloadable()) {
            throw new IllegalStateException(String.format("JDK %s %s is now downloadable.", targetPackage.getDistributionName(), targetPackage.getFileName()));
        }

        try {
            Path downloadFile = Files.createTempFile("jdisko-", targetPackage.getFileName());
            System.out.printf("Start downloading - %s.%n", downloadFile);
            final Future<?> downloading = discoClient().downloadPkg(targetPackage.getId(), downloadFile.toString());
            downloading.get();
            System.out.printf("Downloading succeeded - %s.%n", targetPackage.getFileName());
            final Decompressor decompressor = DecompressorFactory.decompressorFor(downloadFile);

            System.out.printf("Decompressing - %s%n", targetPackage.getFileName());
            final Path targetDir = packageHome(targetPackage);
            decompressor.decompress(downloadFile, targetDir);
            System.out.printf("Decompressed to %s%n.", targetDir.toString());
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new IllegalStateException(String.format("Download failed - %s.", targetPackage.getFileName()), e);
        }
    }
}
