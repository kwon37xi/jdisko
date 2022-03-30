package com.github.kwon37xi.jdisko.commands;

import io.foojay.api.discoclient.DiscoClient;

public abstract class BaseCommand {
    public static final int MAX_WAIT_ELAPSED_MILLIS = 10000;
    private DiscoClient discoClient;

    public BaseCommand() {
        this.discoClient = new DiscoClient("JDisKo");

        long elapsedMillis = 0L;

        while (!discoClient.isInitialzed()) {
            if (elapsedMillis >= MAX_WAIT_ELAPSED_MILLIS) {
                throw new IllegalStateException(String.format("Failed to initialize disco client. It took too long time(%d)", elapsedMillis));
            }
            try {
                Thread.sleep(100);
                elapsedMillis += 100L;
            } catch (InterruptedException e) {
                throw new IllegalStateException("Failed to initialize disco client.", e);
            }
        }
    }

    public DiscoClient discoClient() {
        return discoClient;
    }
}
