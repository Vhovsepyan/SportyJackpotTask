package com.sporty.jackpot.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Production {@link RandomProvider} backed by {@link ThreadLocalRandom}.
 */
@Component
public class ThreadLocalRandomProvider implements RandomProvider {

    @Override
    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }
}
