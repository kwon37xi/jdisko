package com.github.kwon37xi.jdisko.commands;

import io.foojay.api.discoclient.pkg.Distribution;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name = "distributions",
        description = "list all distributions"
)
public class DistributionsCommand extends BaseCommand implements Runnable {

    @Override
    public void run() {
        final List<Distribution> distributions = discoClient().getDistributions();
        distributions.forEach(distribution -> {
            System.out.printf("%20s\t%s%n", distribution.getName(), distribution.getUiString());
        });
    }
}
