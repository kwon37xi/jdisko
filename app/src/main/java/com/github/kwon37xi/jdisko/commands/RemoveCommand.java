package com.github.kwon37xi.jdisko.commands;

import com.github.kwon37xi.jdisko.ArchitectureOptionConverter;
import com.github.kwon37xi.jdisko.EnvironmentVariable;
import com.github.kwon37xi.jdisko.FileUtils;
import eu.hansolo.jdktools.Architecture;
import io.foojay.api.discoclient.pkg.Distribution;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Command(
        name = "remove",
        aliases = {"rm"},
        description = "remove installed JDK"
)
public class RemoveCommand extends BaseCommand implements Runnable {
    @Option(names = {"-d", "--distribution"}, description = "target distribution")
    private String distributionStr;

    @Option(names = {"-a", "--architecture"}, description = "force architecture", converter = ArchitectureOptionConverter.class, required = false)
    private Architecture architecture;

    @Parameters(paramLabel = "<java-version>", defaultValue = "", description = "target Java version")
    private String javaVersion;

    @Override
    public void run() {
        final Distribution distribution = findDistribution(distributionStr);

        if (architecture == null) {
            architecture = Architecture.fromText(EnvironmentVariable.JDISKO_DEFAULT_ARCHITECTURE.getEnvValue());
        }

        final Architecture targetArchitecture = Optional.ofNullable(architecture)
                .orElseGet(() -> Architecture.fromText(EnvironmentVariable.JDISKO_DEFAULT_ARCHITECTURE.getEnvValue()));

        final Path jdkHome = findMatchedJavaVersionHome(distribution, javaVersion, targetArchitecture);

        if (!Files.isDirectory(jdkHome)) {
            throw new IllegalStateException(String.format("%s is not directory.", jdkHome));
        }

        try {
            FileUtils.deleteDirectory(jdkHome);
            System.out.printf("JDK %s is deleted.%n", jdkHome);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Failed to delete %s.", jdkHome), e);
        }
    }

    private Path findMatchedJavaVersionHome(Distribution distribution, String javaVersion, Architecture targetArchitecture) {
        List<Architecture> synonyms = new ArrayList<>();
        synonyms.add(targetArchitecture);
        synonyms.addAll(targetArchitecture.getSynonyms());

        for (Architecture currentArchitecture : synonyms) {
            final Path jdkHome = javaVersionHome(distribution, javaVersion, currentArchitecture.getApiString());

            if (Files.exists(jdkHome)) {
                return jdkHome;
            }
        }
        return null;
    }
}
