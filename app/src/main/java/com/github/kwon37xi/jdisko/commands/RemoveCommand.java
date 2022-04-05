package com.github.kwon37xi.jdisko.commands;

import com.github.kwon37xi.jdisko.FileUtils;
import io.foojay.api.discoclient.pkg.Distribution;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Command(
        name = "remove",
        aliases = {"rm"},
        description = "remove installed JDK"
)
public class RemoveCommand extends BaseCommand implements Runnable {
    @Option(names = {"-d", "--distribution"}, description = "target distribution")
    private String distributionStr;

    @Parameters(paramLabel = "<java-version>", defaultValue = "", description = "target Java version")
    private String javaVersion;

    @Override
    public void run() {
        final Distribution distribution = findDistribution(distributionStr);
        final Path jdkHome = javaVersionHome(distribution, javaVersion);

        if (!Files.exists(jdkHome)) {
            throw new IllegalArgumentException(String.format("JDK %s-%s is not installed.", distribution.getApiString(), javaVersion));
        }
        if (!Files.isDirectory(jdkHome)) {
            throw new IllegalStateException(String.format("%s is not directory.", jdkHome));
        }

        try {
            FileUtils.deleteDirectory(jdkHome);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Failed to delete %s.", jdkHome), e);
        }
    }

}
