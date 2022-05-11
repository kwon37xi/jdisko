package com.github.kwon37xi.jdisko.commands;

import com.github.kwon37xi.jdisko.converter.ArchitectureOptionConverter;
import com.github.kwon37xi.jdisko.FileUtils;
import com.github.kwon37xi.jdisko.converter.OperatingSystemOptionConverter;
import com.github.kwon37xi.jdisko.decompressor.Decompressor;
import com.github.kwon37xi.jdisko.decompressor.DecompressorFactory;
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.Latest;
import eu.hansolo.jdktools.OperatingSystem;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Command(
        name = "install",
        aliases = {"i"},
        description = "install JDK"
)
public class InstallCommand extends BaseCommand implements Runnable {
    private static final int DOWNLOAD_BUFFER_BYTE_SIZE = 1048576;

    @Option(names = {"-d", "--distribution"}, description = "target distribution")
    private String distributionStr;

    @Option(names = {"-o", "--operating-system"}, description = "force OS", converter = OperatingSystemOptionConverter.class, required = false)
    private OperatingSystem operatingSystem;

    @Option(names = {"-a", "--architecture"}, description = "force architecture", converter = ArchitectureOptionConverter.class, required = false)
    private Architecture architecture;

    @Option(names = {"-p", "--print-installed-path-only"}, description = "quiet but print installed path only.", defaultValue = "false")
    private boolean printInstalledPathOnly = false;

    @Parameters(paramLabel = "<java-version>", defaultValue = "", description = "target version")
    private String javaVersionStr;

    @Override
    public void run() {
        Distribution distribution = findDistribution(distributionStr);
        OperatingSystem targetOperatingSystem = Optional.ofNullable(operatingSystem).orElseGet(this::operatingSystem);
        Architecture targetArchitecture = Optional.ofNullable(architecture).orElseGet(this::architecture);

        final List<Pkg> packages = findCandidates(distribution, targetOperatingSystem, targetArchitecture);

        log("installing packages : %n%s%n".formatted(packages.stream().map(Pkg::toString).collect(Collectors.joining("\n"))), !printInstalledPathOnly);

        if (packages.isEmpty()) {
            throw new IllegalArgumentException("There are no candidate.");
        }
        if (packages.size() > 1) {
            throw new IllegalArgumentException(String.format("There are too many candidates - %d.", packages.size()));
        }

        final Pkg targetPackage = packages.get(0);
        log("installing - %s%n".formatted(targetPackage.getFileName()), !printInstalledPathOnly);

        verifyDownloadable(targetPackage);
        verifyAlreadyInstalled(targetPackage);

        try {
            final Path downloadFile = Files.createTempFile("jdisko-", targetPackage.getFileName());
            log("Start downloading - %s.%n".formatted(downloadFile), !printInstalledPathOnly);
            FileUtils.addDeleteOnExistHook(downloadFile);
            downloadPackage(targetPackage, downloadFile);
            decompress(targetPackage, downloadFile);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(String.format("Download failed - %s.", targetPackage.getFileName()), e);
        }
    }

    private void verifyAlreadyInstalled(Pkg targetPackage) {
        if (Files.exists(packageHome(targetPackage))) {
            throw new IllegalStateException(String.format("JDK %s %s_%s is already installed.",
                    targetPackage.getDistributionName(), targetPackage.getJavaVersion(), targetPackage.getArchitecture().getApiString()));
        }
    }

    private void verifyDownloadable(Pkg targetPackage) {
        if (!targetPackage.isDirectlyDownloadable()) {
            throw new IllegalStateException(String.format("JDK %s %s is not downloadable.", targetPackage.getDistributionName(), targetPackage.getFileName()));
        }
    }

    private List<Pkg> findCandidates(Distribution distribution, OperatingSystem targetOperatingSystem, Architecture targetArchitecture) {
        final boolean isTargettingExactVersion = this.javaVersionStr.contains(".");
        if (isTargettingExactVersion) {
            return findPackages(distribution, this.javaVersionStr, targetOperatingSystem, targetArchitecture, null);
        }
        return findPackages(distribution, this.javaVersionStr, targetOperatingSystem, targetArchitecture, Latest.PER_VERSION);
    }

    private void downloadPackage(Pkg targetPackage, Path downloadFile) throws IOException, URISyntaxException {
        final String pkgDirectDownloadUri = discoClient().getPkgDirectDownloadUri(targetPackage.getId());

        try (InputStream dis = new URI(pkgDirectDownloadUri).toURL().openStream();
             BufferedInputStream bdis = new BufferedInputStream(dis);
             FileOutputStream fos = new FileOutputStream(downloadFile.toFile());
             BufferedOutputStream bfos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[DOWNLOAD_BUFFER_BYTE_SIZE];
            int byteRead = 0;
            while ((byteRead = bdis.read(buffer)) >= 0) {
                bfos.write(buffer, 0, byteRead);
                System.out.print("#");
            }
        }

        log("%n%nDownloading succeeded - %s.%n".formatted(targetPackage.getFileName()), !printInstalledPathOnly);
    }

    private void decompress(Pkg targetPackage, Path downloadFile) throws IOException {
        final Decompressor decompressor = DecompressorFactory.decompressorFor(downloadFile);

        log("Decompressing - %s%n".formatted(targetPackage.getFileName()), !printInstalledPathOnly);
        final Path targetPackageDir = packageHome(targetPackage);
        decompressor.decompress(downloadFile, targetPackageDir);
        log("Decompressed to %s%n.".formatted(targetPackageDir.toString()), !printInstalledPathOnly);
        log(targetPackageDir.toFile().getAbsolutePath(), printInstalledPathOnly);
    }

    private void log(String message, boolean printable) {
        if (printable) {
            System.out.println(message);
        }
    }
}
