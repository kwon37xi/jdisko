package com.github.kwon37xi.jdisko.commands;

import eu.hansolo.jdktools.OperatingSystem;
import io.foojay.api.discoclient.pkg.Distribution;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name = "distributions",
        aliases = {"dist"},
        description = "list all distributions"
)
public class DistributionsCommand extends BaseCommand implements Runnable {

    @Override
    protected OperatingSystem operatingSystem() {
        return super.operatingSystem();
    }

    @Override
    public void run() {
        final List<Distribution> distributions = discoClient().getDistributions();
        distributions.forEach(distribution -> {
            System.out.printf("%20s\t%s\t%s%n", distribution.getName().toLowerCase(), distribution.getUiString(), distribution.isMaintained());
        });
    }
}
