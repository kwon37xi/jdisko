package com.github.kwon37xi.jdisko.commands;

import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;
import java.util.Queue;

@Command(
        name = "list",
        description = "list JDKs of the target distribution or default distribution"
)
public class ListCommand extends BaseCommand implements Runnable {
    @Override
    public void run() {
        final Map<String, Distribution> distros = discoClient().getDistros();
        final Distribution temurin = distros.get("temurin");
        final List<Pkg> pkgs = discoClient().getPkgs(List.of(temurin), null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
        pkgs.forEach(pkg -> {
            final Distribution distribution = pkg.getDistribution();
            System.out.printf("%s %s%n", distribution.getName(), pkg.getJavaVersion());
        });
    }
}
