package com.sporty.jackpot.domain;

/**
 * Identifies which {@code RewardStrategy} a jackpot uses to compute the chance
 * of winning. Adding a new type = new enum value + strategy.
 */
public enum RewardType {
    FIXED,
    VARIABLE
}
