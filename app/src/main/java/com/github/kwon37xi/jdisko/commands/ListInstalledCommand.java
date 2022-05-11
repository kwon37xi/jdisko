package com.github.kwon37xi.jdisko.commands;

import io.foojay.api.discoclient.pkg.Distribution;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(
        name = "list-installed",
        aliases = {"lsi"},
        description = "list installed JDKs"
)
public class ListInstalledCommand extends BaseCommand implements Runnable {

    @Override
    public void run() {
        final Path jdiskoHome = jdiskoHome();

        final List<String> distroApiStrings = discoClient().getDistros().values().stream()
                .map(Distribution::getApiString)
                .toList();

        try {
            final List<Path> installedJDKs = Files.list(jdiskoHome)
                    .filter(Files::isDirectory)
                    .filter(distDir -> distroApiStrings.contains(distDir.getFileName().toString()))
                    .flatMap(distDir -> {
                        try {
                            return Files.list(distDir)
                                    .filter(Files::isDirectory);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            installedJDKs.stream()
                    .forEach(jdkDir -> {
                        final String jdkDirName = jdkDir.getFileName().toString();
                        final int lastIndexOfUnderscore = jdkDirName.lastIndexOf("_");
                        final String jdkVersion = jdkDirName.substring(0, lastIndexOfUnderscore);
                        final String jdkArchitecture = jdkDirName.substring(lastIndexOfUnderscore + 1);
                        System.out.printf("%s %s %s%n", jdkDir.getParent().getFileName(), jdkVersion, jdkArchitecture);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
