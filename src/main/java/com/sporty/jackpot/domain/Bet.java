package com.sporty.jackpot.domain;

import java.math.BigDecimal;

/**
 * A bet placed by a user against a jackpot. This is the message that flows
 * through the publisher/consumer; the persisted {@link JackpotContribution}
 * carries its durable trace.
 */
public record Bet(String betId, String userId, String jackpotId, BigDecimal amount) {
}
