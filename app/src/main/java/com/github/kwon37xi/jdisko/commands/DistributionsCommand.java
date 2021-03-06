package com.github.kwon37xi.jdisko.commands;

import io.foojay.api.discoclient.pkg.Distribution;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.List;

@CommandLine.Command(
        name = "distributions",
        aliases = {"dist"},
        description = "list all distributions"
)
public class DistributionsCommand extends BaseCommand implements Runnable {

    @Option(
            names = {"-n", "--names-only"},
            description = "only print distribution names",
            defaultValue = "false"
    )
    private boolean namesOnly = false;

    @Override
    public void run() {
        final List<Distribution> distributions = discoClient().getDistributions();

            if (namesOnly) {
                distributions.forEach(distribution -> System.out.printf("%s%n", distribution.getApiString().toLowerCase()));
            } else {
                distributions.forEach(distribution -> System.out.printf("%20s\t%s\t%s%n", distribution.getApiString().toLowerCase(), distribution.getUiString(), distribution.isMaintained()));
            }
    }
}
