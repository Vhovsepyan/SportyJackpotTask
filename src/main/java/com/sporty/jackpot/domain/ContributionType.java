package com.sporty.jackpot.domain;

/**
 * Identifies which {@code ContributionStrategy} a jackpot uses to turn a bet
 * stake into a pool contribution. Adding a new type = new enum value + strategy.
 */
public enum ContributionType {
    FIXED,
    VARIABLE
}
