package com.sporty.jackpot.service;

/**
 * Supplies the random roll used when evaluating a jackpot win. Abstracted behind
 * an interface so tests can inject deterministic values.
 */
public interface RandomProvider {

    /**
     * @return a uniformly distributed value in the range {@code [0.0, 1.0)}.
     */
    double nextDouble();
}
